package vertx

import java.util.{Timer, TimerTask, UUID}
import java.util.concurrent.ThreadLocalRandom

import com.hazelcast.config.Config
import io.vertx.core.eventbus.{EventBus, Message}
import io.vertx.core.{AsyncResult, DeploymentOptions, Handler, Vertx, VertxOptions, eventbus}
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import vertx.protocol._

import scala.concurrent.{Await, Future, Promise, TimeoutException}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Client {

  val rand = ThreadLocalRandom.current()

  val transactors = Map(
    "0" -> "2551",
    "1" -> "2552"
  )

  case class Account(id: String, var balance: Int)

  def send[S](address: String, payload: Any)(implicit bus: EventBus): Future[Any] = {
    val p = Promise[Any]()

    bus.send(address, payload, (event: AsyncResult[Message[Any]]) => {
      if(event.succeeded()){
        p.success(event.result().body())
      } else {
        println(event.cause())
        p.failure(event.cause())
      }
    })

    p.future
  }

  def main(args: Array[String]): Unit = {

    val cfg = new Config()
    val mgr = new HazelcastClusterManager(cfg)

    //cfg.setProperty("hazelcast.initial.min.cluster.size", "2")

    val netcfg = cfg.getNetworkConfig
    val join = netcfg.getJoin

    val tcp = join.getTcpIpConfig

    join.getMulticastConfig.setEnabled(false)
    tcp.setEnabled(true)

    tcp.addMember("127.0.0.1:2551")
    tcp.addMember("127.0.0.1:2552")

   // netcfg.setPort(3001)

    //val join = netcfg.getJoin
    //join.getTcpIpConfig.addMember("127.0.0.1").setEnabled(true)

    val options = new VertxOptions().setClusterManager(mgr)
    options.setClustered(false)

    options.setBlockedThreadCheckInterval(10000)
    options.setMaxEventLoopExecuteTime((10000 milliseconds) toNanos)

    val p = Promise[EventBus]()

    Vertx.clusteredVertx(options, res => {

      if(res.succeeded()){

        val result = res.result()
        implicit val bus = result.eventBus()
        bus.registerDefaultCodec(classOf[Enqueue], EnqueueCodec)
        bus.registerDefaultCodec(classOf[Release], ReleaseCodec)

        println("CLIENT STARTED....")

        /*send("2551::enqueue", Enqueue("1", Seq("k1", "k2"))).flatMap { _ =>
            send("2551::release", Release("1"))
        }.map { r =>
          println(s"done ${r}")
        }*/

        p.success(result.eventBus())

      } else {
        p.failure(res.cause())
        println("failure!")
      }

    })

    p.future.onComplete {

      case Success(b) =>

        implicit val bus = b

        var accounts = Seq.empty[Account]

        val n = 100

        for(i<-0 until n){
          //val id = UUID.randomUUID.toString
          val id = i.toString

          val balance = rand.nextInt(0, 1000)
          val account = new Account(id, balance)

          accounts = accounts :+ account
        }

        def transaction(i0: Int, i1: Int): Future[Boolean] = {

          val id = UUID.randomUUID.toString

          val a0 = accounts(i0)
          val a1 = accounts(i1)

          val keys = Seq(a0.id, a1.id)

          var requests = Map[String, Enqueue]()

          keys.foreach { k =>
            val s = (k.toInt % transactors.size).toString

            requests.get(k) match {
              case None => requests = requests + (s -> Enqueue(id, Seq(k)))
              case Some(t) => requests = requests + (s -> Enqueue(id, t.keys :+ k))
            }
          }

          val ptmt = Promise[Seq[Boolean]]()
          val timer = new Timer()

          val locks = requests.map{case (s, t) =>
            send(s"${transactors(s)}::enqueue", t)}.toSeq

          timer.schedule(new TimerTask {
            override def run(): Unit = {
              ptmt.failure(new TimeoutException())
            }
          }, TIMEOUT)

          val start = System.currentTimeMillis()

          Future.firstCompletedOf(Seq(Future.sequence(locks), ptmt.future)).map { acks =>
            if(!acks.contains(false)) {
              true
            } else {
              false
            }
          }.recover {case _ =>
            false
          }.map { ok =>

            val elapsed = System.currentTimeMillis() - start

            requests.foreach { case (p, _) =>
              send(s"${transactors(p)}::release", Release(id))
            }

            println(s"tx ${id} done -> ${ok} elapsed: ${elapsed}ms")

            ok
          }
        }

        var tasks = Seq.empty[Future[Boolean]]

        for(i<-0 until 1000){
          val i0 = rand.nextInt(0, n)
          val i1 = rand.nextInt(0, n)

          if(!i0.equals(i1)){
            tasks = tasks :+ transaction(i0, i1)
          }
        }

        val start = System.currentTimeMillis()
        val results = Await.result(Future.sequence(tasks), 5 seconds)
        val elapsed = System.currentTimeMillis() - start

        val size = results.length
        val hits = results.count(_ == true)
        val rate = hits*100/size

        val rps = (size * 1000)/elapsed

        println(s"n: ${size} hits: ${hits} rate: ${rate}% rps: ${rps}\n")

        //Vertx.currentContext().owner().close()

      case Failure(exception) =>

        println(exception)

        //Vertx.currentContext().owner().close()

    }

  }

}

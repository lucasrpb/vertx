package vertx

import java.lang
import java.util.concurrent.ConcurrentLinkedDeque

import io.vertx.core.{AbstractVerticle, Handler}
import io.vertx.core.eventbus
import vertx.protocol._

import scala.collection.concurrent.TrieMap

class Transactor(id: String) extends AbstractVerticle {

  val batch = new ConcurrentLinkedDeque[Transaction]()
  var running = TrieMap[String, Transaction]()

  def release(event: eventbus.Message[Release]): Unit = {

    println(s"releasing... ${event.body().id}")

    running.remove(event.body.id)
    event.reply(true)
  }

  def enqueue(event: eventbus.Message[Enqueue]): Unit = {
    val cmd = event.body()
    val t = Transaction(cmd.id, cmd.keys, event, System.currentTimeMillis())

    if(batch.size() < 10000){
      println(s"\nadding ${t.id}...\n")
      batch.add(t)
    } else {
      event.reply(false)
    }
  }

  override def start(): Unit = {

    val bus = this.vertx.eventBus()
    bus.registerDefaultCodec(classOf[Enqueue], EnqueueCodec)
    bus.registerDefaultCodec(classOf[Release], ReleaseCodec)

    bus.consumer(s"$id::enqueue", enqueue)
    bus.consumer(s"$id::release", release)

    this.vertx.setPeriodic(10L, (event: lang.Long) => {

      var txs = Seq.empty[Transaction]
      val it = batch.iterator()

      while(it.hasNext){
        val t = it.next()
        txs = txs :+ t
      }

      val now = System.currentTimeMillis()

      running = running.filter { case (_, t) =>
        now - t.tmp < TIMEOUT
      }

      var keys = running.map(_._2.keys).flatten.toSeq

      txs.sortBy(_.id).foreach { t =>
        val elapsed = now - t.tmp

        if(elapsed >= TIMEOUT){

          batch.remove(t)
          t.sender.reply(false)

        } else if(!t.keys.exists(keys.contains(_))) {

          batch.remove(t)
          keys = keys ++ t.keys

          t.sender.reply(true)
        }
      }

    })

  }

}

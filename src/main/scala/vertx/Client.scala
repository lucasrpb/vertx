package vertx

import com.hazelcast.config.Config
import io.vertx.core.{AsyncResult, DeploymentOptions, Handler, Vertx, VertxOptions, eventbus}
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import vertx.protocol._

object Client {

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

    Vertx.clusteredVertx(options, res => {

      if(res.succeeded()){

        val result = res.result()
        val bus = result.eventBus()
        bus.registerDefaultCodec(classOf[Enqueue], EnqueueCodec)

        println("CLIENT STARTED....")

        val start = System.currentTimeMillis()
        bus.send("2551::enqueue", Enqueue("1", Seq("k1", "k2")),
          (event: AsyncResult[eventbus.Message[Boolean]]) => {

          val elapsed = System.currentTimeMillis() - start
          println(s"server replied ${event.result().body()} elapsed: ${elapsed}ms")

        })

      } else {
        println("failure!")
      }

    })

  }

}

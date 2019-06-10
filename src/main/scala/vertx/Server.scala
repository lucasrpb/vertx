package vertx

import java.util
import java.util.function.Consumer

import com.hazelcast.config.Config
import io.vertx.core.{AsyncResult, DeploymentOptions, Handler, Vertx, VertxOptions}
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager

object Server {

  def main(args: Array[String]): Unit = {

    val port = args(0)

    val cfg = new Config()
    val mgr = new HazelcastClusterManager(cfg)

    //cfg.setProperty("hazelcast.initial.min.cluster.size", "2")

    val netcfg = cfg.getNetworkConfig
    val join = netcfg.getJoin

    val tcp = join.getTcpIpConfig

    join.getMulticastConfig.setEnabled(false)
    tcp.setEnabled(true)

    netcfg.setPort(port.toInt)

    tcp.addMember("127.0.0.1:2551")
    tcp.addMember("127.0.0.1:2552")

    val options = new VertxOptions().setClusterManager(mgr)

   // options.setClusterHost("localhost")
   // options.setClusterPort(3000)

    Vertx.clusteredVertx(options, res => {

      if(res.succeeded()){

        res.result.deployVerticle("vertx.ServerVerticle", new DeploymentOptions()
          .setInstances(1).setHa(true),
          new Handler[AsyncResult[String]] {
            override def handle(event: AsyncResult[String]): Unit = {

              val cm = options.getClusterManager()

              var nodes = Seq.empty[String]
              cm.getNodes.forEach((t: String) => {
                nodes = nodes :+ t
              })

              println(s"server ${event.succeeded()} cause: ${event.cause()} nodes ${nodes}")

            }
          })

      } else {
        println("failure!")
      }

    })

  }

}

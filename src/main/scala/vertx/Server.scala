package vertx

import java.util.function.Consumer

import com.hazelcast.config.Config
import io.vertx.core.{AsyncResult, DeploymentOptions, Handler, Vertx, VertxOptions}
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager

object Server {

  def main(args: Array[String]): Unit = {

    val cfg = new Config()
    val mgr = new HazelcastClusterManager(cfg)

    //cfg.setProperty("hazelcast.initial.min.cluster.size", "2")

    val netcfg = cfg.getNetworkConfig
    val join = netcfg.getJoin

    val tcp = join.getTcpIpConfig

    tcp.setEnabled(true)
    tcp.setEnabled(false)

    netcfg.setPort(3000)

    val options = new VertxOptions().setClusterManager(mgr)

   // options.setClusterHost("localhost")
   // options.setClusterPort(3000)

    Vertx.clusteredVertx(options, res => {

      if(res.succeeded()){

        res.result.deployVerticle("vertx.ServerVerticle", new DeploymentOptions().setInstances(1),
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
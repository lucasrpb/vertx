package vertx

import com.hazelcast.config.Config
import io.vertx.core.{AsyncResult, DeploymentOptions, Handler, Vertx, VertxOptions}
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager

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

    netcfg.setPort(3001)

    //val join = netcfg.getJoin
    //join.getTcpIpConfig.addMember("127.0.0.1").setEnabled(true)

    val options = new VertxOptions().setClusterManager(mgr)

    Vertx.clusteredVertx(options, res => {

      if(res.succeeded()){

        res.result.deployVerticle("vertx.ClientVerticle", new DeploymentOptions().setInstances(1),
          new Handler[AsyncResult[String]] {
          override def handle(event: AsyncResult[String]): Unit = {

            println(s"client ${event.succeeded()} cause: ${event.cause()}")

          }
        })

      } else {
        println("failure!")
      }

    })

  }

}

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
    options.setBlockedThreadCheckInterval(1000 * 60 * 60)
    options.setMaxEventLoopExecuteTime(1000 * 1000 * 1000)

    Vertx.clusteredVertx(options, res => {

      if(res.succeeded()){

        val deployment = new DeploymentOptions()
          .setInstances(1).setHa(true)

        res.result().deployVerticle(new Transactor(port), deployment,
          new Handler[AsyncResult[String]] {
          override def handle(event: AsyncResult[String]): Unit = {

            println(s"transactor ${event.result} running...")

          }
        })

      } else {
        println("failure!")
      }

    })

  }

}

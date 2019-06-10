package vertx

import io.vertx.core.eventbus.Message
import io.vertx.core.{AbstractVerticle, Handler}
import io.vertx.core.eventbus

class ServerVerticle extends AbstractVerticle {


  override def start(): Unit = {

    val bus = this.vertx.eventBus()
    bus.registerDefaultCodec(classOf[Ping], PingMessageCodec)

    println("SERVER STARTED")

    bus.consumer("test", (event: eventbus.Message[Ping]) => {

      println(s"received ${event.body()}")

      event.reply(Ping("World!"))

    })

  }

}

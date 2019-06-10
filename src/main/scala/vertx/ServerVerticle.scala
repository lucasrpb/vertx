package vertx

import io.vertx.core.eventbus.Message
import io.vertx.core.{AbstractVerticle, Handler}

class ServerVerticle extends AbstractVerticle {

  override def start(): Unit = {

    val bus = this.vertx.eventBus()

    println("SERVER STARTED")

    bus.consumer("test", (event: Message[Any]) => {

      println(s"received ${event.body()}")

      event.reply("World!")

    })

  }

}

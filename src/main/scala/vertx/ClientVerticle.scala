package vertx

import io.vertx.core.{AbstractVerticle, AsyncResult, Handler, eventbus}
import io.vertx.core.eventbus.Message

class ClientVerticle extends AbstractVerticle {

  override def start(): Unit = {

    val bus = this.vertx.eventBus()
    bus.registerDefaultCodec(classOf[Ping], PingMessageCodec)

    println("CLIENT STARTED....")

    val start = System.currentTimeMillis()
    bus.send[Ping]("test", Ping("Hello"), (event: AsyncResult[eventbus.Message[Ping]]) => {

      val elapsed = System.currentTimeMillis() - start
      println(s"server replied ${event.result().body()} elapsed: ${elapsed}ms")

      this.vertx.close()
    })

  }

}

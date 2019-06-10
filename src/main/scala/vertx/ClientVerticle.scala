package vertx

import io.vertx.core.{AbstractVerticle, AsyncResult, Handler}
import io.vertx.core.eventbus.Message

class ClientVerticle extends AbstractVerticle {

  override def start(): Unit = {

    val bus = this.vertx.eventBus()

    println("CLIENT STARTED....")


    val start = System.currentTimeMillis()
    bus.send("test", "Hello", (event: AsyncResult[Message[Any]]) => {

      val elapsed = System.currentTimeMillis() - start
      println(s"server replied ${event.result().body()} elapsed: ${elapsed}ms")

    })


  }

}

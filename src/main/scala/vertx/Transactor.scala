package vertx

import io.vertx.core.eventbus.Message
import io.vertx.core.{AbstractVerticle, Handler}
import io.vertx.core.eventbus
import vertx.protocol._

class Transactor(id: String) extends AbstractVerticle {

  override def start(): Unit = {

    val bus = this.vertx.eventBus()
    bus.registerDefaultCodec(classOf[Enqueue], EnqueueCodec)

    bus.consumer(s"$id::enqueue", (event: eventbus.Message[Enqueue]) => {

      val cmd = event.body()

      println(s"received ${cmd}")

      event.reply(true)

    })

  }

}

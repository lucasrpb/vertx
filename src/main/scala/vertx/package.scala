import java.nio.charset.Charset

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import vertx.protocol._

package object vertx {

  trait Command

  object EnqueueCodec extends MessageCodec[Enqueue, Enqueue] {

    override def encodeToWire(buffer: Buffer, s: Enqueue): Unit = {
      buffer.appendBytes(s.toByteArray)
    }

    override def decodeFromWire(pos: Int, buffer: Buffer): Enqueue = {
      Enqueue.parseFrom(buffer.getBytes(pos, buffer.length()))
    }

    override def transform(s: Enqueue): Enqueue = s

    override def name(): String = "EnqueueCodec"

    override def systemCodecID(): Byte = -1
  }

}

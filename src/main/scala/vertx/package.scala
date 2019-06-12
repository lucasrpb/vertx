import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus
import io.vertx.core.eventbus.MessageCodec
import vertx.protocol._

package object vertx {

  val TIMEOUT = 200L

  case class Transaction(id: String, keys: Seq[String], sender: eventbus.Message[Enqueue],
                         tmp: Long = 0L)

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

  object ReleaseCodec extends MessageCodec[Release, Release] {

    override def encodeToWire(buffer: Buffer, s: Release): Unit = {
      buffer.appendBytes(s.toByteArray)
    }

    override def decodeFromWire(pos: Int, buffer: Buffer): Release = {
      Release.parseFrom(buffer.getBytes(pos, buffer.length()))
    }

    override def transform(s: Release): Release = s

    override def name(): String = "ReleaseCodec"

    override def systemCodecID(): Byte = -1
  }

}

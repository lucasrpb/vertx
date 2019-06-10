import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec

package object vertx {

  trait ProtocolMessage

  case class Ping(msg: String) extends ProtocolMessage
  case class Pong(msg: String) extends ProtocolMessage

  object PingMessageCodec extends MessageCodec[Ping, Ping] {

    override def encodeToWire(buffer: Buffer, s: Ping): Unit = {
     // println(s"encoding...")
      buffer.appendString(s.msg)
    }

    override def decodeFromWire(pos: Int, buffer: Buffer): Ping = {
     // println("decoding...")
      Ping(buffer.getString(pos, buffer.length()))
    }

    override def transform(s: Ping): Ping = s

    override def name(): String = "StringCodec"

    override def systemCodecID(): Byte = 1
  }

}

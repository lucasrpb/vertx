import java.nio.charset.Charset

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import scodec._
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs.implicits._
import scodec.stream.{decode, encode}
import scodec._
import scodec.bits._
import codecs._


package object vertx {

  trait Command

  case class Enqueue(tx: String, keys: List[String])

  object EnqueueCodec extends MessageCodec[Enqueue, Enqueue] {

    val codec = (utf8 :: list(utf8)).as[Enqueue]

    override def encodeToWire(buffer: Buffer, s: Enqueue): Unit = {
      val bytes = codec.encode(s).require
      //buffer.appendByte(bytes)
    }

    override def decodeFromWire(pos: Int, buffer: Buffer): Enqueue = {
     // codec.decodeValue().require
      null
    }

    override def transform(s: Enqueue): Enqueue = s

    override def name(): String = "EnqueueCodec"

    override def systemCodecID(): Byte = -1
  }

}

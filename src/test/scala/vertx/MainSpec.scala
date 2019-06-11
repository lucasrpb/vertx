package vertx

import org.scalatest.FlatSpec
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs.implicits._
import scodec.stream.{decode, encode}
import scodec._
import codecs._
import protos.enqueue.Enqueue

import scala.collection.mutable.ListBuffer

class MainSpec extends FlatSpec {

  case class Demo(keys: Seq[String])

  "" should "" in {

    val e = Enqueue("1", Seq("k1", "k2"))

    val bytes = e.toByteArray
    val e1 = Enqueue.parseFrom(bytes)

    println(e1)

  }

}

package vertx

import org.scalatest.FlatSpec
import scodec._
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs.implicits._
import scodec.stream.{decode, encode}
import scodec._
import scodec.bits._
import codecs._

import scala.collection.mutable.ListBuffer

class MainSpec extends FlatSpec {

  case class Demo(keys: Seq[Int])

  "" should "" in {

    val codec = listOfN(uint8, vpbcd)

    val bits = codec.encode(List(4L)).require
    val cmd2 = codec.decodeValue(bits)

    //println(cmd2)

    println(cmd2)
  }

}

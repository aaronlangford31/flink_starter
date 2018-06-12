package com.instructure.pandata.flink_starter.streaming.operators

import com.instructure.pandata.flink_starter.data.RawTaxiRide
import com.sksamuel.avro4s.AvroInputStream
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector

import scala.util.{Failure, Success, Try}

class DeserializeKinesisJson extends ProcessFunction[String, RawTaxiRide] {
  override def processElement(value: String,
                              ctx: ProcessFunction[String, RawTaxiRide]#Context,
                              out: Collector[RawTaxiRide]): Unit = {

    transform(value) match {
      case Success(taxiRide) => out.collect(taxiRide)
      case Failure(_)        => ctx.output(DeserializeKinesisJson.UnparseableRecordTag, value)
    }
  }

  def transform(element: String): Try[RawTaxiRide] = {
    val in    = new ByteInputStream(element.getBytes("UTF-8"), element.size)
    val input = AvroInputStream.json[RawTaxiRide](in)
    input.singleEntity
  }
}

object DeserializeKinesisJson {

  val UnparseableRecordTag: OutputTag[String] =
    OutputTag[String]("unparsable-record")(TypeInformation.of(classOf[String]))
}

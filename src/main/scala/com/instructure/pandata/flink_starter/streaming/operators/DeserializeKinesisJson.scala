package com.instructure.pandata.flink_starter.streaming.operators

import com.instructure.pandata.flink_starter.data.{RawTaxiRide}
import com.sksamuel.avro4s.AvroInputStream
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector

import scala.util.{Failure, Success}

class DeserializeKinesisJson extends ProcessFunction[String, RawTaxiRide] {

  val UnparsableRecordTag: OutputTag[String] =
    OutputTag[String]("unparsable-record")(TypeInformation.of(classOf[String]))

  override def processElement(value: String,
                              ctx: ProcessFunction[String, RawTaxiRide]#Context,
                              out: Collector[RawTaxiRide]): Unit = {
    val in     = new ByteInputStream(value.getBytes("UTF-8"), value.size)
    val input  = AvroInputStream.json[RawTaxiRide](in)
    val result = input.singleEntity

    result match {
      case Success(taxiRide) => out.collect(taxiRide)
      case Failure(_)        => ctx.output(UnparsableRecordTag, value)
    }
  }
}

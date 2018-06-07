package com.instructure.pandata.flink_starter

import java.util.Properties

import com.instructure.pandata.flink_starter.data.ProcessedTaxiRide
import com.instructure.pandata.flink_starter.streaming.operators.{
  DeserializeKinesisJson,
  FilterRides,
  TransformRides
}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants

object ProcessTaxiData {

  def main(args: Array[String]) {
    if (args.length != 4) {
      System.err.println(
        s"USAGE:\n ProcessTaxiDataKafka <aws-region> <stream-initial-position> <kinesis-stream-name> <path-to-output>")
      return
    }

    val kinesisStreamName = args(2)
    val kinesisConfig     = new Properties()
    kinesisConfig.put("aws.region", args(0))
    kinesisConfig.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, args(1))

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val source = env.addSource(getKinesisSource(kinesisStreamName, kinesisConfig))
    val result = run(source)

    result.writeAsText(args(3))

    env.execute("Scala ProcessTaxiData Example")
  }

  def run(input: DataStream[String]): DataStream[ProcessedTaxiRide] = {
    input
      .process(new DeserializeKinesisJson)
      .filter(new FilterRides())
      .process(new TransformRides())(TypeInformation.of(classOf[ProcessedTaxiRide]))
  }

  def getKinesisSource(kinesisStream: String,
                       kinesisConfigs: Properties): FlinkKinesisConsumer[String] = {
    new FlinkKinesisConsumer[String](
      kinesisStream,
      new SimpleStringSchema,
      kinesisConfigs
    )
  }
}

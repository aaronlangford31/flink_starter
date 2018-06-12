package com.instructure.pandata.flink_starter

import java.util.Properties

import com.instructure.pandata.flink_starter.data.{ProcessedTaxiRide, RawTaxiRide}
import com.instructure.pandata.flink_starter.streaming.operators.{
  DeserializeKinesisJson,
  FilterRides,
  TransformRides
}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer
import org.apache.flink.streaming.connectors.kinesis.config.{
  AWSConfigConstants,
  ConsumerConfigConstants
}

object ProcessTaxiData {

  def main(args: Array[String]) {
    if (args.length != 4) {
      System.err.println(
        s"USAGE:\n ProcessTaxiDataKafka <aws-region> <stream-initial-position> <kinesis-stream-name> <path-to-output>")
      return
    }

    val kinesisStreamName = args(2)
    val kinesisConfig     = new Properties()
    kinesisConfig.put(AWSConfigConstants.AWS_REGION, args(0))
    kinesisConfig.put(AWSConfigConstants.AWS_CREDENTIALS_PROVIDER, "AUTO")
    kinesisConfig.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, args(1))

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val source                                               = env.addSource(getKinesisSource(kinesisStreamName, kinesisConfig))
    val (processed, deserializedFailures, transformFailures) = run(source)
    processed.writeAsText(s"${args(3)}/processed.txt")
    deserializedFailures
      .writeAsText(s"${args(3)}/failedOn-DeserializeKinesisJson.txt")
    transformFailures
      .writeAsText(s"${args(3)}/failedOn-TransformRides.txt")
    env.execute("Scala ProcessTaxiData Example")
  }

  def run(input: DataStream[String]): (DataStream[ProcessedTaxiRide],
                                       DataStream[String],
                                       DataStream[RawTaxiRide]) = {

    val deserialized = input
      .process(new DeserializeKinesisJson)
    val processed = deserialized
      .filter(new FilterRides())
      .process(new TransformRides())(TypeInformation.of(classOf[ProcessedTaxiRide]))

    (processed,
     deserialized.getSideOutput(DeserializeKinesisJson.UnparseableRecordTag),
     processed.getSideOutput(TransformRides.UnparsableTimestampTag))
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

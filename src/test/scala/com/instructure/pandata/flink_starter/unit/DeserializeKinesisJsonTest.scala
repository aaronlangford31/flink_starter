package com.instructure.pandata.flink_starter.unit

import com.instructure.pandata.flink_starter.data.{RawTaxiRide, YesNo}
import com.instructure.pandata.flink_starter.streaming.operators.{
  DeserializeKinesisJson,
  FilterRides
}
import org.scalatest._

class DeserializeKinesisJsonTest extends FlatSpec with Matchers {
  "DeserializeKinesisJson" should "successfully parse a normal json record" in {
    val deserialize = new DeserializeKinesisJson()

    val kinesisRecord =
      "{\"trip_distance\": 9.1, \"VendorID\": 1, \"improvement_surcharge\": 0.3, \"tip_amount\": 0.0, \"total_amount\": 31.84, \"tolls_amount\": 5.54, \"tpep_dropoff_datetime\": \"2017-01-01 07:43:00\", \"DOLocationID\": 138, \"extra\": 0.0, \"fare_amount\": 25.5, \"passenger_count\": 1, \"payment_type\": 2, \"mta_tax\": 0.5, \"store_and_fwd_flag\": \"N\", \"PULocationID\": 170, \"RatecodeID\": 1, \"tpep_pickup_datetime\": \"2017-01-01 07:27:53\"}"
    val result = deserialize.transform(kinesisRecord)
    result.isSuccess should be(true)
  }
}

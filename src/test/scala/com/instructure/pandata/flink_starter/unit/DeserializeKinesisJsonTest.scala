package com.instructure.pandata.flink_starter.unit

import com.instructure.pandata.flink_starter.data.{RawTaxiRide, YesNo}
import com.instructure.pandata.flink_starter.streaming.operators.FilterRides
import org.scalatest._

class FilterRidesTest extends FlatSpec with Matchers {
  "FilterRides" should "exclude zero distance trips" in {
    val filterRides = new FilterRides()

    val zeroTripDistanceRide = RawTaxiRide(
      1,
      "2017-01-01 00:00:10",
      "2017-01-01 00:24:52",
      1,
      trip_distance = 0.0f,
      1,
      YesNo.N,
      1,
      1,
      1,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f
    )

    filterRides.filter(zeroTripDistanceRide) should be(false)
  }

  it should "exclude store and forward trips" in {
    val filterRides = new FilterRides()

    val storeAndForwardRide = RawTaxiRide(
      1,
      "2017-01-01 00:00:10",
      "2017-01-01 00:24:52",
      1,
      1.0f,
      1,
      store_and_fwd_flag = YesNo.Y,
      1,
      1,
      1,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f
    )
    filterRides.filter(storeAndForwardRide) should be(false)
  }

  it should "include non-zero, non-store-and-forward trips" in {
    val filterRides = new FilterRides()

    val normalishRide = RawTaxiRide(
      1,
      "2017-01-01 00:00:10",
      "2017-01-01 00:24:52",
      1,
      1.0f,
      1,
      YesNo.N,
      1,
      1,
      1,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f
    )

    filterRides.filter(normalishRide) should be(true)
  }
}

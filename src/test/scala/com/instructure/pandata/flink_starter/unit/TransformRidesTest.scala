package com.instructure.pandata.flink_starter.unit

import com.instructure.pandata.flink_starter.data
import com.instructure.pandata.flink_starter.data.{
  PaymentType,
  ProcessedTaxiRide,
  RawTaxiRide,
  YesNo
}
import com.instructure.pandata.flink_starter.streaming.operators.TransformRides
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.TryValues._

class TransformRidesTest extends FlatSpec with Matchers {
  "TransformRides.transform" should "map pickup timestamp strings to timestamp longs" in {
    val transformRides = new TransformRides()

    val ride1 = RawTaxiRide(
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
    val ride2 = RawTaxiRide(
      1,
      "2017-01-17 18:00:32",
      "2017-01-17 18:03:11",
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

    val result1 = transformRides.transform(ride1)
    val result2 = transformRides.transform(ride2)

    result1.success.value.PickUpTimestamp should equal(1483254010000L)
    result1.success.value.DropOffTimestamp should equal(1483255492000L)
    result2.success.value.PickUpTimestamp should equal(1484701232000L)
    result2.success.value.DropOffTimestamp should equal(1484701391000L)
  }

  it should "fail to parse malformed date time strings" in {
    val transformRides = new TransformRides()

    val pickupMalformed = RawTaxiRide(
      1,
      "once upon a time",
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

    val dropoffMalformed = RawTaxiRide(
      1,
      "2017-01-01 00:00:10",
      "in a galaxy far, far away",
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

    val result1 = transformRides.transform(pickupMalformed)
    val result2 = transformRides.transform(dropoffMalformed)

    result1.isFailure should be(true)
    result2.isFailure should be(true)
  }

  it should "map payment type integers to PaymentType enums" in {
    val transformRides = new TransformRides()

    val cashRide = RawTaxiRide(
      1,
      "2017-01-01 00:00:10",
      "2017-01-01 00:24:52",
      1,
      1.0f,
      1,
      YesNo.N,
      1,
      1,
      payment_type = 1,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f
    )

    val creditRide = RawTaxiRide(
      1,
      "2017-01-01 00:00:10",
      "2017-01-01 00:24:52",
      1,
      1.0f,
      1,
      YesNo.N,
      1,
      1,
      payment_type = 2,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f
    )

    val debitRide = RawTaxiRide(
      1,
      "2017-01-01 00:00:10",
      "2017-01-01 00:24:52",
      1,
      1.0f,
      1,
      YesNo.N,
      1,
      1,
      payment_type = 3,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f
    )

    val otherRide = RawTaxiRide(
      1,
      "2017-01-01 00:00:10",
      "2017-01-01 00:24:52",
      1,
      1.0f,
      1,
      YesNo.N,
      1,
      1,
      payment_type = 4,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f,
      1.0f
    )

    val cashResult   = transformRides.transform(cashRide)
    val creditResult = transformRides.transform(creditRide)
    val debitResult  = transformRides.transform(debitRide)
    val otherResult  = transformRides.transform(otherRide)

    cashResult.success.value.RidePaymentType shouldEqual data.PaymentType.Cash
    creditResult.success.value.RidePaymentType shouldEqual data.PaymentType.Credit
    debitResult.success.value.RidePaymentType shouldEqual data.PaymentType.Debit
    otherResult.success.value.RidePaymentType shouldEqual data.PaymentType.Other
  }
}

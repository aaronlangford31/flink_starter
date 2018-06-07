package com.instructure.pandata.flink_starter.streaming.operators

import com.instructure.pandata.flink_starter.data.PaymentType.PaymentType
import com.instructure.pandata.flink_starter.data.{PaymentType, ProcessedTaxiRide, RawTaxiRide}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.util.{Failure, Success, Try}

class TransformRides extends ProcessFunction[RawTaxiRide, ProcessedTaxiRide] {

  val UnparsableTimestampTag: OutputTag[RawTaxiRide] =
    OutputTag[RawTaxiRide]("unparsable-timestamp")(TypeInformation.of(classOf[RawTaxiRide]))

  override def processElement(value: RawTaxiRide,
                              ctx: ProcessFunction[RawTaxiRide, ProcessedTaxiRide]#Context,
                              out: Collector[ProcessedTaxiRide]): Unit = {
    transform(value) match {
      case Success(processedTaxiRide) => out.collect(processedTaxiRide)
      case Failure(_) => ctx.output(UnparsableTimestampTag, value)
    }
  }

  def transform(element: RawTaxiRide): Try[ProcessedTaxiRide] = {
    val timestamps: Try[(Long, Long)] = try {
      val fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
      val pickup  = DateTime.parse(element.tpep_pickup_datetime, fmt).getMillis
      val dropoff = DateTime.parse(element.tpep_dropoff_datetime, fmt).getMillis
      Success((pickup, dropoff))
    } catch {
      case ex: Throwable => Failure(ex)
    }

    val paymentType: PaymentType = element.payment_type.intValue() match {
      case 1 => PaymentType.Cash
      case 2 => PaymentType.Credit
      case 3 => PaymentType.Debit
      case _ => PaymentType.Other
    }

    timestamps match {
      case Success((pickupTimestamp, dropoffTimestamp)) => {
        Success(
          ProcessedTaxiRide(
            element.VendorID,
            pickupTimestamp,
            dropoffTimestamp,
            element.passenger_count,
            element.trip_distance,
            element.PULocationID,
            element.DOLocationID,
            paymentType,
            element.fare_amount,
            element.extra,
            element.mta_tax,
            element.tip_amount,
            element.tolls_amount,
            element.improvement_surcharge,
            element.total_amount
          ))
      }
      case Failure(err) => Failure(err)
    }
  }
}

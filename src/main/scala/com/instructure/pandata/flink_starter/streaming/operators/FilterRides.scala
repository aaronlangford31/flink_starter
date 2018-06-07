package com.instructure.pandata.flink_starter.streaming.operators

import com.instructure.pandata.flink_starter.data.{RawTaxiRide, YesNo}
import org.apache.flink.api.common.functions.FilterFunction

class FilterRides extends FilterFunction[RawTaxiRide] {
  override def filter(value: RawTaxiRide): Boolean = {
    value.trip_distance > 0.0 && value.store_and_fwd_flag == YesNo.N
  }
}

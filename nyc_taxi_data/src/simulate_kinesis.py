import argparse
import csv
import boto3
from datetime import datetime
import json
import time

def load_data(path_to_data):
    f = open(path_to_data)
    csv_reader = csv.DictReader(f)

    for record in csv_reader:
        yield record

def send_data(key, record, kinesis, stream_name):
    kinesis.put_record(
        StreamName=stream_name,
        Data=record.encode(),
        PartitionKey=key
    )

    print("sent {}".format(record))

__TIME_FMT__ = '%Y-%m-%d %H:%M:%S'
__PARTITION_KEY__ = 'VendorID'
def run_simulation(data, kinesis, stream_name, time_multiplier):
    print("running simulation")
    last = datetime.strptime(data.next()['tpep_pickup_datetime'], __TIME_FMT__)
    for record in data:
        curr = datetime.strptime(record['tpep_pickup_datetime'], __TIME_FMT__)
        wait_for = (curr - last).total_seconds() / time_multiplier
        time.sleep(wait_for if wait_for >= 0.0 else 0.0)

        typed_record = {
            'VendorID': int(record['VendorID']),
            'tpep_pickup_datetime': record['tpep_pickup_datetime'],
            'tpep_dropoff_datetime': record['tpep_dropoff_datetime'],
            'passenger_count': int(record['passenger_count']),
            'trip_distance': float(record['trip_distance']),
            'RatecodeID': int(record['RatecodeID']),
            'store_and_fwd_flag': record['store_and_fwd_flag'],
            'PULocationID': int(record['PULocationID']),
            'DOLocationID': int(record['DOLocationID']),
            'payment_type': int(record['payment_type']),
            'fare_amount': float(record['fare_amount']),
            'extra': float(record['extra']),
            'mta_tax': float(record['mta_tax']),
            'tip_amount': float(record['tip_amount']),
            'tolls_amount': float(record['tolls_amount']),
            'improvement_surcharge': float(record['improvement_surcharge']),
            'total_amount': float(record['total_amount'])
        }

        send_data(str(record[__PARTITION_KEY__]), json.dumps(typed_record), kinesis, stream_name)
        last = curr


def main(args):
    kinesis = boto3.client('kinesis')
    data = load_data(args.path_to_data)
    run_simulation(data, kinesis, args.stream_name, args.time_multiplier)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Send simulated taxi ride data to Kinesis. You will need to set the appropriate AWS environment variables for this program to work.")
    parser.add_argument(
        'stream_name',
        type=str,
        help='the name of the Kinesis stream to send data to'
    )
    parser.add_argument(
        'path_to_data',
        type=str,
        help='the path to the .csv file that contains the data for this simulation'
    )
    parser.add_argument(
        '--time_multiplier',
        type=float,
        nargs='?',
        default=1.0,
        required=False,
        help='a scalar value to increase the speed of the simulation'
    )

    args = parser.parse_args()

    main(args)

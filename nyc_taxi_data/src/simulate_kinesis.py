import argparse
import avro.schema
from avro.datafile import DataFileReader
from avro.io import DatumReader
import boto3
from datetime import datetime
import json
import time

def get_schema(path_to_schema):
    contents = open(path_to_schema).read()
    return avro.schema.parse(contents)

def load_data(path_to_data, avro_schema):
    f = open(path_to_data)
    avro_reader = DataFileReader(f, DatumReader())
    
    for record in avro_reader:
        yield record

def send_data(key, record, kinesis, stream_name):
    kinesis.put_record(
        StreamName=stream_name,
        Data=record.encode(),
        PartitionKey=key
    )

    print("sent {}".format(record))

__TIME_FMT__ = '%Y-%m-%d %I:%M:%S'
__PARTITION_KEY__ = 'VendorID'
def run_simulation(data, kinesis, stream_name, time_multiplier):
    print("running simulation")
    last = datetime.strptime(data.next()['tpep_pickup_datetime'], __TIME_FMT__)
    for record in data:
        curr = datetime.strptime(record['tpep_pickup_datetime'], __TIME_FMT__)
        time.sleep((curr - last).total_seconds() / time_multiplier)

        send_data(str(record[__PARTITION_KEY__]), json.dumps(record), kinesis, stream_name)
        last = curr
      

def main(args):
    kinesis = boto3.client('kinesis')
    avro_schema = get_schema(args.path_to_schema)
    data = load_data(args.path_to_data, avro_schema)
    run_simulation(data, kinesis, args.stream_name, args.time_multiplier)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Send simulated taxi ride data to Kinesis. You will need to set the appropriate AWS environment variables for this program to work.")
    parser.add_argument(
        'stream_name',
        type=str,
        help='the name of the Kinesis stream to send data to'
    )
    parser.add_argument(
        'path_to_schema',
        type=str,
        help='the path to the .avsc that defines the schema of data for this simulation'
    )
    parser.add_argument(
        'path_to_data',
        type=str,
        help='the path to the .avro file that contains the data for this simulation'
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

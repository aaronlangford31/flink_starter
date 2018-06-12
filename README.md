By Aaron Langford (you can complain to @aaron.langford on Slack)

The goals of this project are the following:
* Provide the SBT tooling to develop, test, and build a Flink project written in Scala
* Demonstrate good testing practices for Flink applications
* Allow an engineer to run an end-to-end stream processing job locally

Before you start working on this project, you should be familiar with the following:
* Basics of Docker
* Stream Processing in Flink
    * Overview: https://ci.apache.org/projects/flink/flink-docs-release-1.5/concepts/programming-model.html
    * Streaming Overview: https://ci.apache.org/projects/flink/flink-docs-release-1.5/dev/datastream_api.html
    * Operators: https://ci.apache.org/projects/flink/flink-docs-release-1.5/dev/stream/operators/
    * Side Outputs: https://ci.apache.org/projects/flink/flink-docs-release-1.5/dev/stream/side_output.html
    * Testing: https://ci.apache.org/projects/flink/flink-docs-release-1.5/dev/stream/testing.html
    * Deploying: https://ci.apache.org/projects/flink/flink-docs-release-1.5/ops/deployment/cluster_setup.html
* Scala Test
    * Home Page: http://www.scalatest.org/
* Avro
    * Home Page: https://avro.apache.org/
    * Blog about Avro, Protobuffs, and Thrift: https://martin.kleppmann.com/2012/12/05/schema-evolution-in-avro-protocol-buffers-thrift.html

You should also have the following set up and working:
* Docker & Docker Compose
* Python 2.7
* Java+JDK 8.x
* Scala 2.13
* SBT 1.0
* IntelliJ Community Edition (recommended, but not required)
* Vaulted
* Flink Kinesis Connector
    * You don't get the Kinesis Connector when your project fetches the rest of the Flink dependencies (because Amazon). You'll need to install the Flink Kinesis Connector yourself.
    * See Flink docs for details on how to get this working: https://ci.apache.org/projects/flink/flink-docs-release-1.5/ops/deployment/cluster_setup.html
    * You will be building Flink from source hosted on GitHub, so make sure you install from Flink 1.5, which will require you to checkout the appropriate branch before running `mvn install...`

# Application Overview

This Flink application is pretty simple. Though Flink has much more powerful stream processing capabilities, this application will take a stream of taxi ride records as input and do some transformations to each record. It is a 1 to 1 mapping of input to output data. There are no aggregations, or other stateful stream operations performed on the data. Examples that demonstrate these types of operations are forthcoming.

The flow of data in this project is as follows:
* A small Python project sends JSON taxi ride records to an Amazon Kinesis stream
* Flink reads records from the Kinesis Stream and performs some light transformations
* Flink outputs the results to the local file system
    * There are three distinct results sets that this Flink application will output:
        * Successfully processed records
        * Records that did not match the expected Schema
        * Records that had malformed datetime strings that couldn't be parsed
    * It is a good idea to think about what to do with problematic records as you develop your application, Flink side outputs are leveraged for this very purpose in this application.

# Schema

This project starts by defining the schema of the data to be processed by our Flink application. Both the input and output data of this Flink application are defined using Avro. Here are a couple of really good reasons why schema definition should be the starting point for any data processing application:
1. Testability
    * Writing tests with more complete coverage is easier with schema constraints
2. Re-usability
    * Eventually your input or output data might get reused by someone else in the organization. Data reuse is much easier when there is a schema to help other teams learn about what any data stream contains.

Schema for the in input and output records is defined in `.avsc` files which you can find in `src/main/avro/`.

FYI: The data platform team aspires to provide a centralized schema registry in the future.

# Producer

The next step in the process of creating a Flink application is getting the data into a place where Flink can easily consume it. AWS Kinesis and Kafka are great candidates for source streams that Flink can read from because Flink comes with client libraries for both systems out of the box.

For this project, we will be sending data to an Amazon Kinesis stream. Run the following to get setup:

```$bash
cd nyc_taxi_data
./bin/get_data.sh
cd src && pip install -r requirements.txt
```

Next, you need to create a Kinesis Stream where you will send the data. This can be done via the Amazon Web Console or you can used the AWS CLI (this is a good opportunity to make sure you have a working vaulted shell):

```bash
aws kinesis create-stream --stream-name my-taxi-stream --shard-count 2
```

Now that you have data and a Kinesis Stream, you are ready to make the data flow! The python program that simulates live taxi data is found in `nyc_taxi_data/src/simulate_kinesis.py`. Run it like so:
```bash
python ./src/simulate_kinesis.py my-taxi-stream ./data/taxi_data.csv --time_multiplier 1000
```

The program prints every record that it sends, so you know its working if you see json dumping into your terminal.

It is a goal of this project to allow you to use Kinesalite instead of Kinesis for offline work.

# Flink Application

There are a few options for running your Flink application. We cover them here.

## 1. Running with SBT

This is probably the easiest option, with the fastest iteration time. To run and test this application locally, you can just execute the following:
 
`sbt "run <aws-region> <stream-initial-position> <stream-name> <path-to-output>"`

Make sure you have the appropriate vaulted shell up.

## 2. Running with IntelliJ

You can also run your application from within IntelliJ:  select the classpath of the 'mainRunner' module in the run/debug configurations.
Simply open 'Run -> Edit configurations...' and then select 'mainRunner' from the "Use classpath of module" dropbox.

Make sure that you set up AWS credentials correctly (sorry, you'll need to dig around the inter-webs to figure out how to do this correctly).

## 3. Submitting a Job to Flink

This option will most closely mirror how you will deploy your Flink applications to production. In production, Flink will be running on some cluster in the cloud. You will upload jars to this cluster and start "jobs" to run this application.
 
You can package the application into a fat jar with `sbt assembly`. How you upload and submit it depends on how you have a Flink cluster setup locally.

### Docker

This project supplies a `docker-compose.yml` file that you can use to spin up a JobManager and TaskManager. Just run `docker-compose up` within the appropriate vaulted shell. Then you can use the web UI at `localhost:8081` to upload your jar, start your job, and monitor the results.
 
Disclaimer: The Flink CLI currently does not work with the Docker containers that get spun up with docker-compose. This is because the containers do not publish the BlobServer port. So you are stuck with the Web UI on this path. 

### Local Flink Install

You can setup Flink locally pretty easily as well. Just follow the instructions here: https://ci.apache.org/projects/flink/flink-docs-release-1.5/quickstart/setup_quickstart.html.

Once you run `./bin/start-cluster.sh` (in a vaulted shell), you can submit and run your job like so: 

```
./bin/flink run /path/to/your/project/target/target/scala-2.11/flink_starter-assembly-0.1-SNAPSHOT.jar \
    <aws-region> <stream-initial-position> <stream-name> <path-to-output>
```

You can also use the Web UI as you did in the Docker version.


# Testing

Tests are located in `src/test/scala/com/instructure/pandata/flink_starter/`. This project uses ScalaTest to perform unit and integration (integration examples are forthcoming) tests.

Run tests like so:

```bash
sbt test
```

You can also run a specific test:
```bash
sbt "testOnly com.instructure.pandata.flink_starter.unit.FilterRidesTest"
```

Pro-tip: use the sbt shell: `sbt shell`. It's much faster than running sbt in "batch mode".

Regarding integration tests, the approach recommended by Flink is to write outputs to files and read them back in again for your test assertions. Currently this is the planned approach for integration test examples.
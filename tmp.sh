#!/bin/bash

java -jar target/benchmarks.jar  HasTypeEqualTo >> ~/tmp/HasTypeEqualTo.txt
sleep 90

java -jar target/benchmarks.jar  HasTypeEqualTo >> ~/tmp.2/HasTypeEqualTo.txt
sleep 90

java -jar target/benchmarks.jar  HasTypeEqualTo >> ~/tmp.3/HasTypeEqualTo.txt
sleep 90

java -jar target/benchmarks.jar  HasTypeEqualTo >> ~/tmp.4/HasTypeEqualTo.txt




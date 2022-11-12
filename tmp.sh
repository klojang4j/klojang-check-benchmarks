#!/bin/bash

java -jar target/benchmarks.jar  NotNull_100_Percent_Pass >> ~/tmp/NotNull_100_Percent_Pass.txt
sleep 60
java -jar target/benchmarks.jar  NotNull_099_Percent_Pass >> ~/tmp/NotNull_099_Percent_Pass.txt
sleep 60
java -jar target/benchmarks.jar  NotNull_050_Percent_Pass >> ~/tmp/NotNull_050_Percent_Pass.txt

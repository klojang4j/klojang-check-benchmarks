#!/bin/bash

java -jar target/benchmarks.jar  NotNull_100_Percent_Pass >> ~/tmp/NotNull_100_Percent_Pass.txt
sleep 30
java -jar target/benchmarks.jar  NotNull_099_Percent_Pass >> ~/tmp/NotNull_099_Percent_Pass.txt
sleep 30
java -jar target/benchmarks.jar  NotNull_050_Percent_Pass >> ~/tmp/NotNull_050_Percent_Pass.txt
sleep 30

java -jar target/benchmarks.jar  LessThan_100_Percent_Pass >> ~/tmp/LessThan_100_Percent_Pass.txt
sleep 30
java -jar target/benchmarks.jar  LessThan_099_Percent_Pass >> ~/tmp/LessThan_099_Percent_Pass.txt
sleep 30
java -jar target/benchmarks.jar  LessThan_050_Percent_Pass >> ~/tmp/LessThan_050_Percent_Pass.txt
sleep 30

java -jar target/benchmarks.jar  InstanceOf_100_Percent_Pass >> ~/tmp/InstanceOf_100_Percent_Pass.txt
sleep 30
java -jar target/benchmarks.jar  InstanceOf_100_Percent_Pass >> ~/tmp/InstanceOf_100_Percent_Pass.txt
sleep 30
java -jar target/benchmarks.jar  InstanceOf_100_Percent_Pass >> ~/tmp/InstanceOf_100_Percent_Pass.txt
sleep 30

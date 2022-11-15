#!/bin/bash

java -jar target/benchmarks.jar  ComposeSugarSyntax >> ~/tmp/ComposeSugarSyntax-1.txt
sleep 90

java -jar target/benchmarks.jar  StringFormatting >> ~/tmp/StringFormatting-1.txt
sleep 90

java -jar target/benchmarks.jar  ComposeSugarSyntax >> ~/tmp/ComposeSugarSyntax-2.txt
sleep 90

java -jar target/benchmarks.jar  StringFormatting >> ~/tmp/StringFormatting-2.txt
sleep 90

java -jar target/benchmarks.jar  ComposeSugarSyntax >> ~/tmp/ComposeSugarSyntax-3.txt
sleep 90

java -jar target/benchmarks.jar  StringFormatting >> ~/tmp/StringFormatting-3.txt
sleep 90


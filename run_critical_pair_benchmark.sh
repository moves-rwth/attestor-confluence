#!/bin/bash

mvn compile

for i in {1..50}
do
	echo "Starting benchmark run $i"
	mvn exec:java -Dexec.mainClass="de.rwth.i2.attestor.grammar.confluence.benchmark.CriticalPairDetectionBenchmarkRunner" > /dev/null 2>&1
done


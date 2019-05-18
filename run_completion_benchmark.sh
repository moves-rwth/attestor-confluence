#!/bin/bash

NUM_CORES=$(nproc)
NUM_THREADS=$@

mvn compile

for i in {1..100}
do
	while true
	do
		RUNNING_PROCESSES=$( grep procs_running /proc/stat | cut -d' ' -f2- )
		if test $RUNNING_PROCESSES -lt $(( $NUM_CORES - 2 * $NUM_THREADS ))
		then
			break
		fi
		sleep 10
	done
	echo "Starting benchmark run $i"
	( (time mvn exec:java -Dexec.mainClass="de.rwth.i2.attestor.grammar.confluence.benchmark.CompletionBenchmarkRunner" -Dexec.args="$NUM_THREADS") > log_${i}.txt 2>&1 )&
	sleep 10
done


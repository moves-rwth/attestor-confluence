#!/bin/bash

mvn compile
time mvn exec:java -Dexec.mainClass="de.rwth.i2.attestor.grammar.confluence.benchmark.CompletionBenchmarkRunner"


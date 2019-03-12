#!/bin/bash

for f in *.tex; do
	lualatex $f
done

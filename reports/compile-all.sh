#!/bin/bash

for f in *.tex; do
	( lualatex $f && lualatex $f )&
done

wait

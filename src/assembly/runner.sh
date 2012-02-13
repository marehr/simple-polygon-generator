#!/bin/sh

java -cp polygonsSWP-bin.jar -Djava.library.path=lib polygonsSWP.analysis.AlgorithmRunner "$@"

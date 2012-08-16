#!/bin/sh

# Ugly hack.
cd lib
for i in *0.*
do
	mv $i `echo $i | sed 's/-0\.[0-9]*//g'`
done
cd ..

java -Djava.library.path=lib -jar polygonsSWP-bin.jar "$@"

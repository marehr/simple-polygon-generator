#!/bin/sh

# Ugly hack.
cd lib
for i in `ls *0.* 2>/dev/null`
do
	mv $i `echo $i | sed 's/-0\.[0-9]*//g'`
done
cd ..

java -Djava.library.path=lib -jar polygonsSWP-bin.jar "$@"

#!/bin/bash

# Example batch script to generate polygons with 5 to 50 points, 10 polygons each.
# Save the history to batch.db
# Default algorithm: SpacePartitioning.

for i in `seq 5 50`
do
	echo "Creating 10 polygons with $i points..."
	./run.sh --number=10 --database=batch.db --points=$i
done

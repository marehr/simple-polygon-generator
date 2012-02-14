#!/usr/bin/env bash
if [ $# -ne 2 ]
then
  echo "Usage: convert.sh <filename> <new tablename>"
  exit 0
fi
sed s/Statistic/$2/g $1 > new.sql
cat new.sql | grep -v "sqlite" > $1

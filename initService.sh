#!/bin/bash

OLDIFS=$IFS
IFS=','
while  read -r a b c d e f 
  do  
    curl -i \
    -H "Content-Type: application/json" \
    -u user:$2 -X POST --data '{"timestamp":'$a', "latitude":'$d',"longitude":'$e',"heading":'$f'}' "http://localhost:8080/vehicle/$b/session/$c/position"
  done < $1
IFS=$OLDIFS

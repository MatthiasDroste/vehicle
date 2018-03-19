#!/bin/bash
OLDIFS=$IFS
IFS=','
while  read -r a b c d e f 
  do 
    echo "$a:$b:$c:$d:$e:$f" 
  done < $1
IFS=$OLDIFS

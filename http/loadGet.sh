#!/bin/bash

curl -X POST --location "http://localhost:8080/testing/tag1,tag2" \
    -H "Content-Type: application/xml; charset=UTF-8" \
    -d "<myXml><someOne/></myXml>"

for I in {1..10000}
do
  echo Try existing $I

  curl -X GET --location "http://localhost:8080/testing/tag1,tag2"
done
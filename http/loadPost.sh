#!/bin/bash

for I in {1..10000}
do
  echo Try $I

	curl -X POST --location "http://localhost:8080/testing/tag1,tag2" \
	    -H "Content-Type: application/xml; charset=UTF-8" \
	    -d "<myXml><someOne/></myXml>"
done
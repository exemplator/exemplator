#!/bin/bash
echo The options are: $@
java $@ -DPORT=4567 -jar bin/exemplator.jar
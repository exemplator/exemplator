#!/bin/bash
echo The options are: $@
java $@ -DPORT=4567 -jar build/libs/exemplator-1.0-all.jar
#!/bin/bash

set -e

pwd
#cd graphql/caliban
java -Xms4G -Xmx4G -jar ./target/scala-3.5.1/scala-caliban-assembly-0.1.0-SNAPSHOT.jar

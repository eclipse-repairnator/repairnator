#!/usr/bin/env bash
# running the Java tests of Repairnator on CI

set -e
set -x
export M2_HOME=/usr/share/maven


mvn clean install -DskipTests -B -f  src/repairnator-core/ && mvn ${MAVEN_OPTS} -Dtest=$TEST_LIST clean test -B -f $TEST_PATH -Dmaven.resolver.transport=native  -Daether.connector.connectTimeout=300000 -Daether.connector.requestTimeout=300000

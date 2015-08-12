#!/usr/bin/env bash

cat target/generated-sources/java.js <(echo) target/classes/org.kevoree.modeling.microframework.typescript.js <(echo) target/test-classes/org.kevoree.modeling.microframework.typescript.js <(echo) target/test-classes/TestRunner.js <(echo) > target/node_runner.js
echo "var timeTestSuite = new gentest.FlatJUnitTest();timeTestSuite.run();console.log('Tests run finished!');" >> target/node_runner.js
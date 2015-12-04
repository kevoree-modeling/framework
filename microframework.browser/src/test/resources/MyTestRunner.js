var fs = require('fs');

// file is included here:
eval(fs.readFileSync(__dirname + '/../generated-test-sources/junit.js')+'');

eval(fs.readFileSync(__dirname + '/../classes/microframework.browser.js')+'');
eval(fs.readFileSync(__dirname + '/microframework.browser.js')+'');
eval(fs.readFileSync(__dirname + '/TestRunner.js')+'');

var timeTestSuite = new gentest.FlatJUnitTest();
timeTestSuite.run();

console.log("Tests run finished!");

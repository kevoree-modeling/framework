/*
 var graph = require('ngraph.graph')();
 console.log(graph);

 graph.(1);
 graph.addLink(1, 2);
 */

var graph = generate();


var renderGraph = require('ngraph.pixel');

//console.log(renderGraph);


var renderer = renderGraph(graph);
renderer.on("nodedblclick", function (node) {
    renderer.showNode(node.id);

    console.log("DoubleClick ", node);
});
renderer.on("nodeclick", function (node) {
    kdispatch("nodeSelected",node.id)
});

function generate() {
    var graphGenerators = require('ngraph.generators');
    return graphGenerators.wattsStrogatz(1000, 5, 0.5);
}


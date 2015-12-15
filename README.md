What's new?
===========

## Versions:

The versioning strategy of KMF is lead by the version of the runtime. 
It means that the runtime version will have a version 4.<RUNTIME_VERSION>.<BUGFIX_VERSION>, like 4.26.4.
According to that, all compatible addons with this runtime version will have a version 4.26.x according to corresponding addons version.

### 27.x

* introducing indexes!
* introducing with instanciation = "true" annotation
* various fixes in the runtime such as bounds management

### 26.x

* stable isomorphic compilation


The Kevoree Modeling Framework presentation
===========================================

The Kevoree Modeling Framework (KMF) started as a research project to create an alternative to the [Eclipse Modeling Framework](https://eclipse.org/modeling/emf/) (EMF).
Like EMF, KMF is a modeling framework and code generation facility for building complex object-oriented applications based on structured data models.
While EMF was primarily designed to support design-time models, KMF is specifically designed to support the models@run.time paradigm and targets runtime models.
Runtime models of complex systems usually have high requirements regarding memory usage, runtime performance, and thread safety.
KMF was specifically designed with this requirements in mind. 

The border between large-scale data management systems and models is becoming less and less strict as models@run.time progressively gains maturity through large-scale and distribution mechanisms.
Therefore, since its early days as an EMF alternative KMF evolved to a framework for efficient modeling (structuring), processing, and analyzing large-scale data.
It enables models with millions of elements, which no longer must fit completely into main memory, and supports the distribution of models over thousands of nodes.
 
KMF provides developers a powerful toolset to model, structure, and reason about complex data (during design- and runtime), while still being a 'lightweight' framework trying to introduce as less overhead as possible.
Advanced features like a notion of time, a native per object based versioning concept, distribution support, and easy-to-plug machine learning algorithms make KMF a powerful toolset for structuring, processing, and analyzing data.
A main focus of KMF is on performance and scalability, which are often neglected to a great extend by modeling frameworks.    

KMF supports the definition of formal meta models (inspired by Ecore) with a simple DSL and generates, based on this meta model definition, the necessary facilities to create and manipulate business objects (runtime entities).
Code can be generated in different programming languages, currently we support Java and JavaScript. 
By using isomorphic models (models in different languages which use the same API) the same model can be used for example in a backend server and browser front-end. 

For a more light-weight approach KMF also supports the definition of dynamic meta models, which can be defined and instantiated with a simple API - no formal definition and code generation step is necessary.

Design Principles and Features
------------------------------
As a main design principle KMF was from the beginning designed with strict memory usage, runtime performance, and thread safety requirements in mind.
KMF takes efficiency seriously. 
This includes implementing custom versions of internal core data structures, like hash maps and red-black trees backed by primitive arrays, to improve performance and memory usage.   
To cope at the same time with the large-scale, distributed, and constantly changing nature of modern applications the design of KMF combines ideas from reactive programming, peer-to-peer distribution, big data management, and machine learning.
The distributed aspect of many modern applications lead to the decision to design a completely asynchronous core for KMF. 
Models are defined as observable streams of chunks that are exchanged between nodes in a peer-to-peer manner. 
A lazy loading strategy allows to transparently access the complete virtual model from every node, although chunks are actually distributed across nodes.
Observers and automatic reloading of chunks enable a reactive programming style.

Features:

* native support for temporal data and reasoning
* asynchronous method calls
* support for Java and JavaScript
* code generation based on a meta model definition **and** dynamically instantiated meta models
* easy-to-use API to traverse models
* native mechanisms for distribution 
* native versioning of models on a per-object basis
* ...

Core Modules:
=============

* Domain Specific Language Aka .mm **(https://github.com/kevoree-modeling/dsl)**

Storage Addons:
===============

* LevelDB **(https://github.com/kevoree-modeling/plugin_leveldb)**
* RocksDB **(https://github.com/kevoree-modeling/plugin_rocksdb)**
* WebSocket **(https://github.com/kevoree-modeling/plugin_websocket)**
* Redis **(https://github.com/kevoree-modeling/plugin_redis)**
* MongoDB **(https://github.com/kevoree-modeling/plugin_mongodb)**

Presentation Addons:
====================

* HTML DOM Template **(https://github.com/kevoree-modeling/plugin_template)**

Advanced Computation Addons:
============================

* Advanced Linear Algebra **(https://github.com/kevoree-modeling/plugin_blas)**


Publications:
-------------
Within the scope of the KMF project several research papers have been published: 

**Foundation papers:**

* Francois Fouquet, Gregory Nain, Brice Morin, Erwan Daubert, Olivier Barais, Noel Plouzeau, and Jean-Marc Jézéquel. **An Eclipse Modelling Framework Alternative to Meet the Models@Runtime Requirements**. In ASM/IEEE 15th Model Driven Engineering Languages ​​and Systems (MODELS'12), 2012. [Get the paper here](https://hal.inria.fr/hal-00714558/document) 
* Thomas Hartmann, Francois Fouquet, Gregory Nain, Jacques Klein, Brice Morin, and Yves Le Traon. **Reasoning at runtime using time-distorted contexts: A models@run.time based approach**. In 26th International Conference on Software Engineering and Knowledge Engineering (SEKE'14), 2014. [Get the paper here](http://orbilu.uni.lu/handle/10993/17637)
* Thomas Hartmann, Francois Fouquet, Gregory Nain, Brice Morin, Jacques Klein, Olivier Barais, and Yves Le Traon. **A Native Versioning Concept to Support Historized Models at Runtime**. In ASM/IEEE 17th International Conference on Model Driven Engineering Languages ​​and Systems (MODELS'14), 2014. [Get the paper here](http://orbilu.uni.lu/handle/10993/18688)
* Assaad Moawad, Thomas Hartmann, Francois Fouquet, Gregory Nain, Jacques Klein, and Johann Bourcier. **A Model-Driven Approach for Simpler, Safer, and Evolutive Multi-Objective Optimization Development**. In 3rd International Conference on Model-Driven Engineering and Software Development (MODELSWARD'15), 2015. [Get the paper here](http://orbilu.uni.lu/handle/10993/20392)
* Thomas Hartmdann, Assaad Moawad, Francois Fouquet, Gregory Nain, Jacques Klein, and Yves Le Traon. **Stream my Models: Reactive Peer-to-Peer Distributed Models@run.time**. (will appear soon).
* Assaad Moawad, Thomas Hartmann, Francois Fouquet, Gregory Nain, Jacques Klein, and Yves Le Traon. **Beyond Discrete Modeling: Continuous and Efficient Models@Run.time for IoT**. (will appear soon).

**Application papers:**

* Thomas Hartmann, Francois Fouquet, Jacques Klein, Gregory Nain, and Yves Le Traon. **Reactive Security for Smart Grids Using Models@run.time-Based Simulation and Reasoning**. In Second Open EIT ICT Labs Workshop on Smart Grid Security (SmartGridSec'14), 2014. [Get the paper here](http://orbilu.uni.lu/handle/10993/16762)
* Thomas Hartmann, Francois Fouquet, Jacques Klein, and Yves Le Traon, Alexander Pelov, Laurent Toutain, and Tanguy Ropitault. **Generating Realistic Smart Grid Communication Topologies Based on Real-Data**. In IEEE 5th International Conference on Smart Grid Communications (SmartGridComm'14), 2014. [Get the paper here](http://orbilu.uni.lu/handle/10993/19009) 	
* Thomas Hartmann, Assaad Moawad, Francois Fouquet, Yves Reckinger, Tejeddine Mouelhi, Jacques Klein, and Yves Le Traon. **Suspicious Electric Consumption Detection Based on Multi-Profiling Using Live Machine Learning**. (will appear soon). 

Getting started
==============

Requirements
-------------
* [NodeJS](https://nodejs.org)

For impatient: the reflexive Quick and Dirty way
-------------



For long term project: custom api generation
-------------
TODO






Annotations
==============

TODO


Traverse and Query data graph
==============

From a graph object (aka KObject), a classical operation consist in traversing the graph to collect related information. 
For instance from a node, a developer can reach subNodes and subSubNodes and so on. KMF offers two consistent and optimized APIs to perform such operations: 
- KTraversal: typed, using the Java or JavaScript with a fluent API to declare the collecting behaviour. 
- Queries: untyped, using plain String an a pipe mechanism similar to UNIX pipe to declare collection and filter

Un a nutshell, Queries are textual version of the KTraversal API


TODO



Avoid callback hell using KDefer
================================

All KMF methods are build around the common asynchronous principle. This means that all results will be given in a KCallback closure (or anonymous function in javascript). Chaining severals operations or collecting severals KObject trough different method calls can leads to a huge nested hierarchy of callback. To avoid this callback hell effect KMF introduces an API named KDefer. In a nutshell, KDefer are barrier object than can be created on demand from the model and able to wait and collector several asynchronous results. The API is death simple, you create a KDefer object and then from it you can create various callbacks that you can inject in various query methods for instance.

The following code snippet illustrates the use of this API in Java code:

```java
KDefer myDefer = myModel.defer();
model.select("nodes[n=n1]", myDefer.waitResult());
model.select("nodes[n=n2]", myDefer.waitResult());
```	 

and similarly in JavaScript:

```js
var myDefer = myModel.defer();
model.select("nodes[name=N1]", myDefer.waitResult());
model.select("nodes[name=N2]", myDefer.waitResult());
```	

Once all necessary callbacks have injected, you can end the KDefer by giving the final KCallback that will be called once all results are ready:

Here is the example in Java:

```java
myDefer.then(new KCallback<KObject[]>{
	void on(KObject[] objects){
		objects[0] //contains objects with name N0
		objects[1] //contains objects with name N1
	};
});
```	 

And similarly here is the example in JavaScript:

```js
myDefer.then(function(objects){
	objects[0] //contains objects with name N0
	objects[1] //contains objects with name N1
});
```	





Related work
============

Facebook relay
--------------

https://facebook.github.io/relay/

Netflix Falcor
--------------

http://netflix.github.io/falcor/

WithEve
-------

http://witheve.com/

The Kevoree Modeling Framework
==========================

The Kevoree Modeling Framework (KMF) started as a research project to create an alternative to the [Eclipse Modeling Framework](https://eclipse.org/modeling/emf/) (EMF).
Like EMF, KMF is a modeling framework and code generation facility for building complex object-oriented applications based on structured data models.
However, while EMF was primarily designed to support design-time models, KMF is specifically designed to support the models@run.time paradigm and targets runtime models.
Runtime models of complex systems usually have high requirements regarding memory usage, runtime performance, and thread safety.
KMF was specifically designed with this requirements in mind. 
 
KMF provides developers a powerful toolset to model, structure, and reason about complex data (during design- and runtime), while still being a 'lightweight' framework trying to introduce as less overhead as possible.
Advanced features like a notion of time, a native per object based versioning concept, distribution support, and easy-to-plug machine learning algorithms make KMF a powerful toolset for structuring, processing, and analyzing data.
A main focus of KMF is on performance and scalability, which are often neglected to a great extend by modeling frameworks.    


Features and Design Principles
-------------
KMF was specifically designed with memory usage, runtime performance, and thread safety requirements in mind.
TODO: async, lazy load etc, end goal data overlay


Publications:
-------------
Within the scope of the KMF project several research papers have been published: 

* Francois Fouquet, Gregory Nain, Brice Morin, Erwan Daubert, Olivier Barais Christmas Plouzeau, and Jean-Marc Jézéquel. **An Eclipse Modelling Framework Alternative to Meet the Models@Runtime Requirements**. In ASM/IEEE 15th Model Driven Engineering Languages ​​and Systems (MODELS'12), 2012. 
* Thomas Hartmann, Francois Fouquet, Gregory Nain, Jacques Klein, Brice Morin, and Yves Le Traon. **Reasoning at runtime using time-distorted contexts: Amodels@run.time based approach**. In 26th International Conference on Software Engineering and Knowledge Engineering (SEKE'14), 2014.
* Thomas Hartmann, Francois Fouquet, Gregory Nain, Brice Morin, Jacques Klein, Olivier Barais, and Yves Le Traon. **A Native Versioning Concept to Support Historized Models at Runtime**. In ASM/IEEE 17th International Conference on Model Driven Engineering Languages ​​and Systems (MODELS'14), 2014.
* Thomas Hartmann, Assaad Moawad, Francois Fouquet, Gregory Nain, Jacques Klein, and Yves Le Traon. **Stream my Models: Reactive Peer-to-Peer Distributed Models@run.time**. (will appear soon).
* Assaad Moawad, Thomas Hartmann, Francois Fouquet, Gregory Nain, Jacques Klein, and Yves Le Traon. **Beyond Discrete Modeling: Continuous and Efficient Models@Run.time for IoT**. (will appear soon).

Getting started
==============

For impatient: the reflexive Quick and Dirty way
-------------



For long term project: custom api generation
-------------


TODO




Traverse and Query data graph
==============




Avoid callback hell using KDefer
=============

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

Here is the exemple in Java:

```java
myDefer.then(new KCallback<KObject[]>{
	void on(KObject[] objects){
		objects[0] //contains objects with name N0
		objects[1] //contains objects with name N1
	};
});
```	 

Similarly here is the JavaScript illustration:

```js
myDefer.then(function(objects){
	objects[0] //contains objects with name N0
	objects[1] //contains objects with name N1
});
```	



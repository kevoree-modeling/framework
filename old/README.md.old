#WARNING, THIS DOCUMENTATION IS NOT UPDATED WITH THE KMF version 4, new documentation aligned with the new asyncronous semantic coming soon... 

Kevoree Modeling
=======

The increasing use of Model@Runtime calls for rich and efficient modeling frameworks.
The Kevoree Modeling Framework(KMF) is developed to provide efficient domain-specific modeling frameworks to author, compose and synchronize models on Java and JavaScript Virtual Machines.

> Free the code from models !

### Why a new framework?

KMF has originally been developped to support the Kevoree Model@Run.time platform.
After years of development for this platform, we have now acquired a strong expertise on tools needed for authoring, ditribution and synchronization of models. KMF is a generalisation of this expertise in an open and generic tool. From any domain-specific meta-model, KMF creates a specific modeling environmnent natively supplied with modeling operators compiled for JVM and JS and tuned for an efficient use at Runtime. It can be used simply host the configuration of a Software, rationalize and store data or help in the management of complex distributed Software systems. To this end, it offers the same powerful API in plain Java, and JavaScript, to ease the development of, for instance, a server-side storage in Java and its  presentation layer in a simple browser. In short, KMF generates business specific API and Tools from a metamodel, ready for distributed modeling activities.

> KMF helps you creating modeling frameworks dedicated for runtime usages

### Features

KMF supplies runtime-oriented features such as:
```
most important feature, a very simple and comprehensive API
```

 * Memory optimized object oriented modeling API
 * JS (Browser, NodeJS) and JVM cross-compiled models
 * Efficient visitors for models traversal
 * Unique path for each model element
 * Optimized query language to lookup model elements
 * Trace operators to atomize model operations into low-level primitive sequences 
 * Built-in load/save operation in JSON/XMI format
 * Built-in clone strategies (mutable elements only, copy on write)
 * Built-in merge and diff operators
 * Persistence layer for BigModels with lazy load policy
 * Distributed datastore for BigData models
 
Getting started
---------------

The Apache Maven dependencies and plugin management system is probably the easiest way to start using KMF.
You can complete one of your existing project with the configuration that comes next, or simply follow the kick-off sample project provided here.

> NB : standalone compiler and NetBeans/Eclipse/IntelliJ plugins are coming soon !

### Using your own maven project

Create a Maven project folder in which you will place the pom and your EcoreMM.

```
myProject
 |-myMetaModel.ecore
 |-pom.xml
```

Then the KMF plugin in the Build/Plugins section

``` html
<plugin>
 <groupId>org.kevoree.modeling</groupId>
 <artifactId>org.kevoree.modeling.kotlin.generator.mavenplugin</artifactId>
 <version>replace.by.last.kmf.version</version>
 <extensions>true</extensions>
 <executions><execution>
   <id>ModelGen</id>
   <goals><goal>generate</goal></goals>
   <configuration>
   <!-- optionS here -->
   </configuration>
  </execution></executions>
</plugin>
```

In addition, add the KMF MicroFramework dependency
``` html
<dependency>
 <groupId>org.kevoree.modeling</groupId>
 <artifactId>org.kevoree.modeling.microframework</artifactId>
 <version>replace.by.last.kmf.version</version>
</dependency>
```

### Using a sample project

If you don't have an existing project you can download one by clicking on the button.

> [Download sample project >](https://github.com/kevoree/kmf-samples/tree/master/tinycloud)

### Compile models

When your Maven project is ready, just open a terminak, cd into the folder containing the pom file and type in the following command.

```
mvn clean install
```

After compilation, the target folder contains a JAR or a JS file of the modeling API. The next step is to include the .js file into a web page (or NodeJS) or add the project as a dependency of your application to start Modeling@Runtime using the concepts you defined in the MetaModel.



Query language
==============

The Eclipse Modeling Framework(EMF) has been developed for design time manipulations of models and provides tools for this purpose, though not developed to be light, embeddable and effective at **run time**. 
The [Kevoree Modeling Framework](https://www.google.lu/url?sa=t&rct=j&q=&esrc=s&source=web&cd=4&cad=rja&ved=0CFcQFjAD&url=http%3A%2F%2Fhal.archives-ouvertes.fr%2Fdocs%2F00%2F71%2F45%2F58%2FPDF%2Femfatruntime.pdf&ei=s8AYUfPlIZCDhQfx54DoCw&usg=AFQjCNFlfrm1NFVs6iIddxVjorbJeOajWA&sig2=nUrWedVJnv8ndOQViy2ZtA&bvm=bv.42080656,d.ZG4), or KMF, is developed specifically to address these drawbacks and provides a drop-in replacement of the EMF *generator* (i.e.: model to code generator). Indeed, models are structured data and must offer efficient solutions for their exploration, loading, saving and cloning.

KMF takes advantage of its generation abilities to now propose two new tools to efficiently select and/or reach any model element.

* The [**Path Selector(KMFQL-PS)**](#pathSelector) gives the ability to efficiently reach a specific element in the model, as soon as the model element has an *ID*.
* The [**Query Selector(KMFQL-QS)**](#querySelector) offers a simple language to collect, in depth, all the elements from the model that satisfy a query


Path Selector
-------------

Collecting model elements with a given property or simply accessing attributes of elements is one of the most common operation in modeling. But looping on relations in order to find a specific model element has serious drawbacks on performance and code complexity. Moreover the Java code generated usually offers no insurance on the uniqueness defined by the `id` attribute in the metamodel.

The development of the **Path Selector (PS)** is motivated by the necessity to have a efficient tool to reach a specific element in the model, identified by a unique id, as it is done in relational databases. The Path Selector uses the *id* attribute expressed in metamodel as the unique key to find a model element by following model relationships. Let see how it works. For the sake of clarity, we illustrate the use of Path Selectors(PS) with an example extracted from [Kevoree](http://www.kevoree.org). Let consider a very simple excerpt of the Kevoree metamodel.

> ![Mini Kevoree Model](https://raw.github.com/dukeboard/kevoree-modeling-framework/master/doc/fig/minikev.png)

**NamedElement** has an attribute `name`. This attribute is marked as the `ID` of NamedElement's elements (i.e.: the property `id` of this attribute is set to `true`).<br/>
**ComponentModelRoot** contains several nodes.<br/>
**Node** are NamedElements, thus has a name attribute, can contain other nodes and host components.<br/>
**Component** are also NamedElements.

Now, imagine that you want to get the `logger`component that you know to be hosted on node `42`. Doing so using the KMF API looks like:

``` java
ComponentModelRoot root = mySystem.getRoot();
Component foundComponent = null;
for(Node loopingNode : root.getNodes()){
	if(loopingNode.getName().equals("42")){
		for(Component loopingComponent : loopingNode.getComponents()){
			if(loopingComponent.equals("logger")){
				foundComponent = loopingComponent.equals;
			}
    	}
	}	
}
```

Using the KMFQL-PS the same research looks like:

``` java
ComponentModelRoot root = mySystem.getRoot();
Component foundComponent = root.findByPath("nodes[42]/components[logger]");
```	

### Path construction

The KMFQL-PS syntax follows the metamodel elements' relationships. Each relation can be navigated using the following syntax.

> relationName[ID]

* RelationName : Name of the relation to navigate (ex : nodes)
* ID : Value of the `id` to identify one specific element in the collection
 
`ID`s can contain a `/` character (such as a sub-paths). In this case, the ID is considered as a [chained path](#pathSelector/chainedPaths). To protect against this behavior, the entire `ID` must be protected by braces (e.g.: `{myHome/room1}`).

Also, in our example the following paths are equivalent

> nodes[42] <=> nodes[{42}]
	
In some particular cases, a model element can contain one and only one relation  to another element. In this particular case, it can be convenient to omit the specification of the relation to navigate. In our example, ComponentModelRoot has only one containment relation (nodes), which enables the path to a specific node: 

```java
Component foundedComponent = root.findByPath("42");
```

Paths to elements can be chained. Each path to a specific element is delimited by a `/` char.
`relationName[ID]/ID/relationName[{ID/with/slash}]`
The paths are evaluated in ordered sequence, from left to right. Thus `ID` in the previous query is applied on the result of the evaluation of `relationName[ID]`

In our example, the retrieval of the logger of the node 420, hosted on node 42 can be expressed as follow :

> nodes[42]/nodes[420]/components[logger]
	
### Path API

The API for PS is automatically included in generated classes for each model element that declares an `ID` attribute.
In this case, two methods are automatically generated : find**relationshipName**ByID and findByPath method.

``` java
Node findNodeByID(String nodeID); 	
Object findByPath(String query); 
```

The starting point for the resolution of a query (Chained Path) is the element on which the method is called. Thus, if you want to retrieve the node 420 from a ComponentModelRool element the chained path is `nodes[42]/nodes[420]`. But if you look for this same element from node42, the query path is reduced like `node[420]`. The resolution process of KMFQL is recursive.

Each element identified by an `ID` also embbed a generated method to produce the unique path to find them. The method path returns a path that follows the containement hierarchie.

```java
String path();
```	

Query Selector
--------------

When working with models, the selection of elements among a collection is one of the most common operation. This selection is often made of a filter on the values of some elements' attributes. This filtered sub-collection can then be applied another filter going deeper in the containment relation and/or a treatment can be applied.

The KMFQL-QS has been created to enable the efficient, deep filtering of model elements. Just as for the Path Selector, queries can be composed of chained filters. The filtering is performed a prefix recursion basis. The first filter returning no element ends the execution of the query and returns null. Otherwise, the query returns the last set of elements that passed the last filter.

The difference with an SQL query for instance, is that the first part of the query is executed, then the second part is executed on each element the sub-set issues from the first query. The results of the second query are aggregated and used as input for the third query; and the algorithm goes until there is no more query part to apply.

KMFQL-QS relies on [JFilter](https://github.com/rouvoy/jfilter) for the definition of filters and execution of the query.

### Selector syntax

The syntax is based on the same construction as KMFQL-PS. The filterExp format is documented on [JFilter](https://github.com/rouvoy/jfilter).

	relationName[{filterExp}]/relationName[{filterExp}]

The relation name is optional. In this case,  the query is executed on **ALL** relations of the element on which it is called.<br/>

	{filterExp}/relationName[{filterExp}]
	
> **BE AWARE** that this behavior can lead to a combinatorial explosion if not used with caution.

### Selector API

As for KMFQL-PS, selector methods are generated directly in the classes of the model elements, if the `selector` option is set to true in the KMF maven plugin.

> selectByQuery(String query);
	
If we take the previous example of tiny component model previously described, selected every node which name began 42 can be expressed as :

> nodes[{ name = 42* }]
	
Path and selector can be mixed together, then selecting every nodes containing a component with ID is expressed as follow :

> nodes[{ name = 42* }] / components[logger]

Another example is the selection of every child (hosted by another node) which has **also** a number of component >10

> nodes[{ name = * }] / nodes[{ components.size > 10 }]

And finally in the same manner the following expression select every master which host more than 3 sub nodes and which name began by Center1_ :

> nodes[{ &(nodes.size > 3)(name = Center1_* ) }]
	
Finally with the syntactic sugar and _or_ operator selecting nodes of Center1 or Center2 can be expressed by the following expression :

> { |(name = Center1_*)(name = Center2_* ) }
> 

### Special keyword for contained elements

In addition to standard relationship names available in the selector language, KMFQL offers a dedicated keyword to perform selections on any element contained by another.

In details, if an element is container for some other, it is possible to select all his child(contained) elements, whatever their containment collection, by using the following expression :

> contained[*]
	
So as with classical relation, it is possible to select a subset of the contained elements (e.g.: any contained element which name starts with `subNode`):

> contained[name = subNode*]
	

Model Events
============

What for?
---------

### Events motivation

Events are handy to be informed about changes in models each time they occur. This mechanism can be used to synchronize models or to synchronize a view with it's backbone model. Basically an event listener can be registerd on a specific model element or recursively on all sub-contained elements *(tree listener)*.

### How to uses it ?

Events are optionaly included in the generated API. To activate events, simply set the `event` option of the KMF Maven pluggin to `true`. The generator includes all necessary methods and classes to provide a listener mechanism to all the features of your Metamodel.

``` xml
   <events>true</events>
```

API description
---------------

### ModelEvent

Each modification of a model now produces and propagates a ModelEvent. 
Events are generated when:

* Attributes are `set`
* References are `set`, `added` or `removed` (the two last are only available for references with unbound max cardinality)
* An opposite element is modified (only if the opposite relation is set).

The `source` of an event is the model element on which the modification applied, and is identified by its `path()`.

### Events listeners

A ModeListener is an object that receives ModelEvents. Once registered on a model element, the method `elementChanged` of the ModelListener is called for each modification of the model element.

```
trait ModelElementListener {
    fun elementChanged(evt : ModelEvent)
}
```

Model listeners can be registered on any model element. They can be registered as ModelElement listener or ModelTreeListener. The first one only receives events from the model element it registers on. The second receives any modification(reference or attribute modified) that occures on any sub-element in the containment hierarchy.

On the right, please find the extract of the KMFContainer API. Model listeners can be registered or unregistered using these methods on any model element. *removeModelTreeListener* method recursively unregisters a listener on child elements.

```
trait KMFContainer {
    fun addModelElementListener(lst : ModelElementListener)
    fun removeModelElementListener(lst : ModelElementListener )
    fun removeAllModelElementListeners()
    fun addModelTreeListener(lst : ModelElementListener)
    fun removeModelTreeListener(lst : ModelElementListener)
    fun removeAllModelTreeListeners()
}
```

For instance, considering a very simple Finite State Machine metamodel <br /> 
(FSM<>--State<>--Transition<>--Action).

``` java
fsm.addModelTreeListener(new ModelTreeListener() {
            @Override
            public void elementChanged(ModelEvent evt) {
                System.out.println("FSM-Tree::" + evt.toString());
            }
        });
```

Would print a message each time something is set, added or removed in the entire FSM !
But, if you want to listen to events concerning the FSM only (States added or removed, don't care about Transitions and Actions), a ModelElementListener would be sufficient.

``` java
fsm.addModelElementListener(new ModelElementListener() {
            @Override
            public void elementChanged(ModelEvent evt) {
                System.out.println("FSM::" + evt.toString());
            }
        });
```


Model visitors
===============

Visitor motivation
------------------

A model is basically a graph of objects organized around relationship. Many model case study need to navigate through these relationships to perform operations. As an exemple a pretty print process needs to traverse the whole graph following the containement relationships.

### Efficient visit
The classical approach in model object oriented API is based on iterations on lists to perform this naviguation. This leads to serious performance drawbacks due to potentially huge number of temporary object created during this iterative process. For this reason KMF generates a built-in very efficient visitor pattern to go through all modelelements, relationships and attributes.

Visitor usage
-------------

### Visitors API

The KMF visitor API is based on the ModelVisitor interface. It is mainly based on a visit method called each time a visitor (custom process) finds a new element to visit.

``` java
package org.kevoree.modeling.api.util;
trait ModelVisitor {
	fun visit(elem : KMFContainer, refInParent : String, parent : KMFContainer)
}
```

In addition, a visitor can optionaly define additional methods to have feedback during the visit. The beginVisitElem method is called just before the visit of an element starts, respectively the endVisitElem method is called just after the visit of an element is completed (i.e.: when this object and all its child elements have been visited). Also, beginVisitRef and endVisitRef are called when a reference of an element is visited. The endVisitRef method is called when all elements of a reference have been visited.


``` java
fun beginVisitElem(elem : KMFContainer){}
fun endVisitElem(elem : KMFContainer){}
fun beginVisitRef(refName : String, refType : String){}
fun endVisitRef(refName : String){}
```

Despite its efficiency to traverse a model, the recursion of the visitor sometimes needs to be controled to optimize the visitor's performance. For this purpose, the ModelVisitor is decorated with a `stopVisit` method, to abort the visit, and a `noChildrenVisit` and `noReferencesVisit` to respectivelly avoid the visit of the contained elements and the visit of referenced model elements.

``` java
fun stopVisit()
fun noChildrenVisit()
fun noReferencesVisit()
```

To visit the attributes of a model element, KMF provides a dedicated model visitor named ModelAttributeVisitor. The `visit`method of this visitor is called for each attribute of a model elements, with the content (value) and the name of the attribute.

```
trait ModelAttributeVisitor {
	fun visit(value: Any?, name: String, parent: KMFContainer)
}
```

### How to use?

Any KMF model element as two built-in methods to start a visit. The `visit` method takes your custom visitor as first parameter. The second parameter allows to define if the visit is recursive; the third specifies if it should include the contained elements and the last parameter if not-contained references has to be visited.
Finally, the visitAttributes allows to trigger a visit of attributes on a model element.

``` java
trait KMFContainer {
 	fun visit(visitor : org.kevoree.modeling.api.util.ModelVisitor, 
		recursive : Boolean, containedReference : Boolean,
		nonContainedReference : Boolean)
	fun visitAttributes(visitor : ModelAttributeVisitor)
}
```

Model operators
===============

Trace concept
---------------

### What is a trace?

A trace is an atomic model modification/operation. It defines a change executed on a model element identified by a path. The atomicity of a trace makes it an ideal candidate to store an action realized or planned to be executed on a model. Traces are serializable thanks to the use of the KMF path to identify the model elements. A trace can be extracted from a model and applied on a remote mirror model, because the path contains the semantic of unique element identification. A trace has a type and here is the complete list of trace types.

```
TraceType {
	SET
	ADD
	ADDALL
	REMOVE
	REMOVEALL
	RENEW_INDEX
}
```

Need an example ? This is a trace to set the attribute `param` of the `node0` element of the `nodes` relation to `newVal`. This trace is also generated when the attribute is set.

> {type:"SET",path:"nodes[node0]/param",content:"newVal"}

### Trace sequence

A trace sequence is basically an ordered list of atomic traces. A trace sequence can be applied on a model to sequencially perform a model transformation, step-by-step replay or a synchronization. A trace sequence can be view a patch.

Set operations on models
-----------------------

### ModelCompare

To perform all KMF model operation, we need to generated a trace sequence through a model compare. This trace sequence can be then apply on a model to perform the real operation. 

```
ModelCompare compare = new DefaultModelCompare();
```

### Model union

A model merge operation aims at merge all models elements present in two models in one model. 

> A + TraceMergeOf(A,B) = A + B

As all KMF model operation it relie on the comparator to generate the trace sequence corresding to such operation. Then the trace sequence can be apply on A, B is unchanged.

``` java
diffSeq = compare.merge(modelA,modelB);
diffSeq.applyOn(modelA)
```

### Model intersection

A model intersection operation aims at building a model containing all common models elements present in two models. 

> A + TraceMergeOf(A,B) = (A+B) - A - B 

As all KMF model operation it relie on the comparator to generate the trace sequence corresding to such operation.

``` java
diffSeq = compare.inter(modelA,modelB);
diffSeq.applyOn(modelA)
```

### Model patch

A model compare operation aims at migrate a model to another. In short a model A should reach the state of a model B this is the right operation. This operation is mainly used in synchronization process. 

> A + TraceMergeOf(A,B) = B

As all KMF model operation it relie on the comparator to generate the trace sequence corresding to such operation.

``` java
diffSeq = compare.inter(modelA,modelB);
diffSeq.applyOn(modelA)
```

Model tracker
-------------

### Undo/Redo for models

KMF framework offers a ModelTracker utility. This tool tracks all changes(events) that occur on a model and stores these changes in a trace sequence. In addition, the tracker maintains a reversed trace sequence that allows to reverse the modifications.

Once create, a Model tracker is activated by calling the **track** method with the element you wanna track in parameter. To stop the tracking, a simple call to **untrack** method is required. The **reset** method allows to define a break point in the tracker. User can then perform any modifications on models. A call of the **undo** method applies the reversed trace sequence, which reverts all modifications. After that, a call to **redo** method applies again the modifications.


``` java
class ModelTracker {
	fun track(model: KMFContainer)
	fun untrack()
	fun reset()
	fun undo()
	fun redo()
}
```

Model Aspects
=============

Modeling through code
----------

The MOF structure of your models like described in .ecore files only reflects the structural way to store models. Though Ecore allows you to define operations into models, the core of these operations can not be properly described directly in the model, because it depends on the target language of the generation.

KMF relies on the Kotlin language to express the core of operations. Kotlin language can be cross-compiled to JS and JVM, making the method core seemlessly working in Java and JavaScript (for browser or nodejs platforms).
In KMF, the synchronization between code and model (ecore files) is partially bi-directional. The skeletons of methods are generated from the operations declared in the model, which eases the completion of the behavioral code of operations. In the other way, any method added next to a generated method and any new class declared in the code, is automatically added in the meta-model.

Use code or model first as needed in your project, but please remind that models are made to abstract and define the domain concepts; technical details should be hidden in code.

Kotlin API
----------

### Aspect API

Let's take as example a simple FSM metamodel (you can find it [here](https://github.com/kevoree/kmf-samples/tree/master/fsm/org.kevoree.modeling.sample.fsm.kt) ). In this metamodel we add an operation `run` to the metaclass `Action`. Then, you can declare in your src/main/java directory several Kotlin traits implementing the generated interface. An example can be found [here](https://github.com/kevoree/kmf-samples/blob/master/fsm/org.kevoree.modeling.sample.fsm.kt/src/main/java/org/jetbrains/annotations/MyAspect.kt).

``` kotlin
aspect trait MyAspect : Action {
	override fun run(p : Boolean): String {return "";}
	private fun internalStuff(){}
    }
```

After a compilation of your project, the resulting Java .class or JavaScript .js files will have all your traits directly woven in the compiled code. The `Aspect` keyword is an annotation meaning that the aspect must be mixed with the meta class : Action. If you define a new operation by adding a **fun** definition, it is directly added in the MetaModel as a new operation (be careful the API will change and you will have to add the override keyword). Private function will remain private to the aspect and will not be pushed in the ecore model. In short, you can now call the method run on any of your Action object, in the JVM or in the JS version.

### Metaclass API

You can also add a new pure code meta class using the same mechanism, just by using the **meta** keyword. This means that you add a new metaclass named **MyMetaClassName** with superclass **Action**. This meta class will be generated in the API as any method class defined in the based ecore file.


``` kotlin
    public meta trait MyMetaClassName : Action {
        override fun myFct(): {return "";}
    }
```

Using JS models
===============

in NodeJS
---------

The KMF maven plugin generates into the target directory a file named : <artefactID>.min.js.
This file is ready to be included as a nodeJS module. To load it, you only have to call the include directive of nodeJS. Of course replace `org.cloud` by the generated package specified in your model.

``` js
var model = require('./org.kevoree.modeling.sample.cloud.js.min.js');
var saver = new model.org.cloud.serializer.JSONModelSerializer();
var loader = new model.org.cloud.loader.JSONModelLoader();
```

in browsers
-----------

TODO

BigModel
===================

What is BigModel ?
------------------

Models can reach a huge size, especially when using it to store the monitored history of a system or while modeling a domain of use such as all the possible topologies of a cluster of machines. In such case study, models can not fit in memory anymore and we need a better way to interact with informations. Similarly to BigData we speak of BigModel in such cases.

### Concept overview

KMF Persistence API is based on the following asumption: despite a model does not fit in memory, or is stored on a remote server, its authoring must be exactly identical to the in memory authoring. The main concept behind the KMF persistence layer is the lazy load. In short, the model seamlessly loads model elements on demand, when required by the navigation in the model. If a model element is created in memory or already loaded, a cache will optimizes the load. To reduce the space in memory, an element is loaded only if its path is looked for. Moreover, the attributes and referenced elements are loaded only if a get or a set is performed on one of them. This concept allows to go directly in deep in the graph object without any extra-cost like a select in a database.

### PersistenceFactory

The main entry point for the persistence KMF API is a special factory : PersistenceKMFFactory. This factory allows to interact with the BigModel through the lookup of elements using their path, and the save of elements using the persist method. A simple batch concept allows to save several elements shortly. All modifications are actually written to the remote storage or disk only when a call on **commit** method is performed. Finally the **clearCache** method allows to close the factory and free the memory.

``` java
trait PersistenceKMFFactory {
	fun lookup(path: String): KMFContainer?
	fun persist(elem: KMFContainer)
	fun persistBatch(batch: Batch)
	fun createBatch()
	fun commit()
	fun clearCache()
}
```

### Batch persit

A batch contains model elements. Elements can be added one by one through the **addElement** operation or all reachable elements can be added through **addElementAndReachable**. The batch define a fulent API, so call can be chained like b.addElement(a).addElement(b);


```java
trait Batch {
	fun addElement(e: KMFContainer): Batch
	fun addElementAndReachable(e: KMFContainer): Batch
}
```

Available DataStores
--------------------

### DataStore API

To perform the actual storage, KMF relies on a very simple DataStore API. Basically it defines an interface with get, put and remove operations. The KMF framework already offers several DataStorage solutions.

* MemoryDataStore
* Redis (in progress)
* MapDB (in progress)
* LevelDB
* HTML5 storage (browser and nodejs only)

### Client/Server deployment

TODO

### Master/Master deployment

TODO

###Distributed high-performance deployment

TODO

### Performances

TODO

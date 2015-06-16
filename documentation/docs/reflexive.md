# Reflexive (pure dynamic) usage of KMF

The *reflexive* package in KMF allows to manipulate models in a pure dynamic way (***i.e.***, without any code generation step).
By the way, such usages are not recommended because of the untyped API and because of the risk to loose data in case of base configuration of the dynamic metaModel.
Indeed, data are store with the current metaModel in mind, and by the way if a user wants to reuse the data from a dataStore their is a risk of inconsistency.

Beyond this warning the reflexive usage of KMF is the sample solution for a working hello world. Additionally the presented API and dynamic reflexive usage is available for both Java and JS world.

To start, man have to create a **DynamicMetaModel** object.

```java
DynamicMetaModel dynamicMetaModel = new DynamicMetaModel("MyMetaModel");
```

Then we can create dynamically metaClasses, for instance by given the name **Sensor**.

```java
DynamicMetaClass sensorMetaClass = dynamicMetaModel.createMetaClass("Sensor");
```

When this is done, we can add some attributes and references to this metaClass. The only parameters are the type and potentially the **contained** or not property for reference.

```java
sensorMetaClass
	.addAttribute("name", PrimitiveMetaTypes.STRING)
	.addAttribute("value", PrimitiveMetaTypes.FLOAT)
	.addReference("siblings", sensorMetaClass, false);
```

Then lets two the same with a **Home** MetaClass

```java
DynamicMetaClass homeMetaClass = dynamicMetaModel.createMetaClass("Home");
homeMetaClass
	.addAttribute("name", PrimitiveMetaTypes.STRING)
	.addReference("sensors", sensorMetaClass, true);
```
Once the configuration of the reflexive usage is done, man can create a **Universe** associated to this metaModel.

```java
KUniverse universe = dynamicMetaModel.universe();
```

Then we can create dynamically a KObject according to the metaClassName

```java
KObject home = universe.dimension(0).time(0).create(universe.metaModel().metaClass("Home"));
```

Users should then use the reflexive API to set and mutate object such as the reflexive set below to set the name attribute of the **Home** object.

```java
home.set(home.metaClass().metaAttribute("name"),"MainHome");
```

We can follow the same procedure to create a dynamic **Sensor** for instance by using the pointer to the previously created DynamicMetaClass.

```java
KObject sensor = universe.dimension(0).time(0).create(sensorMetaClass);
          sensor.set(sensor.metaClass().metaAttribute("name"),"Sensor#1");
```

Additionally, we leverage the mutation reflexive API to add the **sensor** into the **Home** object under the **sensors** relationship.

```java
home.mutate(KActionType.ADD,home.metaClass().metaReference("sensors"),sensor);
```

Finally, users can use all the classical API of KMF, time navigation, persistence and so on, reflexive API create exactly the same kind of objects than the generator.

```java
universe.dimension(0).time(0).json().save(home, new ThrowableCallback<String>() {
	@Override
	public void on(String s, Throwable error) {
		System.out.println(s);
	}
});
```
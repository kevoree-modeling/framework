### Chronicle database

The package offer a chronicle based (https://github.com/OpenHFT) implementation of Kevoree DataBase.
The implementation is localized into the ChronicleDataBase class:

```java
org.kevoree.modeling.databases.chronicle.ChronicleDataBase
```

```java
universe.setDataBase(new ChronicleDataBase(param));
```

The implementation offer two main structure:

- Pure Off-heap structure

The pure off-heap memory allows to fill the system memory til the physical limit.

```java
universe.setDataBase(new ChronicleDataBase(null));
```

- Disk mapped structure

The pure disk mapped structure allows to fill the system memory til the physical limit and then unload last used part on disk.
It also offer a stateful storage through the disk commit method.

```java
universe.setDataBase(new ChronicleDataBase("/myStorage"));
```

It is available on maven central as

```xml
<dependency>
  <groupId>org.kevoree.modeling</groupId>
  <artifactId>org.kevoree.modeling.microframework.databases.chronicle</artifactId>
  <version><!--replace with the latest version--></version>
</dependency>
```

Click here to get the [Latest Version Number](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.kevoree.modeling%22%20AND%20a%3A%22org.kevoree.modeling.microframework.databases.chronicle%22)


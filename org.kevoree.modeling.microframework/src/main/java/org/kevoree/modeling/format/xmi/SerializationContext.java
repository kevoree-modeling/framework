package org.kevoree.modeling.format.xmi;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.traversal.visitor.KModelAttributeVisitor;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap;
import org.kevoree.modeling.memory.struct.map.impl.ArrayStringMap;

import java.util.ArrayList;

public class SerializationContext {
    public boolean ignoreGeneratedID = false;
    public KObject model;
    public KCallback<String> finishCallback;
    public StringBuilder printer;
    public KModelAttributeVisitor attributesVisitor;

    // KPath -> XMIPath
    ArrayLongMap<String> addressTable = new ArrayLongMap<String>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    // KPath -> Count
    ArrayStringMap<Integer> elementsCount = new ArrayStringMap<Integer>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);

    ArrayList<String> packageList = new ArrayList<String>();

}
package org.kevoree.modeling.cloudmodel;

import org.kevoree.modeling.KObject;

/**
 * Created by duke on 10/9/14.
 */
public interface Element extends KObject {

    String getName();

    Element setName(String name);

    Double getValue();

    Element setValue(Double name);

}

package org.kevoree.modeling.cloudmodel;

import org.kevoree.modeling.KView;

/**
 * Created by duke on 10/9/14.
 */
public interface CloudView extends KView {

    Node createNode();

    Element createElement();

}

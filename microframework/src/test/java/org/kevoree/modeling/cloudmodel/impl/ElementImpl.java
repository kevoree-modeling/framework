package org.kevoree.modeling.cloudmodel.impl;

import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.cloudmodel.Element;
import org.kevoree.modeling.cloudmodel.meta.MetaElement;

public class ElementImpl extends AbstractKObject implements Element {

    public ElementImpl(long p_universe, long p_time, long p_uuid, KMetaClass p_metaClass, KInternalDataManager p_manager, long previousUniverse, long previousTime, long currentUniverseMagic, long currentTimeMagic) {
        super(p_universe, p_time, p_uuid, p_metaClass, p_manager, previousUniverse, previousTime, currentUniverseMagic, currentTimeMagic);
    }

    @Override
    public String getName() {
        return (String) this.get(MetaElement.ATT_NAME);
    }

    @Override
    public Element setName(String p_name) {
        this.set(MetaElement.ATT_NAME, p_name);
        return this;
    }

    @Override
    public Double getValue() {
        return (Double) this.get(MetaElement.ATT_VALUE);
    }

    @Override
    public Element setValue(Double p_name) {
        this.set(MetaElement.ATT_VALUE, p_name);
        return this;
    }

}

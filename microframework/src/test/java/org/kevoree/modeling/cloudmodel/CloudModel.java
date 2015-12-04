package org.kevoree.modeling.cloudmodel;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.KMetaEnum;
import org.kevoree.modeling.meta.impl.*;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.cloudmodel.impl.ElementImpl;
import org.kevoree.modeling.cloudmodel.impl.NodeImpl;
import org.kevoree.modeling.cloudmodel.meta.MetaElement;
import org.kevoree.modeling.cloudmodel.meta.MetaNode;

public class CloudModel extends AbstractKModel<CloudUniverse> {

    private MetaModel _metaModel;

    public CloudModel(KInternalDataManager dataManager) {
        super(dataManager);
        _metaModel = new MetaModel("Cloud");
        KMetaClass[] tempMetaClasses = new KMetaClass[2];
        tempMetaClasses[0] = MetaNode.getInstance();
        tempMetaClasses[1] = MetaElement.getInstance();
        _metaModel.init(tempMetaClasses, new KMetaEnum[0]);
    }

    @Override
    protected CloudUniverse internalCreateUniverse(long universe) {
        return new CloudUniverse(universe, _manager);
    }

    @Override
    protected KObject internalCreateObject(long universe, long time, long uuid, KMetaClass clazz, long previousUniverse, long previousTime) {
        if (clazz == null) {
            return null;
        }
        switch (clazz.index()) {
            case 0:
                return new NodeImpl(universe, time, uuid, clazz, _manager, previousUniverse, previousTime);
            case 1:
                return new ElementImpl(universe, time, uuid, clazz, _manager, previousUniverse, previousTime);
            default:
                if (clazz.index() == MetaClassIndex.INSTANCE.index()) {
                    return new GenericObjectIndex(universe, time, uuid, _manager, previousUniverse, previousTime);
                } else if (clazz.inferAlg() != null) {
                    return new GenericObjectInfer(universe, time, uuid, clazz, _manager, previousUniverse, previousTime);
                } else {
                    return new GenericObject(universe, time, uuid, clazz, _manager, previousUniverse, previousTime);
                }
        }
    }

    @Override
    public KMetaModel metaModel() {
        return _metaModel;
    }

}

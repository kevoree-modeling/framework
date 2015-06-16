package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.format.KModelFormat;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KView;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.format.json.JsonFormat;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.traversal.impl.selector.Selector;
import org.kevoree.modeling.util.Checker;
import org.kevoree.modeling.format.xmi.XmiFormat;

public abstract class AbstractKView implements KView {

    final protected long _time;

    final protected long _universe;

    final protected KMemoryManager _manager;

    protected AbstractKView(long p_universe, long _time, KMemoryManager p_manager) {
        this._universe = p_universe;
        this._time = _time;
        this._manager = p_manager;
    }

    @Override
    public long now() {
        return _time;
    }

    @Override
    public long universe() {
        return _universe;
    }

    @Override
    public void setRoot(KObject elem, KCallback cb) {
        _manager.setRoot(elem, cb);
    }

    @Override
    public void getRoot(KCallback cb) {
        _manager.getRoot(_universe, _time, cb);
    }

    @Override
    public void select(final String query, KCallback<KObject[]> cb) {
        if (Checker.isDefined(cb)) {
            if (query == null || query.length() == 0) {
                cb.on(new KObject[0]);
            } else {
                _manager.getRoot(_universe, _time, new KCallback<KObject>() {
                    @Override
                    public void on(KObject rootObj) {
                        if (rootObj == null) {
                            cb.on(new KObject[0]);
                        } else {
                            String cleanedQuery = query;
                            if (query.length() == 1 && query.charAt(0) == '/') {
                                KObject[] param = new KObject[1];
                                param[0] = rootObj;
                                cb.on(param);
                            } else {
                                if (cleanedQuery.charAt(0) == '/') {
                                    cleanedQuery = cleanedQuery.substring(1);
                                }
                                Selector.select(rootObj, cleanedQuery, cb);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void lookup(long kid, KCallback<KObject> cb) {
        _manager.lookup(_universe, _time, kid, cb);
    }

    @Override
    public void lookupAll(long[] keys, KCallback<KObject[]> cb) {
        _manager.lookupAllobjects(_universe, _time, keys, cb);
    }

    @Override
    public KObject create(KMetaClass clazz) {
        return _manager.model().create(clazz, _universe, _time);
    }

    @Override
    public KObject createByName(String metaClassName) {
        return create(_manager.model().metaModel().metaClassByName(metaClassName));
    }

    @Override
    public KModelFormat json() {
        return new JsonFormat(_universe, _time, _manager);
    }

    public KModelFormat xmi() {
        return new XmiFormat(_universe, _time, _manager);
    }

    @Override
    public boolean equals(Object obj) {
        if (!Checker.isDefined(obj)) {
            return false;
        }
        if (!(obj instanceof AbstractKView)) {
            return false;
        } else {
            AbstractKView casted = (AbstractKView) obj;
            return casted._time == _time && casted._universe == _universe;
        }
    }

}

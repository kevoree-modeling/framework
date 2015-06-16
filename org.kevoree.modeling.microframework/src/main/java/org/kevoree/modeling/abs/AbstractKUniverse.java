package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.event.KEventMultiListener;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KUniverse;
import org.kevoree.modeling.KView;
import org.kevoree.modeling.memory.manager.KMemoryManager;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractKUniverse<A extends KView, B extends KUniverse, C extends KModel> implements KUniverse<A, B, C> {

    final protected long _universe;

    final protected KMemoryManager _manager;

    protected AbstractKUniverse(long p_key, KMemoryManager p_manager) {
        this._universe = p_key;
        this._manager = p_manager;
    }

    @Override
    public long key() {
        return _universe;
    }

    @Override
    public C model() {
        return (C) this._manager.model();
    }

    @Override
    public void delete(KCallback cb) {
        model().manager().delete(this, cb);
    }

    @Override
    public A time(long timePoint) {
        if (timePoint <= KConfig.END_OF_TIME && timePoint >= KConfig.BEGINNING_OF_TIME) {
            return internal_create(timePoint);
        } else {
            throw new RuntimeException("The selected Time " + timePoint + " is out of the range of KMF managed time");
        }
    }

    protected abstract A internal_create(long timePoint);

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractKUniverse)) {
            return false;
        } else {
            AbstractKUniverse casted = (AbstractKUniverse) obj;
            return casted._universe == _universe;
        }
    }

    @Override
    public B origin() {
        return (B) _manager.model().universe(_manager.parentUniverseKey(_universe));
    }

    @Override
    public B diverge() {
        AbstractKModel casted = (AbstractKModel) _manager.model();
        long nextKey = _manager.nextUniverseKey();
        B newUniverse = (B) casted.internalCreateUniverse(nextKey);
        _manager.initUniverse(newUniverse, this);
        return newUniverse;
    }

    @Override
    public List<B> descendants() {
        long[] descendentsKey = _manager.descendantsUniverseKeys(_universe);
        List<B> childs = new ArrayList<B>();
        for (int i = 0; i < descendentsKey.length; i++) {
            childs.add((B) _manager.model().universe(descendentsKey[i]));
        }
        return childs;
    }

    @Override
    public void lookupAllTimes(long uuid, long[] times, KCallback<KObject[]> cb) {
        //TODO
        throw new RuntimeException("Not implemented Yet !");
    }

    @Override
    public void listenAll(long groupId, long[] objects, KEventMultiListener multiListener) {
        model().manager().cdn().registerMultiListener(groupId, this, objects, multiListener);
    }
}

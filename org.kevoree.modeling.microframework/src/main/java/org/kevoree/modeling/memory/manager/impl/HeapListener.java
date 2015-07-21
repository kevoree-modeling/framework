package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KListener;
import org.kevoree.modeling.KObject;

public class HeapListener implements KListener {

    private long _universe;

    private ListenerManager _listenerManager;

    private long _id;

    public KCallback<KObject[]> cb;

    protected long listenerID() {
        return this._id;
    }

    public HeapListener(long p_universe, ListenerManager p_listenerManager, long p_id) {
        this._universe = p_universe;
        this._listenerManager = p_listenerManager;
        this._id = p_id;
    }

    @Override
    public long universe() {
        return 0;
    }

    @Override
    public long[] listenObjects() {
        return _listenerManager._listener2Objects.get(_id);
    }

    @Override
    public void listen(KObject obj) {
        _listenerManager.manageRegistration(_id,obj);
    }

    @Override
    public void delete() {
        _listenerManager.manageRegistration(_id,null);
    }

    @Override
    public void then(KCallback<KObject[]> p_cb) {
        this.cb = p_cb;
    }
}

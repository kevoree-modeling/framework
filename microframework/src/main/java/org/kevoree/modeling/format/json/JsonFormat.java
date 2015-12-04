package org.kevoree.modeling.format.json;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.format.KModelFormat;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.util.Checker;

public class JsonFormat implements KModelFormat {

    public static final String KEY_META = "@class";

    public static final String KEY_UUID = "@uuid";
    
    private KInternalDataManager _manager;

    private long _universe;

    private long _time;

    public JsonFormat(long p_universe, long p_time, KInternalDataManager p_manager) {
        this._manager = p_manager;
        this._universe = p_universe;
        this._time = p_time;
    }

    private static final String NULL_PARAM_MSG = "one parameter is null";

    @Override
    public void save(KObject model, KCallback<String> cb) {
        if (Checker.isDefined(model) && Checker.isDefined(cb)) {
            JsonModelSerializer.serialize(model, cb);
        } else {
            throw new RuntimeException(NULL_PARAM_MSG);
        }
    }

    @Override
    public void load(String payload, KCallback cb) {
        if (Checker.isDefined(payload)) {
            JsonModelLoader.load(_manager, _universe, _time, payload, cb);
        } else {
            throw new RuntimeException(NULL_PARAM_MSG);
        }
    }

}

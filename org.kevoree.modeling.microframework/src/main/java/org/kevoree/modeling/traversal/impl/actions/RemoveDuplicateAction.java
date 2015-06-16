package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap;
import org.kevoree.modeling.memory.struct.map.KLongMapCallBack;
import org.kevoree.modeling.traversal.KTraversalAction;

public class RemoveDuplicateAction implements KTraversalAction {

    private KTraversalAction _next;

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    @Override
    public void execute(KObject[] p_inputs) {
        ArrayLongMap elems = new ArrayLongMap(p_inputs.length, KConfig.CACHE_LOAD_FACTOR);
        for (int i = 0; i < p_inputs.length; i++) {
            elems.put(p_inputs[i].uuid(), p_inputs[i]);
        }
        final KObject[] trimmed = new KObject[elems.size()];
        final int[] nbInserted = {0};
        elems.each(new KLongMapCallBack<KObject>() {
            @Override
            public void on(long key, KObject value) {
                trimmed[nbInserted[0]] = value;
                nbInserted[0]++;
            }
        });
        _next.execute(trimmed);
    }

}

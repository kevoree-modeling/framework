package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongMap;
import org.kevoree.modeling.memory.chunk.KLongMapCallBack;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;

public class RemoveDuplicateAction implements KTraversalAction {

    private KTraversalAction _next;

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    @Override
    public void execute(KTraversalActionContext context) {
        ArrayLongMap elems = new ArrayLongMap(context.inputObjects().length, KConfig.CACHE_LOAD_FACTOR);
        for (int i = 0; i < context.inputObjects().length; i++) {
            elems.put(context.inputObjects()[i].uuid(), context.inputObjects()[i]);
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
        if (_next == null) {
            context.finalCallback().on(trimmed);
        } else {
            context.setInputObjects(trimmed);
            _next.execute(context);
        }
    }

}

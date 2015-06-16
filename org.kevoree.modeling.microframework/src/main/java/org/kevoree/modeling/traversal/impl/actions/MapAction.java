package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.traversal.KTraversalAction;

public class MapAction implements KTraversalAction {

    private KCallback<Object[]> _finalCallback;

    private KMetaAttribute _attribute;

    public MapAction(KMetaAttribute p_attribute, KCallback<Object[]> p_callback) {
        this._finalCallback = p_callback;
        this._attribute = p_attribute;
    }

    @Override
    public void chain(KTraversalAction next) {
        //terminal leaf action
    }

    @Override
    public void execute(KObject[] inputs) {
        Object[] selected = new Object[inputs.length];
        int nbElem = 0;
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] != null) {
                Object resolved = inputs[i].get(_attribute);
                if (resolved != null) {
                    selected[i] = resolved;
                    nbElem++;
                }
            }
        }
        //trim the array
        Object[] trimmed = new Object[nbElem];
        int nbInserted = 0;
        for (int i = 0; i < inputs.length; i++) {
            if (selected[i] != null) {
                trimmed[nbInserted] = selected[i];
                nbInserted++;
            }
        }
        _finalCallback.on(trimmed);
    }
}

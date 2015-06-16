package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.memory.manager.AccessMode;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.impl.MetaAttribute;
import org.kevoree.modeling.traversal.KTraversalAction;

public class FilterAttributeAction implements KTraversalAction {

    private KTraversalAction _next;

    private KMetaAttribute _attribute;

    private Object _expectedValue;

    public FilterAttributeAction(KMetaAttribute p_attribute, Object p_expectedValue) {
        this._attribute = p_attribute;
        this._expectedValue = p_expectedValue;
    }

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    @Override
    public void execute(KObject[] p_inputs) {
        if (p_inputs == null || p_inputs.length == 0) {
            _next.execute(p_inputs);
            return;
        } else {
            boolean[] selectedIndexes = new boolean[p_inputs.length];
            int nbSelected = 0;
            for (int i = 0; i < p_inputs.length; i++) {
                try {
                    final AbstractKObject loopObj = (AbstractKObject) p_inputs[i];
                    KMemorySegment raw = (loopObj)._manager.segment(loopObj.universe(),loopObj.now(),loopObj.uuid(), AccessMode.RESOLVE,loopObj.metaClass(), null);
                    if (raw != null) {
                        if (_attribute == null) {
                            if (_expectedValue == null) {
                                selectedIndexes[i] = true;
                                nbSelected++;
                            } else {
                                boolean addToNext = false;
                                KMeta[] metaElements = loopObj.metaClass().metaElements();
                                for (int j = 0; j < metaElements.length; j++) {
                                    if (metaElements[j] instanceof MetaAttribute) {
                                        Object resolved = raw.get(metaElements[j].index(), loopObj.metaClass());
                                        if (resolved == null) {
                                            if (_expectedValue.toString().equals("*")) {
                                                addToNext = true;
                                            }
                                        } else {
                                            if (resolved.equals(_expectedValue)) {
                                                addToNext = true;
                                            } else {
                                                if (resolved.toString().matches(_expectedValue.toString().replace("*", ".*"))) {
                                                    addToNext = true;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (addToNext) {
                                    selectedIndexes[i] = true;
                                    nbSelected++;
                                }
                            }
                        } else {
                            KMetaAttribute translatedAtt = loopObj.internal_transpose_att(_attribute);
                            if (translatedAtt != null) {
                                Object resolved = raw.get(translatedAtt.index(), loopObj.metaClass());
                                if (_expectedValue == null) {
                                    if (resolved == null) {
                                        selectedIndexes[i] = true;
                                        nbSelected++;
                                    }
                                } else {
                                    if (resolved == null) {
                                        if (_expectedValue.toString().equals("*")) {
                                            selectedIndexes[i] = true;
                                            nbSelected++;
                                        }
                                    } else {
                                        if (resolved.equals(_expectedValue)) {
                                            selectedIndexes[i] = true;
                                            nbSelected++;
                                        } else {
                                            if (resolved.toString().matches(_expectedValue.toString().replace("*", ".*"))) {
                                                selectedIndexes[i] = true;
                                                nbSelected++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        System.err.println("WARN: Empty KObject " + loopObj.uuid());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            KObject[] nextStepElement = new KObject[nbSelected];
            int inserted = 0;
            for (int i = 0; i < p_inputs.length; i++) {
                if (selectedIndexes[i]) {
                    nextStepElement[inserted] = p_inputs[i];
                    inserted++;
                }
            }
            _next.execute(nextStepElement);
        }
    }

}

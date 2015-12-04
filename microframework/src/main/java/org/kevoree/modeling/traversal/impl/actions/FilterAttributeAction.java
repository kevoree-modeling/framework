package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.impl.MetaAttribute;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;
import org.kevoree.modeling.util.PrimitiveHelper;

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
    public void execute(KTraversalActionContext context) {
        if (context.inputObjects() == null || context.inputObjects().length == 0) {
            if (_next != null) {
                _next.execute(context);
            } else {
                context.finalCallback().on(context.inputObjects());
            }
        } else {
            boolean[] selectedIndexes = new boolean[context.inputObjects().length];
            int nbSelected = 0;
            for (int i = 0; i < context.inputObjects().length; i++) {
                try {
                    final AbstractKObject loopObj = (AbstractKObject) context.inputObjects()[i];
                    KObjectChunk raw = (loopObj)._manager.closestChunk(loopObj.universe(), loopObj.now(), loopObj.uuid(), loopObj.metaClass(), loopObj.previousResolved());
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
                                        Object resolved = raw.getPrimitiveType(metaElements[j].index(), loopObj.metaClass());
                                        if (resolved == null) {
                                            if (PrimitiveHelper.equals(_expectedValue.toString(), "*")) {
                                                addToNext = true;
                                            }
                                        } else {
                                            if (PrimitiveHelper.equals(resolved.toString(), _expectedValue.toString())) {
                                                addToNext = true;
                                            } else {
                                                if (PrimitiveHelper.matches(resolved.toString(), _expectedValue.toString().replace("*", ".*"))) {
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
                                Object resolved = raw.getPrimitiveType(translatedAtt.index(), loopObj.metaClass());
                                if (_expectedValue == null) {
                                    if (resolved == null) {
                                        selectedIndexes[i] = true;
                                        nbSelected++;
                                    }
                                } else {
                                    if (resolved == null) {
                                        if (PrimitiveHelper.equals(_expectedValue.toString(), "*")) {
                                            selectedIndexes[i] = true;
                                            nbSelected++;
                                        }
                                    } else {
                                        if (PrimitiveHelper.equals(resolved.toString(), _expectedValue.toString())) {
                                            selectedIndexes[i] = true;
                                            nbSelected++;
                                        } else {
                                            if (PrimitiveHelper.matches(resolved.toString(), _expectedValue.toString().replace("*", ".*"))) {
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
            for (int i = 0; i < context.inputObjects().length; i++) {
                if (selectedIndexes[i]) {
                    nextStepElement[inserted] = context.inputObjects()[i];
                    inserted++;
                }
            }
            if (_next == null) {
                context.finalCallback().on(nextStepElement);
            } else {
                context.setInputObjects(nextStepElement);
                _next.execute(context);
            }
        }
    }

}

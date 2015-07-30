package org.kevoree.modeling.traversal.impl.actions;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.map.KStringMapCallBack;
import org.kevoree.modeling.memory.map.impl.ArrayStringMap;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaAttribute;
import org.kevoree.modeling.meta.impl.MetaAttribute;
import org.kevoree.modeling.traversal.KTraversalAction;
import org.kevoree.modeling.traversal.KTraversalActionContext;

public class FilterAttributeQueryAction implements KTraversalAction {

    private KTraversalAction _next;

    private String _attributeQuery;

    public FilterAttributeQueryAction(String p_attributeQuery) {
        this._attributeQuery = p_attributeQuery;
    }

    @Override
    public void chain(KTraversalAction p_next) {
        _next = p_next;
    }

    @Override
    public void execute(KTraversalActionContext context) {
        if (context.inputObjects() == null || context.inputObjects().length == 0) {
            if(_next != null){
                _next.execute(context);
            } else {
                context.finalCallback().on(context.inputObjects());
            }
        } else {
            boolean[] selectedIndexes = new boolean[context.inputObjects().length];
            int nbSelected = 0;
            for (int i = 0; i < context.inputObjects().length; i++) {
                try {
                    AbstractKObject loopObj = (AbstractKObject) context.inputObjects()[i];
                    if (_attributeQuery == null) {
                        selectedIndexes[i] = true;
                        nbSelected++;
                    } else {
                        KMeta[] metaElements = loopObj.metaClass().metaElements();
                        ArrayStringMap<QueryParam> params = buildParams(_attributeQuery);
                        final boolean[] selectedForNext = {true};
                        params.each(new KStringMapCallBack<QueryParam>() {
                            @Override
                            public void on(String key, QueryParam param) {
                                for (int j = 0; j < metaElements.length; j++) {
                                    if(metaElements[j] instanceof MetaAttribute){
                                        KMetaAttribute metaAttribute = (KMetaAttribute) metaElements[j];
                                        if (metaAttribute.metaName().matches("^"+param.name()+"$")) {
                                            Object o_raw = loopObj.get(metaAttribute);
                                            if (o_raw != null) {
                                                if (param.value().equals("null")) {
                                                    if (!param.isNegative()) {
                                                        selectedForNext[0] = false;
                                                    }
                                                } else if (o_raw.toString().matches("^"+param.value()+"$")) {
                                                    if (param.isNegative()) {
                                                        selectedForNext[0] = false;
                                                    }
                                                } else {
                                                    if (!param.isNegative()) {
                                                        selectedForNext[0] = false;
                                                    }
                                                }
                                            } else {
                                                if (param.value().equals("null") || param.value().equals("*")) {
                                                    if (param.isNegative()) {
                                                        selectedForNext[0] = false;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        if (selectedForNext[0]) {
                            selectedIndexes[i] = true;
                            nbSelected++;
                        }
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

    private ArrayStringMap<QueryParam> buildParams(String p_paramString) {
        ArrayStringMap<QueryParam> params = new ArrayStringMap<QueryParam>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        int iParam = 0;
        int lastStart = iParam;
        while (iParam < p_paramString.length()) {
            if (p_paramString.charAt(iParam) == ',') {
                String p = p_paramString.substring(lastStart, iParam).trim();
                if (!p.equals("") && !p.equals("*")) {
                    if (p.endsWith("=")) {
                        p = p + "*";
                    }
                    String[] pArray = p.split("=");
                    QueryParam pObject;
                    if (pArray.length > 1) {
                        String paramKey = pArray[0].trim();
                        boolean negative = paramKey.endsWith("!");
                        pObject = new QueryParam(paramKey.replace("!", "").replace("*", ".*"), pArray[1].trim().replace("*", ".*"), negative);
                        params.put(pObject.name(), pObject);
                    }
                }
                lastStart = iParam + 1;
            }
            iParam = iParam + 1;
        }
        String lastParam = p_paramString.substring(lastStart, iParam).trim();
        if (!lastParam.equals("") && !lastParam.equals("*")) {
            if (lastParam.endsWith("=")) {
                lastParam = lastParam + "*";
            }
            String[] pArray = lastParam.split("=");
            QueryParam pObject;
            if (pArray.length > 1) {
                String paramKey = pArray[0].trim();
                boolean negative = paramKey.endsWith("!");
                pObject = new QueryParam(paramKey.replace("!", "").replace("*", ".*"), pArray[1].trim().replace("*", ".*"), negative);
                params.put(pObject.name(), pObject);
            }
        }
        return params;
    }

    private class QueryParam {

        private String _name;
        private String _value;
        private boolean _negative;

        public QueryParam(String p_name, String p_value, boolean p_negative) {
            this._name = p_name;
            this._value = p_value;
            this._negative = p_negative;
        }

        public String name() {
            return _name;
        }

        public String value() {
            return _value;
        }

        public boolean isNegative() {
            return _negative;
        }

    }

}

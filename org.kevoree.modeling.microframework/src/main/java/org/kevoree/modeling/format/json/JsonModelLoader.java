package org.kevoree.modeling.format.json;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.abs.AbstractKModel;
import org.kevoree.modeling.abs.AbstractKObject;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KLongLongMap;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongLongMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayStringMap;
import org.kevoree.modeling.memory.chunk.KStringMapCallBack;
import org.kevoree.modeling.util.PrimitiveHelper;

import java.util.ArrayList;
import java.util.List;

public class JsonModelLoader {

    /**
     * @native ts
     * if (payload == null) {
     * callback(null);
     * } else {
     * var toLoadObj = JSON.parse(payload);
     * var rootElem = [];
     * var mappedKeys: org.kevoree.modeling.memory.chunk.impl.ArrayLongLongMap = new org.kevoree.modeling.memory.chunk.impl.ArrayLongLongMap(-1,-1,-1,null);
     * for(var i = 0; i < toLoadObj.length; i++) {
     * var elem = toLoadObj[i];
     * var kid = elem[org.kevoree.modeling.format.json.JsonFormat.KEY_UUID];
     * mappedKeys.put(<number>kid, manager.nextObjectKey());
     * }
     * for(var i = 0; i < toLoadObj.length; i++) {
     * var elemRaw = toLoadObj[i];
     * var elem2 = new org.kevoree.modeling.memory.chunk.impl.ArrayStringMap<any>(Object.keys(elemRaw).length, org.kevoree.modeling.KConfig.CACHE_LOAD_FACTOR);
     * for(var ik in elemRaw){ elem2[ik] = elemRaw[ik]; }
     * try {
     * org.kevoree.modeling.format.json.JsonModelLoader.loadObj(elem2, manager, universe, time, mappedKeys, rootElem);
     * } catch(e){ console.error(e); }
     * }
     * if (rootElem[0] != null) { manager.setRoot(rootElem[0], (throwable : java.lang.Throwable) => { if (callback != null) { callback(throwable); }}); } else { if (callback != null) { callback(null); } }
     * }
     */
    public static void load(KInternalDataManager manager, long universe, long time, String payload, final KCallback<Throwable> callback) {
        if (payload == null) {
            callback.on(null);
        } else {
            Lexer lexer = new Lexer(payload);
            JsonType currentToken = lexer.nextToken();
            if (currentToken != JsonType.LEFT_BRACKET) {
                callback.on(null);
            } else {
                final List<ArrayStringMap<Object>> alls = new ArrayList<ArrayStringMap<Object>>();
                ArrayStringMap<Object> content = new ArrayStringMap<Object>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                String currentAttributeName = null;
                ArrayList<String> arrayPayload = null;
                currentToken = lexer.nextToken();
                while (currentToken != JsonType.EOF) {
                    if (currentToken.equals(JsonType.LEFT_BRACKET)) {
                        arrayPayload = new ArrayList<String>();
                    } else if (currentToken.equals(JsonType.RIGHT_BRACKET)) {
                        content.put(currentAttributeName, arrayPayload);
                        arrayPayload = null;
                        currentAttributeName = null;
                    } else if (currentToken.equals(JsonType.LEFT_BRACE)) {
                        content = new ArrayStringMap<Object>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                    } else if (currentToken.equals(JsonType.RIGHT_BRACE)) {
                        alls.add(content);
                        content = new ArrayStringMap<Object>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                    } else if (currentToken.equals(JsonType.VALUE)) {
                        if (currentAttributeName == null) {
                            currentAttributeName = lexer.lastValue();
                        } else {
                            if (arrayPayload == null) {
                                content.put(currentAttributeName, lexer.lastValue());
                                currentAttributeName = null;
                            } else {
                                arrayPayload.add(lexer.lastValue());
                            }
                        }
                    }
                    currentToken = lexer.nextToken();
                }
                final KObject[] rootElem = {null};
                ArrayLongLongMap mappedKeys = new ArrayLongLongMap(-1, -1, -1, null);
                for (int i = 0; i < alls.size(); i++) {
                    try {
                        ArrayStringMap<Object> elem = alls.get(i);
                        long kid = PrimitiveHelper.parseLong(elem.get(JsonFormat.KEY_UUID).toString());
                        mappedKeys.put(kid, manager.nextObjectKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < alls.size(); i++) {
                    try {
                        ArrayStringMap<Object> elem = alls.get(i);
                        loadObj(elem, manager, universe, time, mappedKeys, rootElem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (rootElem[0] != null) {
                    manager.setRoot(rootElem[0], new KCallback<Throwable>() {
                        @Override
                        public void on(Throwable throwable) {
                            if (callback != null) {
                                callback.on(throwable);
                            }
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.on(null);
                    }
                }
            }
        }
    }

    private static void loadObj(ArrayStringMap<Object> p_param, KInternalDataManager manager, long universe, long time, KLongLongMap p_mappedKeys, KObject[] p_rootElem) {
        long kid = PrimitiveHelper.parseLong(p_param.get(JsonFormat.KEY_UUID).toString());
        String meta = p_param.get(JsonFormat.KEY_META).toString();
        KMetaClass metaClass = manager.model().metaModel().metaClassByName(meta);
        KObject current = ((AbstractKModel) manager.model()).createProxy(universe, time, p_mappedKeys.get(kid), metaClass, universe, time);
        manager.initKObject(current);
        KObjectChunk raw = manager.preciseChunk(current.universe(), current.now(), current.uuid(), current.metaClass(), ((AbstractKObject) current).previousResolved());
        p_param.each(new KStringMapCallBack<Object>() {
            @Override
            public void on(String metaKey, Object payload_content) {
                if (PrimitiveHelper.equals(metaKey, JsonFormat.KEY_ROOT)) {
                    p_rootElem[0] = current;
                } else {
                    KMeta metaElement = metaClass.metaByName(metaKey);
                    if (payload_content != null) {
                        if (metaElement != null && metaElement.metaType().equals(MetaType.ATTRIBUTE)) {
                            KMetaAttribute metaAttribute = (KMetaAttribute) metaElement;
                            int metaAttId = metaAttribute.attributeTypeId();
                            switch (metaAttId) {
                                case KPrimitiveTypes.CONTINUOUS_ID:
                                    String[] plainRawSet = (String[]) p_param.get(metaAttribute.metaName());
                                    double[] convertedRaw = new double[plainRawSet.length];
                                    for (int l = 0; l < plainRawSet.length; l++) {
                                        try {
                                            convertedRaw[l] = PrimitiveHelper.parseDouble(plainRawSet[l]);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    raw.setPrimitiveType(metaElement.index(), convertedRaw, current.metaClass());
                                    break;
                                default:
                                    Object converted = null;
                                    String rawPayload = p_param.get(metaElement.metaName()).toString();
                                    switch (metaAttId) {
                                        case KPrimitiveTypes.STRING_ID:
                                            converted = JsonString.unescape(rawPayload);
                                            break;
                                        case KPrimitiveTypes.LONG_ID:
                                            converted = PrimitiveHelper.parseLong(rawPayload);
                                            break;
                                        case KPrimitiveTypes.INT_ID:
                                            converted = PrimitiveHelper.parseInt(rawPayload);
                                            break;
                                        case KPrimitiveTypes.BOOL_ID:
                                            converted = PrimitiveHelper.parseBoolean(rawPayload);
                                            break;
                                        case KPrimitiveTypes.DOUBLE_ID:
                                            converted = PrimitiveHelper.parseDouble(rawPayload);
                                            break;
                                    }
                                    raw.setPrimitiveType(metaElement.index(), converted, current.metaClass());
                                    break;
                            }
                        } else if (metaElement != null && metaElement.metaType() == MetaType.REFERENCE) {
                            try {
                                raw.setPrimitiveType(metaElement.index(), transposeArr((ArrayList<String>) payload_content, p_mappedKeys), current.metaClass());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    private static long[] transposeArr(ArrayList<String> plainRawSet, KLongLongMap p_mappedKeys) {
        if (plainRawSet == null) {
            return null;
        }
        int sizeOfL = sizeOfList(plainRawSet);
        long[] convertedRaw = new long[sizeOfL];
        for (int l = 0; l < sizeOfL; l++) {
            try {
                long converted = PrimitiveHelper.parseLong(getString(plainRawSet, l));
                if (p_mappedKeys.contains(converted)) {
                    converted = p_mappedKeys.get(converted);
                }
                convertedRaw[l] = converted;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return convertedRaw;
    }

    /**
     * @native ts
     * if(plainRawSet != null && plainRawSet != undefined){
     * if(plainRawSet.size != undefined){
     * return plainRawSet.size();
     * } else {
     * return plainRawSet.length;
     * }
     * }
     */
    private static int sizeOfList(ArrayList<String> plainRawSet) {
        return plainRawSet.size();
    }

    /**
     * @native ts
     * if(plainRawSet.get != undefined){
     * return plainRawSet.get(l);
     * } else {
     * return plainRawSet[l];
     * }
     */
    private static String getString(ArrayList<String> plainRawSet, int l) {
        return plainRawSet.get(l);
    }

}


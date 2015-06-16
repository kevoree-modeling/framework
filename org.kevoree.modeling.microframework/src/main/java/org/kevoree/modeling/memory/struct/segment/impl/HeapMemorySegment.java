package org.kevoree.modeling.memory.struct.segment.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.format.json.JsonFormat;
import org.kevoree.modeling.format.json.JsonObjectReader;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.meta.*;

public class HeapMemorySegment implements KMemorySegment {

    private Object[] raw;

    private int _counter = 0;

    private int _metaClassIndex = -1;

    private boolean[] _modifiedIndexes = null;

    private boolean _dirty = false;

    @Override
    public void initMetaClass(KMetaClass p_metaClass) {
        this.raw = new Object[p_metaClass.metaElements().length];
        _metaClassIndex = p_metaClass.index();
    }

    @Override
    public int metaClassIndex() {
        return _metaClassIndex;
    }

    @Override
    public boolean isDirty() {
        return _dirty;
    }

    /**
     * @native ts
     * var builder = {};
     * var metaClass = metaModel.metaClass(this._metaClassIndex);
     * var metaElements = metaClass.metaElements();
     * for (var i = 0; i < this.raw.length; i++) {
     * if(this.raw[i] != undefined && this.raw[i] != null){ builder[metaElements[i].metaName()] = this.raw[i]; }
     * }
     * return JSON.stringify(builder);
     */
    @Override
    public String serialize(KMetaModel metaModel) {
        KMetaClass metaClass = metaModel.metaClass(_metaClassIndex);
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean isFirst = true;
        KMeta[] metaElements = metaClass.metaElements();
        if (raw != null && metaElements != null) {
            for (int i = 0; i < raw.length && i < metaElements.length; i++) {
                Object o = raw[i];
                if (o != null) {
                    if (isFirst) {
                        builder.append("\"");
                        isFirst = false;
                    } else {
                        builder.append(",\"");
                    }
                    builder.append(metaElements[i].metaName());
                    builder.append("\":");
                    if (o instanceof String) {
                        builder.append("\"");
                        builder.append(JsonString.encode((String) o));
                        builder.append("\"");
                    } else if (o instanceof double[]) {
                        builder.append("[");
                        double[] castedArr = (double[]) o;
                        for (int j = 0; j < castedArr.length; j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append(castedArr[j]);
                        }
                        builder.append("]");
                    } else if (o instanceof long[]) {
                        builder.append("[");
                        long[] castedArr = (long[]) o;
                        for (int j = 0; j < castedArr.length; j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append(castedArr[j]);
                        }
                        builder.append("]");
                    } else {
                        builder.append(o.toString());
                    }
                }
            }
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public int[] modifiedIndexes(KMetaClass p_metaClass) {
        if (_modifiedIndexes == null) {
            return new int[0];
        } else {
            int nbModified = 0;
            for (int i = 0; i < _modifiedIndexes.length; i++) {
                if (_modifiedIndexes[i]) {
                    nbModified = nbModified + 1;
                }
            }
            int[] result = new int[nbModified];
            int inserted = 0;
            for (int i = 0; i < _modifiedIndexes.length; i++) {
                if (_modifiedIndexes[i]) {
                    result[inserted] = i;
                    inserted = inserted + 1;
                }
            }
            return result;
        }
    }

    @Override
    public void setClean(KMetaModel metaModel) {
        _dirty = false;
        _modifiedIndexes = null;
    }

    @Override
    public void setDirty() {
        _dirty = true;
    }

    /**
     * @native ts
     * var rawElem = JSON.parse(payload);
     * var metaClass = metaModel.metaClass(this._metaClassIndex);
     * this.raw = [];
     * for (var key in rawElem) {
     * if("@class" != key){
     * var elem = metaClass.metaByName(key);
     * if(elem != null && elem != undefined){ this.raw[elem.index()] = rawElem[key]; }
     * }
     * }
     */
    @Override
    public void init(String payload, KMetaModel metaModel) throws Exception {
        if (payload != null) {
            JsonObjectReader objectReader = new JsonObjectReader();
            objectReader.parseObject(payload);
            KMetaClass metaClass = metaModel.metaClass(_metaClassIndex);
            String[] metaKeys = objectReader.keys();
            for (int i = 0; i < metaKeys.length; i++) {
                KMeta metaElement = metaClass.metaByName(metaKeys[i]);
                Object insideContent = objectReader.get(metaKeys[i]);
                if (insideContent != null) {
                    if (metaElement != null && metaElement.metaType().equals(MetaType.ATTRIBUTE)) {
                        KMetaAttribute metaAttribute = (KMetaAttribute) metaElement;
                        Object converted = null;
                        if (metaAttribute.attributeType() == KPrimitiveTypes.STRING) {
                            converted = JsonString.unescape((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.LONG) {
                            converted = Long.parseLong((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.INT) {
                            converted = Integer.parseInt((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.BOOL) {
                            converted = Boolean.parseBoolean((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.SHORT) {
                            converted = Short.parseShort((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.DOUBLE) {
                            converted = Double.parseDouble((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.FLOAT) {
                            converted = Float.parseFloat((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.CONTINUOUS) {
                            String[] plainRawSet = objectReader.getAsStringArray(metaKeys[i]);
                            double[] convertedRaw = new double[plainRawSet.length];
                            for (int l = 0; l < plainRawSet.length; l++) {
                                try {
                                    convertedRaw[l] = Double.parseDouble(plainRawSet[l]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            converted = convertedRaw;
                        }
                        raw[metaAttribute.index()] = converted;
                    } else if (metaElement != null && metaElement instanceof KMetaReference) {
                        try {
                            String[] plainRawSet = objectReader.getAsStringArray(metaKeys[i]);
                            long[] convertedRaw = new long[plainRawSet.length];
                            for (int l = 0; l < plainRawSet.length; l++) {
                                try {
                                    convertedRaw[l] = Long.parseLong(plainRawSet[l]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            raw[metaElement.index()] = convertedRaw;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    @Override
    public int counter() {
        return _counter;
    }

    @Override
    public void inc() {
        _counter++;
    }

    @Override
    public void dec() {
        _counter--;
    }

    @Override
    public void free(KMetaModel metaModel) {
        raw = null;
    }

    @Override
    public Object get(int index, KMetaClass p_metaClass) {
        if (raw != null) {
            return raw[index];
        } else {
            return null;
        }
    }

    @Override
    public int getRefSize(int index, KMetaClass metaClass) {
        long[] existing = (long[]) raw[index];
        if (existing != null) {
            return existing.length;
        }
        return 0;
    }

    @Override
    public long getRefElem(int index, int refIndex, KMetaClass metaClass) {
        long[] existing = (long[]) raw[index];
        if (existing != null) {
            return existing[refIndex];
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long[] getRef(int index, KMetaClass p_metaClass) {
        if (raw != null) {
            Object previousObj = raw[index];
            if (previousObj != null) {
                try {
                    return (long[]) previousObj;
                } catch (Exception e) {
                    e.printStackTrace();
                    raw[index] = null;
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean addRef(int index, long newRef, KMetaClass metaClass) {
        if (raw != null) {
            long[] previous = (long[]) raw[index];
            if (previous == null) {
                previous = new long[1];
                previous[0] = newRef;
            } else {
                for (int i = 0; i < previous.length; i++) {
                    if (previous[i] == newRef) {
                        return false;
                    }
                }
                long[] incArray = new long[previous.length + 1];
                System.arraycopy(previous, 0, incArray, 0, previous.length);
                incArray[previous.length] = newRef;
                previous = incArray;
            }
            raw[index] = previous;
            if (_modifiedIndexes == null) {
                _modifiedIndexes = new boolean[raw.length];
            }
            _modifiedIndexes[index] = true;
            _dirty = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeRef(int index, long refToRemove, KMetaClass metaClass) {
        if (raw != null) {
            long[] previous = (long[]) raw[index];
            if (previous != null) {
                int indexToRemove = -1;
                for (int i = 0; i < previous.length; i++) {
                    if (previous[i] == refToRemove) {
                        indexToRemove = i;
                        break;
                    }
                }
                if (indexToRemove != -1) {
                    if ((previous.length - 1) == 0) {
                        raw[index] = null;
                    } else {
                        long[] newArray = new long[previous.length - 1];
                        System.arraycopy(previous, 0, newArray, 0, indexToRemove);
                        System.arraycopy(previous, indexToRemove + 1, newArray, indexToRemove, previous.length - indexToRemove - 1);
                        raw[index] = newArray;
                    }
                    if (_modifiedIndexes == null) {
                        _modifiedIndexes = new boolean[raw.length];
                    }
                    _modifiedIndexes[index] = true;
                    _dirty = true;
                }
            }
        }
        return false;
    }

    @Override
    public double[] getInfer(int index, KMetaClass metaClass) {
        if (raw != null) {
            Object previousObj = raw[index];
            if (previousObj != null) {
                try {
                    return (double[]) previousObj;
                } catch (Exception e) {
                    e.printStackTrace();
                    raw[index] = null;
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public int getInferSize(int index, KMetaClass metaClass) {
        Object previousObj = raw[index];
        if (previousObj != null) {
            return ((double[]) previousObj).length;
        }
        return 0;
    }

    /**
     * @native ts
     * var res = this.raw[index];
     * if(res != null && res != undefined){ return res[arrayIndex]; }
     * return 0;
     */
    @Override
    public double getInferElem(int index, int arrayIndex, KMetaClass metaClass) {
        double[] res = getInfer(index, metaClass);
        if (res != null && arrayIndex >= 0 && arrayIndex < res.length) {
            return res[arrayIndex];
        }
        return 0;
    }

    /**
     * @native ts
     * var res = this.raw[index];
     * if(res != null && res != undefined){ res[arrayIndex] = valueToInsert; }
     * this._dirty = true;
     */
    @Override
    public void setInferElem(int index, int arrayIndex, double valueToInsert, KMetaClass metaClass) {
        double[] res = getInfer(index, metaClass);
        if (res != null && arrayIndex >= 0 && arrayIndex < res.length) {
            res[arrayIndex] = valueToInsert;
            _dirty = true;
        }
    }

    @Override
    public void extendInfer(int index, int newSize, KMetaClass metaClass) {
        if (raw != null) {
            double[] previous = (double[]) raw[index];
            if (previous == null) {
                previous = new double[newSize];
            } else {
                double[] incArray = new double[newSize];
                System.arraycopy(previous, 0, incArray, 0, previous.length);
                previous = incArray;
            }
            raw[index] = previous;
            if (_modifiedIndexes == null) {
                _modifiedIndexes = new boolean[raw.length];
            }
            _modifiedIndexes[index] = true;
            _dirty = true;
        }
    }

    @Override
    public synchronized void set(int index, Object content, KMetaClass p_metaClass) {
        raw[index] = content;
        _dirty = true;
        if (_modifiedIndexes == null) {
            _modifiedIndexes = new boolean[raw.length];
        }
        _modifiedIndexes[index] = true;
    }

    @Override
    public KMemorySegment clone(KMetaClass p_metaClass) {
        if (raw == null) {
            return new HeapMemorySegment();
        } else {
            Object[] cloned = new Object[raw.length];
            System.arraycopy(raw, 0, cloned, 0, raw.length);
            HeapMemorySegment clonedEntry = new HeapMemorySegment();
            clonedEntry._dirty = true;
            clonedEntry.raw = cloned;
            clonedEntry._metaClassIndex = _metaClassIndex;
            return clonedEntry;
        }
    }

}

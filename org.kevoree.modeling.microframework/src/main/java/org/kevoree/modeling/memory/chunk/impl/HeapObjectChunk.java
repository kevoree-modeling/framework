package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.format.json.JsonObjectReader;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.util.Checker;
import org.kevoree.modeling.util.PrimitiveHelper;
import org.kevoree.modeling.util.Base64;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class HeapObjectChunk implements KObjectChunk {

    private final KChunkSpace _space;

    private final AtomicLong _flags;

    private final AtomicInteger _counter;

    private AtomicReference<long[]> _dependencies;

    private final long _universe;

    private final long _time;

    private final long _obj;

    private Object[] raw;

    private int _metaClassIndex = -1;

    public HeapObjectChunk(long p_universe, long p_time, long p_obj, KChunkSpace p_space) {
        this._universe = p_universe;
        this._time = p_time;
        this._obj = p_obj;
        this._flags = new AtomicLong(0);
        this._counter = new AtomicInteger(0);
        this._dependencies = new AtomicReference<long[]>();
        this._space = p_space;
    }

    @Override
    public KChunkSpace space() {
        return _space;
    }

    @Override
    public int metaClassIndex() {
        return _metaClassIndex;
    }

    @Override
    public String serialize(final KMetaModel metaModel) {
        final KMetaClass metaClass = metaModel.metaClass(_metaClassIndex);
        final StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        KMeta[] metaElements = metaClass.metaElements();
        if (raw != null && metaElements != null) {
            for (int i = 0; i < raw.length && i < metaElements.length; i++) {
                if (raw[i] != null) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        builder.append(KConfig.CHUNK_ELEM_SEP);
                    }
                    Base64.encodeStringToBuffer(metaElements[i].metaName(), builder);
                    builder.append(KConfig.CHUNK_VAL_SEP);
                    if (metaElements[i].metaType() == MetaType.ATTRIBUTE) {
                        KMetaAttribute metaAttribute = (KMetaAttribute) metaElements[i];
                        int metaAttId = metaAttribute.attributeTypeId();
                        switch (metaAttId) {
                            case KPrimitiveTypes.STRING_ID:
                                Base64.encodeStringToBuffer((String) raw[i], builder);
                                break;
                            case KPrimitiveTypes.LONG_ID:
                                Base64.encodeLongToBuffer((long) raw[i], builder);
                                break;
                            case KPrimitiveTypes.CONTINUOUS_ID:
                                double[] castedArr = (double[]) raw[i];
                                Base64.encodeIntToBuffer(castedArr.length, builder);
                                for (int j = 0; j < castedArr.length; j++) {
                                    builder.append(KConfig.CHUNK_VAL_SEP);
                                    Base64.encodeDoubleToBuffer(castedArr[j], builder);
                                }
                                break;
                            case KPrimitiveTypes.BOOL_ID:
                                if ((boolean) raw[i]) {
                                    builder.append("1");
                                } else {
                                    builder.append("0");
                                }
                                break;
                            case KPrimitiveTypes.DOUBLE_ID:
                                Base64.encodeDoubleToBuffer((double) raw[i], builder);
                                break;
                            case KPrimitiveTypes.INT_ID:
                                Base64.encodeIntToBuffer((int) raw[i], builder);
                                break;
                            default:
                                if (KPrimitiveTypes.isEnum(metaAttribute.attributeTypeId())) {
                                    Base64.encodeIntToBuffer((int) raw[i], builder);
                                }
                                break;
                        }
                    } else if (metaElements[i].metaType() == MetaType.RELATION) {
                        long[] castedArr = (long[]) raw[i];
                        Base64.encodeIntToBuffer(castedArr.length, builder);
                        for (int j = 0; j < castedArr.length; j++) {
                            builder.append(KConfig.CHUNK_VAL_SEP);
                            Base64.encodeLongToBuffer(castedArr[j], builder);
                        }
                    } else if (metaElements[i].metaType() == MetaType.DEPENDENCIES || metaElements[i].metaType() == MetaType.INPUT || metaElements[i].metaType() == MetaType.OUTPUT) {
                        double[] castedArr = (double[]) raw[i];
                        Base64.encodeIntToBuffer(castedArr.length, builder);
                        for (int j = 0; j < castedArr.length; j++) {
                            builder.append(KConfig.CHUNK_VAL_SEP);
                            Base64.encodeDoubleToBuffer(castedArr[j], builder);
                        }
                    }
                }
            }
        }
        return builder.toString();
    }

    private final Object loadObject(KMetaAttribute metaAttribute, String p_payload, int p_start, int p_end) {
        int metaAttId = metaAttribute.attributeTypeId();
        switch (metaAttId) {
            case KPrimitiveTypes.STRING_ID:
                return Base64.decodeToStringWithBounds(p_payload, p_start, p_end);
            case KPrimitiveTypes.LONG_ID:
                return Base64.decodeToLongWithBounds(p_payload, p_start, p_end);
            case KPrimitiveTypes.INT_ID:
                return Base64.decodeToIntWithBounds(p_payload, p_start, p_end);
            case KPrimitiveTypes.BOOL_ID:
                if (p_payload.charAt(p_start) == '1') {
                    return true;
                } else {
                    return false;
                }
            case KPrimitiveTypes.DOUBLE_ID:
                return Base64.decodeToDoubleWithBounds(p_payload, p_start, p_end);
            default:
                return null;
        }
    }

    @Override
    public void init(String payload, KMetaModel metaModel, int metaClassIndex) {
        if (this._metaClassIndex == -1) {
            this._metaClassIndex = metaClassIndex;
        }
        if (this._metaClassIndex == -1) {
            return;
        }
        KMetaClass metaClass = metaModel.metaClass(_metaClassIndex);
        this.raw = new Object[metaClass.metaElements().length];
        if (payload != null) {
            int i = 0;
            final int payloadSize = payload.length();
            KMeta previousMeta = null;
            int previousValStart = 0;
            double[] doubleArray = null;
            long[] longArray = null;
            int currentArrayIndex = -1;
            while (i < payloadSize) {
                if (payload.charAt(i) == KConfig.CHUNK_ELEM_SEP) {
                    if (previousMeta != null) {
                        if (previousMeta.metaType().equals(MetaType.ATTRIBUTE) && ((KMetaAttribute) previousMeta).attributeTypeId() != KPrimitiveTypes.CONTINUOUS_ID) {
                            raw[previousMeta.index()] = loadObject((KMetaAttribute) previousMeta, payload, previousValStart, i);
                        } else if (previousMeta.metaType().equals(MetaType.RELATION) && longArray != null) {
                            longArray[currentArrayIndex] = Base64.decodeToLongWithBounds(payload, previousValStart, i);
                            raw[previousMeta.index()] = longArray;
                            longArray = null;
                        } else if (doubleArray != null) {
                            doubleArray[currentArrayIndex] = Base64.decodeToDoubleWithBounds(payload, previousValStart, i);
                            raw[previousMeta.index()] = doubleArray;
                            doubleArray = null;
                        }
                    }
                    previousMeta = null;
                    previousValStart = i + 1;
                } else if (payload.charAt(i) == KConfig.CHUNK_VAL_SEP) {
                    if (previousMeta == null) {
                        previousMeta = metaClass.metaByName(Base64.decodeToStringWithBounds(payload,previousValStart, i));
                    } else {
                        if (previousMeta.metaType().equals(MetaType.RELATION)) {
                            if (longArray == null) {
                                longArray = new long[Base64.decodeToIntWithBounds(payload, previousValStart, i)];
                                currentArrayIndex = 0;
                            } else {
                                longArray[currentArrayIndex] = Base64.decodeToLongWithBounds(payload, previousValStart, i);
                                currentArrayIndex++;
                            }
                        } else {
                            //DEPENDENCY, INPUT or OUTPUT, or ATT CONTINUOUS , => double[]
                            if (doubleArray == null) {
                                doubleArray = new double[Base64.decodeToIntWithBounds(payload, previousValStart, i)];
                                currentArrayIndex = 0;
                            } else {
                                doubleArray[currentArrayIndex] = Base64.decodeToDoubleWithBounds(payload, previousValStart, i);
                                currentArrayIndex++;
                            }
                        }
                    }
                    previousValStart = i + 1;
                }
                i++;
            }
            if (previousMeta != null) {
                if (previousMeta.metaType().equals(MetaType.ATTRIBUTE) && ((KMetaAttribute) previousMeta).attributeTypeId() != KPrimitiveTypes.CONTINUOUS_ID) {
                    raw[previousMeta.index()] = loadObject((KMetaAttribute) previousMeta, payload, previousValStart, i);
                } else if (previousMeta.metaType().equals(MetaType.RELATION) && longArray != null) {
                    longArray[currentArrayIndex] = Base64.decodeToLongWithBounds(payload, previousValStart, i);
                    raw[previousMeta.index()] = longArray;
                } else if (doubleArray != null) {
                    doubleArray[currentArrayIndex] = Base64.decodeToDoubleWithBounds(payload, previousValStart, i);
                    raw[previousMeta.index()] = doubleArray;
                }
            }
        }
    }

    @Override
    public final int counter() {
        return this._counter.get();
    }

    @Override
    public final int inc() {
        return this._counter.incrementAndGet();
    }

    @Override
    public final int dec() {
        return this._counter.decrementAndGet();
    }

    @Override
    public void free(KMetaModel metaModel) {
        raw = null;
    }

    @Override
    public short type() {
        return KChunkTypes.OBJECT_CHUNK;
    }

    @Override
    public Object getPrimitiveType(int index, KMetaClass p_metaClass) {
        if (raw != null) {
            return raw[index];
        } else {
            return null;
        }
    }

    @Override
    public int getLongArraySize(int index, KMetaClass metaClass) {
        long[] existing = (long[]) raw[index];
        if (existing != null) {
            return existing.length;
        }
        return 0;
    }

    @Override
    public long getLongArrayElem(int index, int refIndex, KMetaClass metaClass) {
        long[] existing = (long[]) raw[index];
        if (existing != null) {
            return existing[refIndex];
        } else {
            return KConfig.NULL_LONG;
        }
    }

    @Override
    public long[] getLongArray(int index, KMetaClass p_metaClass) {
        Object previousObj = raw[index];
        if (previousObj != null) {
            return (long[]) previousObj;
        }
        return null;
    }

    @Override
    public boolean addLongToArray(int index, long newRef, KMetaClass metaClass) {
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
            internal_set_dirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeLongToArray(int index, long refToRemove, KMetaClass metaClass) {
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
                    internal_set_dirty();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void clearLongArray(int index, KMetaClass metaClass) {
        raw[index] = null;
    }

    @Override
    public double[] getDoubleArray(int index, KMetaClass metaClass) {
        Object previousObj = raw[index];
        if (previousObj != null) {
            return (double[]) previousObj;
        }
        return null;
    }

    @Override
    public int getDoubleArraySize(int index, KMetaClass metaClass) {
        Object previousObj = raw[index];
        if (previousObj != null) {
            return ((double[]) previousObj).length;
        }
        return 0;
    }

    @Override
    public double getDoubleArrayElem(int index, int arrayIndex, KMetaClass metaClass) {
        double[] res = getDoubleArray(index, metaClass);
        if (Checker.isDefined(res)) {
            return res[arrayIndex];
        }
        return 0;
    }

    @Override
    public void setDoubleArrayElem(int index, int arrayIndex, double valueToInsert, KMetaClass metaClass) {
        double[] res = getDoubleArray(index, metaClass);
        if (Checker.isDefined(res)) {
            res[arrayIndex] = valueToInsert;
            internal_set_dirty();
        }
    }

    @Override
    public void extendDoubleArray(int index, int newSize, KMetaClass metaClass) {
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
            internal_set_dirty();
        }
    }

    @Override
    public void clearDoubleArray(int index, KMetaClass metaClass) {
        raw[index] = null;
    }

    @Override
    public void setPrimitiveType(int index, Object content, KMetaClass p_metaClass) {
        raw[index] = content;
        internal_set_dirty();
    }

    @Override
    public KObjectChunk clone(long p_universe, long p_time, long p_obj, KMetaModel p_metaClass) {
        if (raw == null) {
            return new HeapObjectChunk(p_universe, p_time, p_obj, _space);
        } else {
            Object[] cloned = new Object[raw.length];
            System.arraycopy(raw, 0, cloned, 0, raw.length);
            HeapObjectChunk clonedEntry = new HeapObjectChunk(p_universe, p_time, p_obj, _space);
            clonedEntry.raw = cloned;
            clonedEntry._metaClassIndex = _metaClassIndex;
            clonedEntry.internal_set_dirty();
            return clonedEntry;
        }
    }

    @Override
    public String toJSON(KMetaModel metaModel) {
        KMetaClass metaClass = metaModel.metaClass(_metaClassIndex);
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean isFirst = true;
        KMeta[] metaElements = metaClass.metaElements();
        if (raw != null && metaElements != null) {
            for (int i = 0; i < raw.length && i < metaElements.length; i++) {
                if (raw[i] != null) {
                    if (isFirst) {
                        builder.append("\"");
                        isFirst = false;
                    } else {
                        builder.append(",\"");
                    }
                    builder.append(metaElements[i].metaName());
                    builder.append("\":");
                    if (metaElements[i].metaType() == MetaType.ATTRIBUTE) {
                        KMetaAttribute metaAttribute = (KMetaAttribute) metaElements[i];
                        int metaAttId = metaAttribute.attributeTypeId();
                        switch (metaAttId) {
                            case KPrimitiveTypes.STRING_ID:
                                builder.append("\"");
                                builder.append(JsonString.encode((String) raw[i]));
                                builder.append("\"");
                                break;
                            case KPrimitiveTypes.LONG_ID:
                                builder.append(raw[i]);
                                break;
                            case KPrimitiveTypes.CONTINUOUS_ID:
                                builder.append("[");
                                double[] castedArr = (double[]) raw[i];
                                for (int j = 0; j < castedArr.length; j++) {
                                    if (j != 0) {
                                        builder.append(",");
                                    }
                                    builder.append(castedArr[j]);
                                }
                                builder.append("]");
                                break;
                            case KPrimitiveTypes.BOOL_ID:
                                if ((boolean) raw[i]) {
                                    builder.append("1");
                                } else {
                                    builder.append("0");
                                }
                                break;
                            case KPrimitiveTypes.DOUBLE_ID:
                                builder.append(raw[i]);
                                break;
                            case KPrimitiveTypes.INT_ID:
                                builder.append(raw[i]);
                                break;
                            default:
                                if (KPrimitiveTypes.isEnum(metaAttribute.attributeTypeId())) {
                                    Base64.encodeIntToBuffer((int) raw[i], builder);
                                }
                                break;
                        }
                    } else if (metaElements[i].metaType() == MetaType.RELATION) {
                        builder.append("[");
                        long[] castedArr = (long[]) raw[i];
                        for (int j = 0; j < castedArr.length; j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append(castedArr[j]);
                        }
                        builder.append("]");
                    } else if (metaElements[i].metaType() == MetaType.DEPENDENCIES || metaElements[i].metaType() == MetaType.INPUT || metaElements[i].metaType() == MetaType.OUTPUT) {
                        builder.append("[");
                        double[] castedArr = (double[]) raw[i];
                        for (int j = 0; j < castedArr.length; j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append(castedArr[j]);
                        }
                        builder.append("]");
                    }
                }
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private void internal_set_dirty() {
        if (_space != null) {
            if ((_flags.get() & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
                _space.declareDirty(this);
                //the synchronization risk is minim here, at worse the object will be saved twice for the next iteration
                setFlags(KChunkFlags.DIRTY_BIT, 0);
            }
        } else {
            setFlags(KChunkFlags.DIRTY_BIT, 0);
        }
    }

    @Override
    public long getFlags() {
        return _flags.get();
    }

    @Override
    public void setFlags(long bitsToEnable, long bitsToDisable) {
        long val;
        long nval;
        do {
            val = _flags.get();
            nval = val & ~bitsToDisable | bitsToEnable;
        } while (!_flags.compareAndSet(val, nval));
    }

    @Override
    public long universe() {
        return this._universe;
    }

    @Override
    public long time() {
        return this._time;
    }

    @Override
    public long obj() {
        return this._obj;
    }


    @Override
    public long[] dependencies() {
        return this._dependencies.get();
    }

    @Override
    public void addDependency(long universe, long time, long uuid) {
        long[] previousVal;
        long[] newVal;
        do {
            previousVal = _dependencies.get();
            if (previousVal == null) {
                newVal = new long[]{universe, time, uuid};
            } else {
                newVal = new long[previousVal.length + 3];
                int previousLength = previousVal.length;
                System.arraycopy(previousVal, 0, newVal, 0, previousLength);
                newVal[previousLength] = universe;
                newVal[previousLength + 1] = time;
                newVal[previousLength + 2] = uuid;
            }
        } while (!_dependencies.compareAndSet(previousVal, newVal));
    }

}

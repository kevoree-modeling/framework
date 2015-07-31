package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.format.json.JsonObjectReader;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.chunk.KMemoryChunk;
import org.kevoree.modeling.memory.storage.KMemoryElementTypes;
import org.kevoree.modeling.memory.storage.impl.OffHeapMemoryStorage;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.meta.impl.MetaAttribute;
import org.kevoree.modeling.meta.impl.MetaReference;
import org.kevoree.modeling.util.maths.Base64;
import sun.misc.Unsafe;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @ignore ts
 * OffHeap implementation of KMemoryChunk
 * - Memory structure: |meta class index  |counter    |dirty    |raw     |
 * -                   |(4 byte)          |(4 byte)   |(1 byte) |(x byte)|
 */
public class OffHeapMemoryChunk implements KMemoryChunk, KOffHeapMemoryElement {
    private static final Unsafe UNSAFE = getUnsafe();

    private OffHeapMemoryStorage storage;
    private long universe, time, obj;

    private static final int ATT_META_CLASS_INDEX_LEN = 4;
    private static final int ATT_COUNTER_LEN = 4;
    private static final int ATT_DIRTY_LEN = 1;

    private static final int OFFSET_META_CLASS_INDEX = 0;
    private static final int OFFSET_COUNTER = OFFSET_META_CLASS_INDEX + ATT_META_CLASS_INDEX_LEN;
    private static final int OFFSET_DIRTY = OFFSET_COUNTER + ATT_COUNTER_LEN;
    private static final int OFFSET_RAW = OFFSET_DIRTY + 1;

    private static final int BASE_SEGMENT_SIZE = ATT_META_CLASS_INDEX_LEN + ATT_COUNTER_LEN + ATT_DIRTY_LEN;

    private static final int BYTE = 8;

    // native pointer to the start of the memory chunk
    private long _start_address;
    private int _allocated_segments = 0;

    private int internal_size_of_raw_segment(KMetaClass metaClass) {
        int rawSegment = 0;

        for (int i = 0; i < metaClass.metaElements().length; i++) {
            KMeta meta = metaClass.metaElements()[i];
            rawSegment += internal_size_of(meta.index(), metaClass);
        }
        return rawSegment;
    }

    private int internal_size_of(int index, KMetaClass metaClass) {
        KMeta meta = metaClass.meta(index);

        int size = 0;
        if (meta.metaType().equals(MetaType.ATTRIBUTE)) {
            KMetaAttribute metaAttribute = (KMetaAttribute) meta;

            if (metaAttribute.attributeType() == KPrimitiveTypes.STRING) {
                size = 8; // reserve space for a native pointer
            } else if (metaAttribute.attributeType() == KPrimitiveTypes.LONG) {
                size = 8;
            } else if (metaAttribute.attributeType() == KPrimitiveTypes.INT) {
                size = 4;
            } else if (metaAttribute.attributeType() == KPrimitiveTypes.BOOL) {
                size = 1;
            } else if (metaAttribute.attributeType() == KPrimitiveTypes.DOUBLE) {
                size = 8;
            } else if (metaAttribute.attributeType() == KPrimitiveTypes.CONTINUOUS) {
                size = 8; // native pointer to the double[]
            }
        } else if (meta.metaType().equals(MetaType.REFERENCE)) {
            size = 8;
        }

        return size;
    }

    private long internal_ptr_raw_for_index(int index, KMetaClass metaClass) {
        int offset = 0;
        for (int i = 0; i < metaClass.metaElements().length; i++) {
            KMeta meta = metaClass.metaElements()[i];

            if (meta.index() < index) {
                if (meta.metaType().equals(MetaType.ATTRIBUTE) || meta.metaType().equals(MetaType.REFERENCE)) {
                    offset += internal_size_of(index, metaClass);
                }
            }
        }
        return _start_address + OFFSET_RAW + offset;
    }

    @Override
    public final KMemoryChunk clone(KMetaModel metaModel) {
        // TODO for now it is a deep copy, in the future a shallow copy would be more efficient (attention for the free)

        KMetaClass metaClass = metaModel.metaClass(UNSAFE.getInt(_start_address + OFFSET_META_CLASS_INDEX));

        OffHeapMemoryChunk clonedEntry = new OffHeapMemoryChunk();
        int baseSegment = BASE_SEGMENT_SIZE;
        int modifiedIndexSegment = metaClass.metaElements().length;
        int rawSegment = internal_size_of_raw_segment(metaClass);
        int cloneBytes = baseSegment + modifiedIndexSegment + rawSegment;

        long _clone_start_address = UNSAFE.allocateMemory(cloneBytes);
        clonedEntry._allocated_segments++;
        clonedEntry._start_address = _clone_start_address;
        UNSAFE.copyMemory(this._start_address, clonedEntry._start_address, cloneBytes);
        // strings and references
        for (int i = 0; i < metaClass.metaElements().length; i++) {
            KMeta meta = metaClass.metaElements()[i];
            if (meta.metaType().equals(MetaType.ATTRIBUTE)) {
                KMetaAttribute metaAttribute = (KMetaAttribute) meta;
                if (metaAttribute.attributeType() == KPrimitiveTypes.STRING) {
                    long clone_ptr = clonedEntry.internal_ptr_raw_for_index(metaAttribute.index(), metaClass);
                    if (UNSAFE.getLong(clone_ptr) != 0) {
                        long clone_ptr_str_segment = UNSAFE.getLong(clone_ptr);
                        if (clone_ptr_str_segment != 0) {
                            // copy the chunk
                            int str_size = UNSAFE.getInt(clone_ptr_str_segment);
                            int bytes = 4 + str_size * BYTE;
                            long new_ref_segment = UNSAFE.allocateMemory(bytes);
                            clonedEntry._allocated_segments++;
                            UNSAFE.copyMemory(clone_ptr_str_segment, new_ref_segment, bytes);

                            UNSAFE.putLong(clone_ptr, new_ref_segment); // update ptr
                        }
                    }
                }
                if (metaAttribute.attributeType() == KPrimitiveTypes.CONTINUOUS) {
                    long clone_ptr = clonedEntry.internal_ptr_raw_for_index(metaAttribute.index(), metaClass);
                    if (UNSAFE.getLong(clone_ptr) != 0) {
                        long clone_ptr_str_segment = UNSAFE.getLong(clone_ptr);
                        if (clone_ptr_str_segment != 0) {
                            // copy the chunk
                            int str_size = UNSAFE.getInt(clone_ptr_str_segment);
                            int bytes = 4 + str_size * BYTE;
                            long new_ref_segment = UNSAFE.allocateMemory(bytes);
                            clonedEntry._allocated_segments++;
                            UNSAFE.copyMemory(clone_ptr_str_segment, new_ref_segment, bytes);
                            UNSAFE.putLong(clone_ptr, new_ref_segment); // update ptr
                        }
                    }
                }

            } else if (meta.metaType().equals(MetaType.REFERENCE)) {
                KMetaReference metaReference = (KMetaReference) meta;
                long clone_ptr = clonedEntry.internal_ptr_raw_for_index(metaReference.index(), metaClass);
                if (UNSAFE.getLong(clone_ptr) != 0) {
                    long clone_ptr_ref_segment = UNSAFE.getLong(clone_ptr);
                    if (clone_ptr_ref_segment != 0) {
                        // copy the chunk
                        int size = UNSAFE.getInt(clone_ptr_ref_segment);
                        int bytes = 4 + size * BYTE;
                        long new_ref_segment = UNSAFE.allocateMemory(bytes);
                        clonedEntry._allocated_segments++;
                        UNSAFE.copyMemory(clone_ptr_ref_segment, new_ref_segment, bytes);
                        UNSAFE.putLong(clone_ptr, new_ref_segment); // update ptr
                    }
                }
            }
        }

        // dirty
        clonedEntry.setDirty();

        return clonedEntry;
    }

    @Override
    public final void setPrimitiveType(int index, Object content, KMetaClass metaClass) {
        try {
            MetaType type = metaClass.meta(index).metaType();
            long ptr = internal_ptr_raw_for_index(index, metaClass);

            // primitive types
            if (type.equals(MetaType.ATTRIBUTE)) {

                if (content instanceof String) {
                    String s = (String) content;
                    int size = s.length();
                    long newSegment = UNSAFE.allocateMemory(4 + size * BYTE); // size + the actual string
                    _allocated_segments++;
                    byte[] bytes = s.getBytes("UTF-8");
                    UNSAFE.putInt(newSegment, size);
                    for (int i = 0; i < bytes.length; i++) {
                        UNSAFE.putByte(newSegment + 4 + i * BYTE, bytes[i]);
                    }
                    UNSAFE.putLong(ptr, newSegment);

                } else if (content instanceof Long) {
                    UNSAFE.putLong(ptr, (Long) content);
                } else if (content instanceof Integer) {
                    UNSAFE.putInt(ptr, (Integer) content);
                } else if (content instanceof Boolean) {
                    UNSAFE.putByte(ptr, (byte) (((boolean) content) ? 1 : 0));
                } else if (content instanceof Short) {
                    UNSAFE.putShort(ptr, (Short) content);
                } else if (content instanceof Double) {
                    UNSAFE.putDouble(ptr, (Double) content);
                } else if (content instanceof Float) {
                    UNSAFE.putFloat(ptr, (Float) content);
                }

                setDirty();
                //UNSAFE.putByte(_start_address + OFFSET_MODIFIED_INDEXES + index, (byte) 1);
            }

        } catch (
                UnsupportedEncodingException e
                )

        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public final long[] getLongArray(int index, KMetaClass metaClass) {
        long[] result = null;

        KMeta meta = metaClass.meta(index);
        long ptr = internal_ptr_raw_for_index(index, metaClass);

        if (meta.metaType().equals(MetaType.REFERENCE)) {
            long ptr_ref_segment = UNSAFE.getLong(ptr);
            if (ptr_ref_segment != 0) {
                int size = UNSAFE.getInt(ptr_ref_segment);
                result = new long[size];
                for (int i = 0; i < size; i++) {
                    result[i] = UNSAFE.getLong(ptr_ref_segment + 4 + i * BYTE);
                }
            }
        }

        return result;
    }

    @Override
    public final boolean addLongToArray(int index, long newRef, KMetaClass metaClass) {
        boolean result = false;

        KMeta meta = metaClass.meta(index);
        long ptr = internal_ptr_raw_for_index(index, metaClass);

        if (meta.metaType().equals(MetaType.REFERENCE)) {
            long ptr_ref_segment = UNSAFE.getLong(ptr);
            long new_ref_ptr;
            if (ptr_ref_segment != 0) {
                int newSize = UNSAFE.getInt(ptr_ref_segment) + 1;
                new_ref_ptr = UNSAFE.reallocateMemory(ptr_ref_segment, 4 + newSize * BYTE);
                UNSAFE.putInt(new_ref_ptr, newSize); // size
                UNSAFE.putLong(new_ref_ptr + 4 + (newSize - 1) * BYTE, newRef); // content

            } else {
                new_ref_ptr = UNSAFE.allocateMemory(4 + 8);
                _allocated_segments++;
                UNSAFE.putInt(new_ref_ptr, 1); // size
                UNSAFE.putLong(new_ref_ptr + 4, newRef); // content
            }
            UNSAFE.putLong(ptr, new_ref_ptr); // update ptr
            result = true;
        }
        return result;
    }

    @Override
    public final boolean removeLongToArray(int index, long ref, KMetaClass metaClass) {
        boolean result = false;

        KMeta meta = metaClass.meta(index);
        long ptr = internal_ptr_raw_for_index(index, metaClass);

        if (meta.metaType().equals(MetaType.REFERENCE)) {
            long ptr_ref_segment = UNSAFE.getLong(ptr);
            if (ptr_ref_segment != 0) {
                int size = UNSAFE.getInt(ptr_ref_segment);
                if (size > 1) {
                    long new_ref_ptr = UNSAFE.allocateMemory((size - 1) * BYTE);
                    _allocated_segments++;
                    int j = 0;
                    for (int i = 0; i < size; i++) {
                        long value = UNSAFE.getLong(ptr_ref_segment + 4 + i * BYTE);
                        if (value != ref) {
                            UNSAFE.putLong(new_ref_ptr + 4 + j * BYTE, value);
                            j++;
                        }
                    }
                    UNSAFE.putInt(new_ref_ptr, j); // setPrimitiveType the new size
                    UNSAFE.freeMemory(ptr_ref_segment); // release the old memory zone
                    _allocated_segments--;
                    UNSAFE.putLong(ptr, new_ref_ptr); // update pointer

                } else {
                    UNSAFE.freeMemory(ptr_ref_segment); // release the old memory zone
                    _allocated_segments--;
                    UNSAFE.putLong(ptr, 0);
                }
                result = true;
            }
        }

        return result;
    }

    @Override
    public final void clearLongArray(int index, KMetaClass metaClass) {
        KMeta meta = metaClass.meta(index);
        long ptr = internal_ptr_raw_for_index(index, metaClass);

        if (meta.metaType().equals(MetaType.REFERENCE)) {
            long ptr_ref_segment = UNSAFE.getLong(ptr);
            if (ptr_ref_segment != 0) {
                UNSAFE.freeMemory(ptr_ref_segment);
                _allocated_segments--;

                UNSAFE.putLong(ptr, 0);
            }
        }
    }

    @Override
    public final double[] getDoubleArray(int index, KMetaClass metaClass) {
        double[] infer = null;
        long ptr = internal_ptr_raw_for_index(index, metaClass);
        long ptr_segment = UNSAFE.getLong(ptr);
        if (ptr_segment != 0) {
            int size = UNSAFE.getInt(ptr_segment);
            infer = new double[size];
            for (int i = 0; i < size; i++) {
                infer[i] = UNSAFE.getDouble(ptr_segment + 4 + i * BYTE);
            }
        }
        return infer;
    }

    @Override
    public final int getDoubleArraySize(int index, KMetaClass metaClass) {
        int size = 0;
        double[] infer = getDoubleArray(index, metaClass);
        if (infer != null) {
            size = infer.length;
        }
        return size;
    }

    @Override
    public final double getDoubleArrayElem(int index, int arrayIndex, KMetaClass metaClass) {
        return getDoubleArray(index, metaClass)[arrayIndex];
    }

    @Override
    public final void setDoubleArrayElem(int index, int arrayIndex, double valueToInsert, KMetaClass metaClass) {
        long ptr = internal_ptr_raw_for_index(index, metaClass);
        long ptr_segment = UNSAFE.getLong(ptr);

        if (ptr_segment == 0) {
            throw new IndexOutOfBoundsException();
        }
        int size = UNSAFE.getInt(ptr_segment);
        if (index > size) {
            throw new IndexOutOfBoundsException();
        }

        UNSAFE.putDouble(ptr_segment + 4 + arrayIndex * BYTE, valueToInsert);

        setDirty();
    }

    @Override
    public final void extendDoubleArray(int index, int newSize, KMetaClass metaClass) {
        long ptr = internal_ptr_raw_for_index(index, metaClass);
        long ptr_segment = UNSAFE.getLong(ptr);

        long new_ptr_segment;
        if (ptr_segment != 0) {
            new_ptr_segment = UNSAFE.reallocateMemory(ptr_segment, 4 + newSize * BYTE);
        } else {
            new_ptr_segment = UNSAFE.allocateMemory(4 + newSize * BYTE);
            _allocated_segments++;
        }
        UNSAFE.putInt(new_ptr_segment, newSize); // update size
        UNSAFE.putLong(ptr, new_ptr_segment); // update pointer
        setDirty();

    }

    @Override
    public final Object getPrimitiveType(int index, KMetaClass metaClass) {
        Object result = null;

        try {
            KMeta meta = metaClass.meta(index);
            long ptr = internal_ptr_raw_for_index(index, metaClass);

            if (meta.metaType().equals(MetaType.ATTRIBUTE)) {
                KMetaAttribute metaAttribute = (KMetaAttribute) meta;

                if (metaAttribute.attributeType() == KPrimitiveTypes.STRING) {
                    long ptr_str_segment = UNSAFE.getLong(ptr);
                    if (ptr_str_segment != 0) {
                        int size = UNSAFE.getInt(ptr_str_segment);
                        byte[] bytes = new byte[size];
                        for (int i = 0; i < size; i++) {
                            bytes[i] = UNSAFE.getByte(ptr_str_segment + 4 + i * BYTE);
                        }
                        result = new String(bytes, "UTF-8");
                    }

                } else if (metaAttribute.attributeType() == KPrimitiveTypes.LONG) {
                    result = UNSAFE.getLong(ptr);
                } else if (metaAttribute.attributeType() == KPrimitiveTypes.INT) {
                    result = UNSAFE.getInt(ptr);
                } else if (metaAttribute.attributeType() == KPrimitiveTypes.BOOL) {
                    result = UNSAFE.getByte(ptr) != 0;
                } else if (metaAttribute.attributeType() == KPrimitiveTypes.DOUBLE) {
                    result = UNSAFE.getDouble(ptr);
                } else if (metaAttribute.attributeType() == KPrimitiveTypes.CONTINUOUS) {
                    result = getDoubleArray(index, metaClass);
                }
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private final void initMetaClass(KMetaClass metaClass) {
        int baseSegment = BASE_SEGMENT_SIZE;
        int modifiedIndexSegment = metaClass.metaElements().length;
        int rawSegment = internal_size_of_raw_segment(metaClass);

        int bytes = baseSegment + modifiedIndexSegment + rawSegment;

        _start_address = UNSAFE.allocateMemory(bytes);
        _allocated_segments++;
        UNSAFE.setMemory(_start_address, bytes, (byte) 0);
        UNSAFE.putInt(_start_address + OFFSET_META_CLASS_INDEX, metaClass.index());

        if (this.storage != null) {
            storage.notifyRealloc(_start_address, this.universe, this.time, this.obj);
        }
    }


    @Override
    public final int metaClassIndex() {
        return UNSAFE.getInt(_start_address + OFFSET_META_CLASS_INDEX);
    }

    @Override
    public final boolean isDirty() {
        return UNSAFE.getByte(_start_address + OFFSET_DIRTY) != 0;
    }

    @Override
    public final String toJSON(KMetaModel metaModel) {
        KMetaClass metaClass = metaModel.metaClass(metaClassIndex());
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean isFirst = true;
        KMeta[] metaElements = metaClass.metaElements();
        if (_start_address != 0 && metaElements != null) {
            for (int i = 0; i < metaElements.length; i++) {
                KMeta meta = metaElements[i];
                if (meta.metaType().equals(MetaType.ATTRIBUTE)) {
                    MetaAttribute metaAttribute = (MetaAttribute) meta;
                    if (metaAttribute.attributeType() != KPrimitiveTypes.CONTINUOUS) {
                        Object o = getPrimitiveType(meta.index(), metaClass);
                        if (o != null) {
                            if (isFirst) {
                                builder.append("\"");
                                isFirst = false;
                            } else {
                                builder.append(",\"");
                            }
                            builder.append(metaAttribute.metaName());
                            builder.append("\":");

                            if (o instanceof String) {
                                builder.append("\"");
                                builder.append(JsonString.encode((String) o));
                                builder.append("\"");
                            } else {
                                builder.append(o.toString());
                            }
                        }

                    } else {
                        double[] o = getDoubleArray(meta.index(), metaClass);
                        if (o != null) {
                            builder.append(",\"");
                            builder.append(metaAttribute.metaName());
                            builder.append("\":");

                            builder.append("[");
                            double[] castedArr = (double[]) o;
                            for (int j = 0; j < castedArr.length; j++) {
                                if (j != 0) {
                                    builder.append(",");
                                }
                                builder.append(castedArr[j]);
                            }
                            builder.append("]");
                        }
                    }

                } else if (meta.metaType().equals(MetaType.REFERENCE)) {
                    MetaReference metaReference = (MetaReference) meta;
                    long[] o = getLongArray(metaReference.index(), metaClass);
                    if (o != null) {
                        builder.append(",\"");
                        builder.append(metaElements[i].metaName());
                        builder.append("\":");

                        builder.append("[");
                        long[] castedArr = (long[]) o;
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

    @Override
    public String serialize(KMetaModel metaModel) {
        KMetaClass metaClass = metaModel.metaClass(metaClassIndex());
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean isFirst = true;
        KMeta[] metaElements = metaClass.metaElements();
        if (_start_address != 0 && metaElements != null) {
            for (int i = 0; i < metaElements.length; i++) {
                KMeta meta = metaElements[i];
                if (metaElements[i].metaType() == MetaType.ATTRIBUTE) {
                    KMetaAttribute metaAttribute = (KMetaAttribute) metaElements[i];
                    Object o = getPrimitiveType(meta.index(), metaClass);
                    if (o != null) {
                        if (isFirst) {
                            builder.append("\"");
                            isFirst = false;
                        } else {
                            builder.append(",\"");
                        }
                        builder.append(metaElements[i].metaName());
                        builder.append("\":");
                        if (metaAttribute.attributeType() == KPrimitiveTypes.STRING) {
                            builder.append("\"");
                            builder.append(JsonString.encode((String) o));
                            builder.append("\"");
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.LONG) {
                            builder.append("\"");
                            Base64.encodeLongToBuffer((long) o, builder);
                            builder.append("\"");
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.CONTINUOUS) {
                            builder.append("[");
                            double[] castedArr = (double[]) o;
                            for (int j = 0; j < castedArr.length; j++) {
                                if (j != 0) {
                                    builder.append(",");
                                }
                                builder.append("\"");
                                Base64.encodeDoubleToBuffer(castedArr[j], builder);
                                builder.append("\"");
                            }
                            builder.append("]");
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.BOOL) {
                            if ((boolean) o) {
                                builder.append("1");
                            } else {
                                builder.append("0");
                            }
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.DOUBLE) {
                            builder.append("\"");
                            Base64.encodeDoubleToBuffer((double) o, builder);
                            builder.append("\"");
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.INT) {
                            builder.append("\"");
                            Base64.encodeIntToBuffer((int) o, builder);
                            builder.append("\"");
                        } else if (metaAttribute.attributeType().isEnum()) {
                            Base64.encodeIntToBuffer((int) o, builder);
                        }
                    }
                } else if (metaElements[i].metaType() == MetaType.REFERENCE) {
                    long[] o = getLongArray(meta.index(), metaClass);
                    if (o != null) {
                        if (isFirst) {
                            builder.append("\"");
                            isFirst = false;
                        } else {
                            builder.append(",\"");
                        }
                        builder.append(metaElements[i].metaName());
                        builder.append("\":");
                        builder.append("[");
                        for (int j = 0; j < o.length; j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append("\"");
                            Base64.encodeLongToBuffer(o[j], builder);
                            builder.append("\"");
                        }
                        builder.append("]");
                    }
                } else if (metaElements[i].metaType() == MetaType.DEPENDENCIES || metaElements[i].metaType() == MetaType.INPUT || metaElements[i].metaType() == MetaType.OUTPUT) {
                    double[] o = getDoubleArray(meta.index(), metaClass);
                    if (o != null) {
                        if (isFirst) {
                            builder.append("\"");
                            isFirst = false;
                        } else {
                            builder.append(",\"");
                        }
                        builder.append(metaElements[i].metaName());
                        builder.append("\":");
                        builder.append("[");
                        for (int j = 0; j < o.length; j++) {
                            if (j != 0) {
                                builder.append(",");
                            }
                            builder.append("\"");
                            Base64.encodeDoubleToBuffer(o[j], builder);
                            builder.append("\"");
                        }
                        builder.append("]");
                    }
                }
            }
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public final void init(String payload, KMetaModel metaModel, int metaClassIndex) {
        KMetaClass metaClass = metaModel.metaClass(metaClassIndex);
        initMetaClass(metaClass);
        UNSAFE.putInt(_start_address + OFFSET_META_CLASS_INDEX, metaClassIndex);

        if (payload != null) {
            JsonObjectReader objectReader = new JsonObjectReader();
            objectReader.parseObject(payload);
            //KMetaClass metaClass = metaModel.metaClass(UNSAFE.getInt(_start_address + OFFSET_META_CLASS_INDEX));

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
                            converted = Base64.decodeToLong((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.INT) {
                            converted = Base64.decodeToInt((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.BOOL) {
                            converted = Boolean.parseBoolean((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.DOUBLE) {
                            converted = Base64.decodeToDouble((String) insideContent);
                        } else if (metaAttribute.attributeType() == KPrimitiveTypes.CONTINUOUS) {
                            String[] plainRawSet = objectReader.getAsStringArray(metaKeys[i]);
                            double[] convertedRaw = new double[plainRawSet.length];
                            for (int l = 0; l < plainRawSet.length; l++) {
                                try {
                                    convertedRaw[l] = Base64.decodeToDouble(plainRawSet[l]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            converted = convertedRaw;
                        }

                        if (metaAttribute.attributeType() == KPrimitiveTypes.CONTINUOUS) {
                            double[] infer = (double[]) converted;
                            extendDoubleArray(metaAttribute.index(), infer.length, metaClass);
                            for (int k = 0; k < infer.length; k++) {
                                setDoubleArrayElem(metaAttribute.index(), k, infer[k], metaClass);
                            }
                        } else {
                            setPrimitiveType(metaAttribute.index(), converted, metaClass);
                        }

                    } else if (metaElement != null && metaElement instanceof KMetaReference) {
                        try {
                            String[] plainRawSet = objectReader.getAsStringArray(metaKeys[i]);
                            long[] convertedRaw = new long[plainRawSet.length];
                            for (int l = 0; l < plainRawSet.length; l++) {
                                try {
                                    convertedRaw[l] = Base64.decodeToLong(plainRawSet[l]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            for (int k = 0; k < convertedRaw.length; k++) {
                                addLongToArray(metaElement.index(), convertedRaw[k], metaClass);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (metaElement != null && (metaElement.metaType().equals(MetaType.DEPENDENCIES) || metaElement.metaType().equals(MetaType.INPUT) || metaElement.metaType().equals(MetaType.OUTPUT))) {
                        try {
                            String[] plainRawSet = objectReader.getAsStringArray(metaKeys[i]);
                            double[] convertedRaw = new double[plainRawSet.length];
                            for (int l = 0; l < plainRawSet.length; l++) {
                                try {
                                    convertedRaw[l] = Base64.decodeToDouble(plainRawSet[l]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            for (int k = 0; k < convertedRaw.length; k++) {
                                setDoubleArrayElem(metaElement.index(), k, convertedRaw[k], metaClass);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }

        // should not be dirty  after unserialization
        UNSAFE.putByte(_start_address + OFFSET_DIRTY, (byte) 0);


    }

    @Override
    public final void setClean(KMetaModel model) {
        KMetaClass metaClass = model.metaClass(UNSAFE.getInt(_start_address + OFFSET_META_CLASS_INDEX));
        UNSAFE.putByte(_start_address + OFFSET_DIRTY, (byte) 0);
        //UNSAFE.setMemory(_start_address + OFFSET_MODIFIED_INDEXES, metaClass.metaElements().length, (byte) 0);
    }

    @Override
    public final void setDirty() {
        UNSAFE.putByte(_start_address + OFFSET_DIRTY, (byte) 1);
    }

    @Override
    public long getFlags() {
        return 0; // FIXME
    }

    @Override
    public void setFlags(long flagsToEnable, long flagsToDisable) {
        // FIXME
    }

    @Override
    public KMemoryElement next() {
        return null; // FIXME
    }

    @Override
    public void insertInto(AtomicReference<KMemoryElement> list) {
        // FIXME
    }


    @Override
    public final int counter() {
        return UNSAFE.getInt(_start_address + OFFSET_COUNTER);
    }

    @Override
    public final void inc() {
        int c = UNSAFE.getInt(_start_address + OFFSET_COUNTER);
        UNSAFE.putInt(_start_address + OFFSET_COUNTER, c + 1);
    }

    @Override
    public final void dec() {
        int c = UNSAFE.getInt(_start_address + OFFSET_COUNTER);
        UNSAFE.putInt(_start_address + OFFSET_COUNTER, c - 1);
    }

    @Override
    public final void free(KMetaModel metaModel) {
        KMetaClass metaClass = metaModel.metaClass(UNSAFE.getInt(_start_address + OFFSET_META_CLASS_INDEX));

        for (int i = 0; i < metaClass.metaElements().length; i++) {
            KMeta meta = metaClass.metaElements()[i];

            if (meta.metaType().equals(MetaType.ATTRIBUTE)) {
                KMetaAttribute metaAttribute = (KMetaAttribute) meta;
                if (metaAttribute.attributeType() == KPrimitiveTypes.STRING) {
                    long ptr = internal_ptr_raw_for_index(metaAttribute.index(), metaClass);
                    long ptr_str_segment = UNSAFE.getLong(ptr);
                    if (ptr_str_segment != 0) {
                        UNSAFE.freeMemory(ptr_str_segment);
                        _allocated_segments--;
                    }
                }
                if (metaAttribute.attributeType() == KPrimitiveTypes.CONTINUOUS) {
                    long ptr = internal_ptr_raw_for_index(metaAttribute.index(), metaClass);
                    long ptr_segment = UNSAFE.getLong(ptr);
                    if (ptr_segment != 0) {
                        UNSAFE.freeMemory(ptr_segment);
                        _allocated_segments--;
                    }
                }
            } else if (meta.metaType().equals(MetaType.REFERENCE)) {
                KMetaReference metaReference = (KMetaReference) meta;
                long ptr = internal_ptr_raw_for_index(metaReference.index(), metaClass);
                long ptr_str_segment = UNSAFE.getLong(ptr);
                if (ptr_str_segment != 0) {
                    UNSAFE.freeMemory(ptr_str_segment);
                    _allocated_segments--;
                }
            }
        }

        UNSAFE.freeMemory(_start_address);
        _allocated_segments--;

//        if (_allocated_segments != 0) {
//            throw new RuntimeException("OffHeap Memory Management Exception: more segments allocated than freed");
//        }
    }

    @Override
    public short type() {
        return KMemoryElementTypes.CHUNK;
    }


    @Override
    public final int getLongArraySize(int index, KMetaClass metaClass) {
        int size = 0;
        long[] refs = getLongArray(index, metaClass);
        if (refs != null) {
            size = refs.length;
        }
        return size;
    }

    @Override
    public final long getLongArrayElem(int index, int refIndex, KMetaClass metaClass) {
        long elem = KConfig.NULL_LONG;
        long[] refs = getLongArray(index, metaClass);
        if (refs != null) {
            elem = refs[refIndex];
        }
        return elem;
    }


    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);

        } catch (Exception e) {
            throw new RuntimeException("ERROR: unsafe operations are not available", e);
        }
    }

    @Override
    public final long getMemoryAddress() {
        return _start_address;
    }

    @Override
    public final void setMemoryAddress(long address) {
        _start_address = address;
        if (this.storage != null) {
            storage.notifyRealloc(_start_address, this.universe, this.time, this.obj);
        }
    }

    @Override
    public void setStorage(OffHeapMemoryStorage storage, long universe, long time, long obj) {
        this.storage = storage;
        this.universe = universe;
        this.time = time;
        this.obj = obj;
    }

}

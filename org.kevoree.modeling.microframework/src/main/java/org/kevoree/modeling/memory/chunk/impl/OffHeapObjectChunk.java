package org.kevoree.modeling.memory.chunk.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.format.json.JsonObjectReader;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.KOffHeapChunk;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkTypes;
import org.kevoree.modeling.memory.space.impl.OffHeapChunkSpace;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.meta.impl.MetaAttribute;
import org.kevoree.modeling.meta.impl.MetaReference;
import org.kevoree.modeling.util.maths.Base64;
import sun.misc.Unsafe;

import java.io.UnsupportedEncodingException;

/**
 * @ignore ts
 * OffHeap implementation of KObjectChunk
 * - Memory structure: |meta class index  |counter    |flags    |raw     |
 * -                   |(4 byte)          |(4 byte)   |(8 byte) |(x byte)|
 */
public class OffHeapObjectChunk implements KObjectChunk, KOffHeapChunk {
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    private OffHeapChunkSpace _space;
    private long _universe, _time, _obj;

    // native pointer to the start of the memory chunk
    private volatile long _start_address;
    private int _allocated_segments = 0;

    // constants for off-heap memory layout
    private static final int ATT_META_CLASS_INDEX_LEN = 4;
    private static final int ATT_COUNTER_LEN = 4;
    private static final int ATT_FLAGS_LEN = 8;

    private static final int OFFSET_META_CLASS_INDEX = 0;
    private static final int OFFSET_COUNTER = OFFSET_META_CLASS_INDEX + ATT_META_CLASS_INDEX_LEN;
    private static final int OFFSET_FLAGS = OFFSET_COUNTER + ATT_COUNTER_LEN;
    private static final int OFFSET_RAW = OFFSET_FLAGS + ATT_FLAGS_LEN;

    private static final int BASE_SEGMENT_SIZE = ATT_META_CLASS_INDEX_LEN + ATT_COUNTER_LEN + ATT_FLAGS_LEN;

    private static final int BYTE = 8;

    public OffHeapObjectChunk(OffHeapChunkSpace p_space, long p_universe, long p_time, long p_obj) {
        super();
        this._space = p_space;
        this._universe = p_universe;
        this._time = p_time;
        this._obj = p_obj;
    }

    private int sizeOfRawSegment(KMetaClass p_metaClass) {
        int rawSegment = 0;

        for (int i = 0; i < p_metaClass.metaElements().length; i++) {
            KMeta meta = p_metaClass.metaElements()[i];
            rawSegment += sizeOf(meta.index(), p_metaClass);
        }
        return rawSegment;
    }

    private int sizeOf(int p_index, KMetaClass p_metaClass) {
        KMeta meta = p_metaClass.meta(p_index);

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

    private long rawPointerForIndex(int p_index, KMetaClass p_metaClass) {
        int offset = 0;
        for (int i = 0; i < p_metaClass.metaElements().length; i++) {
            KMeta meta = p_metaClass.metaElements()[i];

            if (meta.index() < p_index) {
                if (meta.metaType().equals(MetaType.ATTRIBUTE) || meta.metaType().equals(MetaType.REFERENCE)) {
                    offset += sizeOf(p_index, p_metaClass);
                }
            }
        }
        return _start_address + OFFSET_RAW + offset;
    }

    @Override
    public final KObjectChunk clone(long p_universe, long p_time, long p_obj, KMetaModel p_metaModel) {
        // TODO for now it is a deep copy, in the future a shallow copy would be more efficient (attention for the free)
        KMetaClass metaClass = p_metaModel.metaClass(UNSAFE.getInt(_start_address + OFFSET_META_CLASS_INDEX));

        OffHeapObjectChunk clonedEntry = new OffHeapObjectChunk(this._space, p_universe, p_time, p_obj);
        int baseSegment = BASE_SEGMENT_SIZE;
        int modifiedIndexSegment = metaClass.metaElements().length;
        int rawSegment = sizeOfRawSegment(metaClass);
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
                    long clone_ptr = clonedEntry.rawPointerForIndex(metaAttribute.index(), metaClass);
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
                    long clone_ptr = clonedEntry.rawPointerForIndex(metaAttribute.index(), metaClass);
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
                long clone_ptr = clonedEntry.rawPointerForIndex(metaReference.index(), metaClass);
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

    private void setDirty() {
        if (_space != null) {
            if ((UNSAFE.getLong(this._start_address + OFFSET_FLAGS) & KChunkFlags.DIRTY_BIT) == KChunkFlags.DIRTY_BIT) {
                _space.declareDirty(this);
                //the synchronization risk is minimal here, at worse the object will be saved twice for the next iteration
                setFlags(KChunkFlags.DIRTY_BIT, 0);
            }
        } else {
            setFlags(KChunkFlags.DIRTY_BIT, 0);
        }
    }


    @Override
    public final void setPrimitiveType(int p_index, Object p_content, KMetaClass p_metaClass) {
        try {
            MetaType type = p_metaClass.meta(p_index).metaType();
            long ptr = rawPointerForIndex(p_index, p_metaClass);

            // primitive types
            if (type.equals(MetaType.ATTRIBUTE)) {

                if (p_content instanceof String) {
                    String s = (String) p_content;
                    int size = s.length();
                    long newSegment = UNSAFE.allocateMemory(4 + size * BYTE); // size + the actual string
                    _allocated_segments++;
                    byte[] bytes = s.getBytes("UTF-8");
                    UNSAFE.putInt(newSegment, size);
                    for (int i = 0; i < bytes.length; i++) {
                        UNSAFE.putByte(newSegment + 4 + i * BYTE, bytes[i]);
                    }
                    UNSAFE.putLong(ptr, newSegment);

                } else if (p_content instanceof Long) {
                    UNSAFE.putLong(ptr, (Long) p_content);
                } else if (p_content instanceof Integer) {
                    UNSAFE.putInt(ptr, (Integer) p_content);
                } else if (p_content instanceof Boolean) {
                    UNSAFE.putByte(ptr, (byte) (((boolean) p_content) ? 1 : 0));
                } else if (p_content instanceof Short) {
                    UNSAFE.putShort(ptr, (Short) p_content);
                } else if (p_content instanceof Double) {
                    UNSAFE.putDouble(ptr, (Double) p_content);
                } else if (p_content instanceof Float) {
                    UNSAFE.putFloat(ptr, (Float) p_content);
                }

                setDirty();
            }

        } catch (
                UnsupportedEncodingException e
                )

        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public final long[] getLongArray(int p_index, KMetaClass p_metaClass) {
        long[] result = null;

        KMeta meta = p_metaClass.meta(p_index);
        long ptr = rawPointerForIndex(p_index, p_metaClass);

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
    public final boolean addLongToArray(int p_index, long p_newRef, KMetaClass p_metaClass) {
        boolean result = false;

        KMeta meta = p_metaClass.meta(p_index);
        long ptr = rawPointerForIndex(p_index, p_metaClass);

        if (meta.metaType().equals(MetaType.REFERENCE)) {
            long ptr_ref_segment = UNSAFE.getLong(ptr);
            long new_ref_ptr;
            if (ptr_ref_segment != 0) {
                int newSize = UNSAFE.getInt(ptr_ref_segment) + 1;
                new_ref_ptr = UNSAFE.reallocateMemory(ptr_ref_segment, 4 + newSize * BYTE);
                UNSAFE.putInt(new_ref_ptr, newSize); // size
                UNSAFE.putLong(new_ref_ptr + 4 + (newSize - 1) * BYTE, p_newRef); // content

            } else {
                new_ref_ptr = UNSAFE.allocateMemory(4 + 8);
                _allocated_segments++;
                UNSAFE.putInt(new_ref_ptr, 1); // size
                UNSAFE.putLong(new_ref_ptr + 4, p_newRef); // content
            }
            UNSAFE.putLong(ptr, new_ref_ptr); // update ptr
            setDirty();
            result = true;
        }
        return result;
    }

    @Override
    public final boolean removeLongToArray(int p_index, long p_ref, KMetaClass p_metaClass) {
        boolean result = false;

        KMeta meta = p_metaClass.meta(p_index);
        long ptr = rawPointerForIndex(p_index, p_metaClass);

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
                        if (value != p_ref) {
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
                setDirty();
                result = true;
            }
        }

        return result;
    }

    @Override
    public final void clearLongArray(int p_index, KMetaClass p_metaClass) {
        KMeta meta = p_metaClass.meta(p_index);
        long ptr = rawPointerForIndex(p_index, p_metaClass);

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
    public final double[] getDoubleArray(int p_index, KMetaClass p_metaClass) {
        double[] infer = null;
        long ptr = rawPointerForIndex(p_index, p_metaClass);
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
    public final int getDoubleArraySize(int p_index, KMetaClass p_metaClass) {
        int size = 0;
        double[] infer = getDoubleArray(p_index, p_metaClass);
        if (infer != null) {
            size = infer.length;
        }
        return size;
    }

    @Override
    public final double getDoubleArrayElem(int p_index, int p_arrayIndex, KMetaClass p_metaClass) {
        return getDoubleArray(p_index, p_metaClass)[p_arrayIndex];
    }

    @Override
    public final void setDoubleArrayElem(int p_index, int p_arrayIndex, double valueToInsert, KMetaClass p_metaClass) {
        long ptr = rawPointerForIndex(p_index, p_metaClass);
        long ptr_segment = UNSAFE.getLong(ptr);

        if (ptr_segment == 0) {
            throw new IndexOutOfBoundsException();
        }
        int size = UNSAFE.getInt(ptr_segment);
        if (p_index > size) {
            throw new IndexOutOfBoundsException();
        }

        UNSAFE.putDouble(ptr_segment + 4 + p_arrayIndex * BYTE, valueToInsert);
        setDirty();
    }

    @Override
    public final void extendDoubleArray(int p_index, int p_newSize, KMetaClass p_metaClass) {
        long ptr = rawPointerForIndex(p_index, p_metaClass);
        long ptr_segment = UNSAFE.getLong(ptr);

        long new_ptr_segment;
        if (ptr_segment != 0) {
            new_ptr_segment = UNSAFE.reallocateMemory(ptr_segment, 4 + p_newSize * BYTE);
        } else {
            new_ptr_segment = UNSAFE.allocateMemory(4 + p_newSize * BYTE);
            _allocated_segments++;
        }
        UNSAFE.putInt(new_ptr_segment, p_newSize); // update size
        UNSAFE.putLong(ptr, new_ptr_segment); // update pointer

        setDirty();

    }

    @Override
    public final Object getPrimitiveType(int p_index, KMetaClass p_metaClass) {
        Object result = null;

        try {
            KMeta meta = p_metaClass.meta(p_index);
            long ptr = rawPointerForIndex(p_index, p_metaClass);

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
                    result = getDoubleArray(p_index, p_metaClass);
                }
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private final void initMetaClass(KMetaClass p_metaClass) {
        int baseSegment = BASE_SEGMENT_SIZE;
        int modifiedIndexSegment = p_metaClass.metaElements().length;
        int rawSegment = sizeOfRawSegment(p_metaClass);

        int bytes = baseSegment + modifiedIndexSegment + rawSegment;

        _start_address = UNSAFE.allocateMemory(bytes);
        _allocated_segments++;
        UNSAFE.setMemory(_start_address, bytes, (byte) 0);
        UNSAFE.putInt(_start_address + OFFSET_META_CLASS_INDEX, p_metaClass.index());

        if (this._space != null) {
            this._space.notifyRealloc(_start_address, this._universe, this._time, this._obj);
        }
    }


    @Override
    public final int metaClassIndex() {
        return UNSAFE.getInt(_start_address + OFFSET_META_CLASS_INDEX);
    }


    @Override
    public final String toJSON(KMetaModel p_metaModel) {
        KMetaClass metaClass = p_metaModel.metaClass(metaClassIndex());
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
    public String serialize(KMetaModel p_metaModel) {
        KMetaClass metaClass = p_metaModel.metaClass(metaClassIndex());
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
    public final void init(String p_payload, KMetaModel p_metaModel, int p_metaClassIndex) {
        KMetaClass metaClass = p_metaModel.metaClass(p_metaClassIndex);
        initMetaClass(metaClass);
        UNSAFE.putInt(_start_address + OFFSET_META_CLASS_INDEX, p_metaClassIndex);

        if (p_payload != null) {
            JsonObjectReader objectReader = new JsonObjectReader();
            objectReader.parseObject(p_payload);
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
//        UNSAFE.putByte(_start_address + OFFSET_DIRTY, (byte) 0);


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
    public final void free(KMetaModel p_metaModel) {
        KMetaClass metaClass = p_metaModel.metaClass(UNSAFE.getInt(_start_address + OFFSET_META_CLASS_INDEX));

        for (int i = 0; i < metaClass.metaElements().length; i++) {
            KMeta meta = metaClass.metaElements()[i];

            if (meta.metaType().equals(MetaType.ATTRIBUTE)) {
                KMetaAttribute metaAttribute = (KMetaAttribute) meta;
                if (metaAttribute.attributeType() == KPrimitiveTypes.STRING) {
                    long ptr = rawPointerForIndex(metaAttribute.index(), metaClass);
                    long ptr_str_segment = UNSAFE.getLong(ptr);
                    if (ptr_str_segment != 0) {
                        UNSAFE.freeMemory(ptr_str_segment);
                        _allocated_segments--;
                    }
                }
                if (metaAttribute.attributeType() == KPrimitiveTypes.CONTINUOUS) {
                    long ptr = rawPointerForIndex(metaAttribute.index(), metaClass);
                    long ptr_segment = UNSAFE.getLong(ptr);
                    if (ptr_segment != 0) {
                        UNSAFE.freeMemory(ptr_segment);
                        _allocated_segments--;
                    }
                }
            } else if (meta.metaType().equals(MetaType.REFERENCE)) {
                KMetaReference metaReference = (KMetaReference) meta;
                long ptr = rawPointerForIndex(metaReference.index(), metaClass);
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
        return KChunkTypes.CHUNK;
    }

    @Override
    public KChunkSpace space() {
        return this._space;
    }

    @Override
    public long getFlags() {
        return UNSAFE.getLong(this._start_address + OFFSET_FLAGS);
    }

    @Override
    public void setFlags(long p_bitsToEnable, long p_bitsToDisable) {
        long expected;
        long updated;
        do {
            expected = UNSAFE.getLong(this._start_address + OFFSET_FLAGS);
            updated = expected & ~p_bitsToDisable | p_bitsToEnable;
        } while (!UNSAFE.compareAndSwapLong(this, this._start_address + OFFSET_FLAGS, expected, updated));
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
    public final int getLongArraySize(int p_index, KMetaClass p_metaClass) {
        int size = 0;
        long[] refs = getLongArray(p_index, p_metaClass);
        if (refs != null) {
            size = refs.length;
        }
        return size;
    }

    @Override
    public final long getLongArrayElem(int p_index, int p_refIndex, KMetaClass p_metaClass) {
        long elem = KConfig.NULL_LONG;
        long[] refs = getLongArray(p_index, p_metaClass);
        if (refs != null) {
            elem = refs[p_refIndex];
        }
        return elem;
    }

    @Override
    public final long memoryAddress() {
        return _start_address;
    }

    @Override
    public final void setMemoryAddress(long p_address) {
        _start_address = p_address;
        if (this._space != null) {
            this._space.notifyRealloc(_start_address, this._universe, this._time, this._obj);
        }
    }

}

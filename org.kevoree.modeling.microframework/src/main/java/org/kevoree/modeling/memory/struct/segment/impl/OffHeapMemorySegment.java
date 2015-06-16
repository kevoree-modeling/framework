package org.kevoree.modeling.memory.struct.segment.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.format.json.JsonFormat;
import org.kevoree.modeling.format.json.JsonObjectReader;
import org.kevoree.modeling.format.json.JsonString;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;
import org.kevoree.modeling.meta.*;
import org.kevoree.modeling.meta.impl.MetaAttribute;
import org.kevoree.modeling.meta.impl.MetaReference;
import sun.misc.Unsafe;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

/**
 * OffHeap implementation of KMemorySegment
 * - Memory structure: |meta class index  |counter    |dirty    |modified indexes        |raw     |
 * -                   |(4 byte)          |(4 byte)   |(1 byte) |(meta class elem byte)  |(x byte)|
 */
public class OffHeapMemorySegment implements KMemorySegment, KOffHeapMemoryElement {
    private static final Unsafe UNSAFE = getUnsafe();

    // native pointer to the start of the memory segment
    private long _start_address;
    private int _allocated_segments = 0;

    private long internal_ptr_metaClassIndex() {
        return _start_address;
    }

    private long internal_ptr_counter() {
        return internal_ptr_metaClassIndex() + 4;
    }

    private long internal_ptr_dirty() {
        return internal_ptr_counter() + 4;
    }

    private long internal_ptr_modifiedIndexes() {
        return internal_ptr_dirty() + 1;
    }

    private long internal_ptr_raw(KMetaClass metaClass) {
        return internal_ptr_modifiedIndexes() + metaClass.metaElements().length;
    }

    private int internal_size_of_base_segment() {
        return 4 + 4 + 1; // meta class index, counter, and dirty flag
    }

    private int internal_size_of_modifiedIndexes_segment(KMetaClass metaClass) {
        return metaClass.metaElements().length; // modified indexes
    }

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
            } else if (metaAttribute.attributeType() == KPrimitiveTypes.SHORT) {
                size = 2;
            } else if (metaAttribute.attributeType() == KPrimitiveTypes.DOUBLE) {
                size = 8;
            } else if (metaAttribute.attributeType() == KPrimitiveTypes.FLOAT) {
                size = 4;
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
        return internal_ptr_raw(metaClass) + offset;
    }

    @Override
    public KMemorySegment clone(KMetaClass metaClass) {
        // TODO for now it is a deep copy, in the future a shallow copy would be more efficient (attention for the free)

        OffHeapMemorySegment clonedEntry = new OffHeapMemorySegment();
        int baseSegment = internal_size_of_base_segment();
        int modifiedIndexSegment = internal_size_of_modifiedIndexes_segment(metaClass);
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
                            // copy the segment
                            int str_size = UNSAFE.getInt(clone_ptr_str_segment);
                            int bytes = 4 + str_size * 8;
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
                            // copy the segment
                            int str_size = UNSAFE.getInt(clone_ptr_str_segment);
                            int bytes = 4 + str_size * 8;
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
                        // copy the segment
                        int size = UNSAFE.getInt(clone_ptr_ref_segment);
                        int bytes = 4 + size * 8;
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
    public void set(int index, Object content, KMetaClass metaClass) {
        try {
            MetaType type = metaClass.meta(index).metaType();
            long ptr = internal_ptr_raw_for_index(index, metaClass);

            // primitive types
            if (type.equals(MetaType.ATTRIBUTE)) {

                if (content instanceof String) {
                    String s = (String) content;
                    int size = s.length();
                    long newSegment = UNSAFE.allocateMemory(4 + size * 8); // size + the actual string
                    _allocated_segments++;
                    byte[] bytes = s.getBytes("UTF-8");
                    UNSAFE.putInt(newSegment, size);
                    for (int i = 0; i < bytes.length; i++) {
                        UNSAFE.putByte(newSegment + 4 + i * 8, bytes[i]);
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
                UNSAFE.putByte(internal_ptr_modifiedIndexes() + index, (byte) 1);
            }

        } catch (
                UnsupportedEncodingException e
                )

        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public long[] getRef(int index, KMetaClass metaClass) {
        long[] result = null;

        KMeta meta = metaClass.meta(index);
        long ptr = internal_ptr_raw_for_index(index, metaClass);

        if (meta.metaType().equals(MetaType.REFERENCE)) {
            long ptr_ref_segment = UNSAFE.getLong(ptr);
            if (ptr_ref_segment != 0) {
                int size = UNSAFE.getInt(ptr_ref_segment);
                result = new long[size];
                for (int i = 0; i < size; i++) {
                    result[i] = UNSAFE.getLong(ptr_ref_segment + 4 + i * 8);
                }
            }
        }

        return result;
    }

    @Override
    public boolean addRef(int index, long newRef, KMetaClass metaClass) {
        boolean result = false;

        KMeta meta = metaClass.meta(index);
        long ptr = internal_ptr_raw_for_index(index, metaClass);

        if (meta.metaType().equals(MetaType.REFERENCE)) {
            long ptr_ref_segment = UNSAFE.getLong(ptr);
            long new_ref_ptr;
            if (ptr_ref_segment != 0) {
                int newSize = UNSAFE.getInt(ptr_ref_segment) + 1;
                new_ref_ptr = UNSAFE.reallocateMemory(ptr_ref_segment, 4 + newSize * 8);
                UNSAFE.putInt(new_ref_ptr, newSize); // size
                UNSAFE.putLong(new_ref_ptr + 4 + (newSize - 1) * 8, newRef); // content

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
    public boolean removeRef(int index, long ref, KMetaClass metaClass) {
        boolean result = false;

        KMeta meta = metaClass.meta(index);
        long ptr = internal_ptr_raw_for_index(index, metaClass);

        if (meta.metaType().equals(MetaType.REFERENCE)) {
            long ptr_ref_segment = UNSAFE.getLong(ptr);
            if (ptr_ref_segment != 0) {
                int size = UNSAFE.getInt(ptr_ref_segment);
                if (size > 1) {
                    long new_ref_ptr = UNSAFE.allocateMemory((size - 1) * 8);
                    _allocated_segments++;
                    int j = 0;
                    for (int i = 0; i < size; i++) {
                        long value = UNSAFE.getLong(ptr_ref_segment + 4 + i * 8);
                        if (value != ref) {
                            UNSAFE.putLong(new_ref_ptr + 4 + j * 8, value);
                            j++;
                        }
                    }
                    UNSAFE.putInt(new_ref_ptr, j); // set the new size
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
    public void clearRef(int index, KMetaClass metaClass) {
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
    public double[] getInfer(int index, KMetaClass metaClass) {
        double[] infer = null;
        long ptr = internal_ptr_raw_for_index(index, metaClass);
        long ptr_segment = UNSAFE.getLong(ptr);
        if (ptr_segment != 0) {
            int size = UNSAFE.getInt(ptr_segment);
            infer = new double[size];
            for (int i = 0; i < size; i++) {
                infer[i] = UNSAFE.getDouble(ptr_segment + 4 + i * 8);
            }
        }
        return infer;
    }

    @Override
    public int getInferSize(int index, KMetaClass metaClass) {
        int size = 0;
        double[] infer = getInfer(index, metaClass);
        if (infer != null) {
            size = infer.length;
        }
        return size;
    }

    @Override
    public double getInferElem(int index, int arrayIndex, KMetaClass metaClass) {
        return getInfer(index, metaClass)[arrayIndex];
    }

    @Override
    public void setInferElem(int index, int arrayIndex, double valueToInsert, KMetaClass metaClass) {
        long ptr = internal_ptr_raw_for_index(index, metaClass);
        long ptr_segment = UNSAFE.getLong(ptr);

        if (ptr_segment == 0) {
            throw new IndexOutOfBoundsException();
        }
        int size = UNSAFE.getInt(ptr_segment);
        if (index > size) {
            throw new IndexOutOfBoundsException();
        }

        UNSAFE.putDouble(ptr_segment + 4 + arrayIndex * 8, valueToInsert);

        setDirty();
    }

    @Override
    public void extendInfer(int index, int newSize, KMetaClass metaClass) {
        long ptr = internal_ptr_raw_for_index(index, metaClass);
        long ptr_segment = UNSAFE.getLong(ptr);

        long new_ptr_segment;
        if (ptr_segment != 0) {
            new_ptr_segment = UNSAFE.reallocateMemory(ptr_segment, 4 + newSize * 8);
        } else {
            new_ptr_segment = UNSAFE.allocateMemory(4 + newSize * 8);
            _allocated_segments++;
        }
        UNSAFE.putInt(new_ptr_segment, newSize); // update size
        UNSAFE.putLong(ptr, new_ptr_segment); // update pointer
        setDirty();

    }

    @Override
    public Object get(int index, KMetaClass metaClass) {
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
                            bytes[i] = UNSAFE.getByte(ptr_str_segment + 4 + i * 8);
                        }
                        result = new String(bytes, "UTF-8");
                    }

                } else if (metaAttribute.attributeType() == KPrimitiveTypes.LONG) {
                    result = UNSAFE.getLong(ptr);
                } else if (metaAttribute.attributeType() == KPrimitiveTypes.INT) {
                    result = UNSAFE.getInt(ptr);
                } else if (metaAttribute.attributeType() == KPrimitiveTypes.BOOL) {
                    result = UNSAFE.getByte(ptr) != 0;
                } else if (metaAttribute.attributeType() == KPrimitiveTypes.SHORT) {
                    result = UNSAFE.getShort(ptr);
                } else if (metaAttribute.attributeType() == KPrimitiveTypes.DOUBLE) {
                    result = UNSAFE.getLong(ptr);
                } else if (metaAttribute.attributeType() == KPrimitiveTypes.FLOAT) {
                    result = UNSAFE.getFloat(ptr);
                }
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public int[] modifiedIndexes(KMetaClass metaClass) {
        int nbModified = 0;
        long ptr = internal_ptr_modifiedIndexes();

        for (int i = 0; i < metaClass.metaElements().length; i++) {
            if (UNSAFE.getByte(ptr) != 0) {
                nbModified = nbModified + 1;
            }
            ptr = ptr + 8; // inc pointer
        }

        ptr = internal_ptr_modifiedIndexes(); // reset pointer
        int[] result = new int[nbModified];
        int inserted = 0;
        for (int i = 0; i < metaClass.metaElements().length; i++) {
            if (UNSAFE.getByte(ptr) != 0) {
                result[inserted] = i;
                inserted = inserted + 1;
            }
            ptr = ptr + 8; // inc pointer
        }
        return result;
    }

    @Override
    public void initMetaClass(KMetaClass metaClass) {
        int baseSegment = internal_size_of_base_segment();
        int modifiedIndexSegment = internal_size_of_modifiedIndexes_segment(metaClass);
        int rawSegment = internal_size_of_raw_segment(metaClass);

        int bytes = baseSegment + modifiedIndexSegment + rawSegment;

        _start_address = UNSAFE.allocateMemory(bytes);
        _allocated_segments++;
        UNSAFE.setMemory(_start_address, bytes, (byte) 0);
        UNSAFE.putInt(internal_ptr_metaClassIndex(), metaClass.index());
    }


    @Override
    public int metaClassIndex() {
        return UNSAFE.getInt(internal_ptr_metaClassIndex());
    }

    @Override
    public boolean isDirty() {
        return UNSAFE.getByte(internal_ptr_dirty()) != 0;
    }

    @Override
    public String serialize(KMetaModel metaModel) {
        KMetaClass metaClass = metaModel.metaClass(metaClassIndex());

        StringBuilder builder = new StringBuilder();
        builder.append("{\"@class\":\"");
        builder.append(metaClass.metaName());
        builder.append("\"");

        KMeta[] metaElements = metaClass.metaElements();
        if (_start_address != 0 && metaElements != null) {
            for (int i = 0; i < metaElements.length; i++) {
                KMeta meta = metaElements[i];

                if (meta.metaType().equals(MetaType.ATTRIBUTE)) {
                    MetaAttribute metaAttribute = (MetaAttribute) meta;

                    if (metaAttribute.attributeType() != KPrimitiveTypes.CONTINUOUS) {
                        Object o = get(meta.index(), metaClass);
                        if (o != null) {
                            builder.append(",\"");
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
                        double[] o = getInfer(meta.index(), metaClass);
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
                    long[] o = getRef(metaReference.index(), metaClass);
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
    public void init(String payload, KMetaModel metaModel) throws Exception {
        if (payload != null) {
            JsonObjectReader objectReader = new JsonObjectReader();
            objectReader.parseObject(payload);
            if (objectReader.get(JsonFormat.KEY_META) != null) {
                KMetaClass metaClass = metaModel.metaClassByName(objectReader.get(JsonFormat.KEY_META).toString());
                initMetaClass(metaClass);
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

                            if (metaAttribute.attributeType() == KPrimitiveTypes.CONTINUOUS) {
                                double[] infer = (double[]) converted;
                                extendInfer(metaAttribute.index(), infer.length, metaClass);
                                for (int k = 0; k < infer.length; k++) {
                                    setInferElem(metaAttribute.index(), k, infer[k], metaClass);
                                }
                            } else {
                                set(metaAttribute.index(), converted, metaClass);
                            }

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
                                for (int k = 0; k < convertedRaw.length; k++) {
                                    addRef(metaElement.index(), convertedRaw[k], metaClass);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        // should not be dirty after unserialization
        UNSAFE.putByte(internal_ptr_dirty(), (byte) 0);

    }

    @Override
    public void setClean(KMetaModel model) {
        KMetaClass metaClass = model.metaClass(UNSAFE.getInt(internal_ptr_metaClassIndex()));
        UNSAFE.putByte(internal_ptr_dirty(), (byte) 0);
        UNSAFE.setMemory(internal_ptr_modifiedIndexes(), metaClass.metaElements().length, (byte) 0);
    }

    @Override
    public void setDirty() {
        UNSAFE.putByte(internal_ptr_dirty(), (byte) 1);
    }


    @Override
    public int counter() {
        return UNSAFE.getInt(internal_ptr_counter());
    }

    @Override
    public void inc() {
        int c = UNSAFE.getInt(internal_ptr_counter());
        UNSAFE.putInt(internal_ptr_counter(), c + 1);
    }

    @Override
    public void dec() {
        int c = UNSAFE.getInt(internal_ptr_counter());
        UNSAFE.putInt(internal_ptr_counter(), c - 1);
    }

    @Override
    public void free(KMetaModel metaModel) {
        KMetaClass metaClass = metaModel.metaClass(UNSAFE.getInt(internal_ptr_metaClassIndex()));

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

        if (_allocated_segments != 0) {
            throw new RuntimeException("OffHeap Memory Management Exception: more segments allocated than freed");
        }
    }


    @Override
    public int getRefSize(int index, KMetaClass metaClass) {
        int size = 0;
        long[] refs = getRef(index, metaClass);
        if (refs != null) {
            size = refs.length;
        }
        return size;
    }

    @Override
    public long getRefElem(int index, int refIndex, KMetaClass metaClass) {
        long elem = KConfig.NULL_LONG;
        long[] refs = getRef(index, metaClass);
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
    public long getMemoryAddress() {
        return _start_address;
    }

    @Override
    public void setMemoryAddress(long address) {
        _start_address = address;
    }

    //    private void debugSegments(KMetaClass metaClass) {
//        for (int i = 0; i < metaClass.metaElements().length; i++) {
//            KMeta meta = metaClass.metaElements()[i];
//            if (meta.metaType().equals(MetaType.ATTRIBUTE)) {
//                KMetaAttribute metaAttribute = (KMetaAttribute) meta;
//
//                long ptr = internal_ptr_raw_for_index(metaAttribute.index(), metaClass);
//                System.out.println("attr pointer: " + ptr);
//                System.out.println("attr ref pointer: " + UNSAFE.getLong(ptr));
//
//            } else if (meta.metaType().equals(MetaType.REFERENCE)) {
//                KMetaReference metaReference = (KMetaReference) meta;
//
//                long ptr = internal_ptr_raw_for_index(metaReference.index(), metaClass);
//                System.out.println("ref pointer: " + ptr);
//                System.out.println("ref ref pointer: " + UNSAFE.getLong(ptr));
//
//            }
//        }
//    }


}

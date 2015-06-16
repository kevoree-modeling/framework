package org.kevoree.modeling.memory.struct.segment;

import org.kevoree.modeling.memory.KMemoryElement;
import org.kevoree.modeling.meta.KMetaClass;

public interface KMemorySegment extends KMemoryElement {

    KMemorySegment clone(KMetaClass metaClass);

    void set(int index, Object content, KMetaClass metaClass);

    Object get(int index, KMetaClass metaClass);

    //TODO add for primitive

    /* References management */
    int getRefSize(int index, KMetaClass metaClass);

    long getRefElem(int index, int refIndex, KMetaClass metaClass);

    long[] getRef(int index, KMetaClass metaClass);

    boolean addRef(int index, long newRef, KMetaClass metaClass);

    boolean removeRef(int index, long previousRef, KMetaClass metaClass);

    /* Extrapolated attributes management */
    double[] getInfer(int index, KMetaClass metaClass);

    int getInferSize(int index, KMetaClass metaClass);

    double getInferElem(int index, int arrayIndex, KMetaClass metaClass);

    void setInferElem(int index, int arrayIndex, double valueToInsert, KMetaClass metaClass);

    void extendInfer(int index, int newSize, KMetaClass metaClass);
    /*  */

    int[] modifiedIndexes(KMetaClass metaClass);

    void initMetaClass(KMetaClass metaClass);

    int metaClassIndex();

}

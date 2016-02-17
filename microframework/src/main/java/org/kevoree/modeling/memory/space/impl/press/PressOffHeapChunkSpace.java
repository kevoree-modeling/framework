package org.kevoree.modeling.memory.space.impl.press;

import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.manager.KDataManager;
import org.kevoree.modeling.memory.space.KChunkIterator;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.meta.KMetaModel;

/**
 * Created by thomas on 16/02/16.
 *
 * memory structure: | max entries (4) | elem count (4) |
 */
public class PressOffHeapChunkSpace implements KChunkSpace {

    private volatile long _start_address;


    @Override
    public void setManager(KDataManager dataManager) {

    }

    @Override
    public KChunk get(long universe, long time, long obj) {
        return null;
    }

    @Override
    public KChunk create(long universe, long time, long obj, short type, KMetaModel metaModel) {
        return null;
    }

    @Override
    public KObjectChunk clone(KObjectChunk previousElement, long newUniverse, long newTime, long newObj, KMetaModel metaModel) {
        return null;
    }

    @Override
    public void clear(KMetaModel metaModel) {

    }

    @Override
    public void free(KMetaModel metaModel) {

    }

    @Override
    public void remove(long universe, long time, long obj, KMetaModel metaModel) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public KChunkIterator detachDirties() {
        return null;
    }

    @Override
    public void declareDirty(KChunk dirtyChunk) {

    }

    @Override
    public void printDebug(KMetaModel p_metaModel) {

    }
}

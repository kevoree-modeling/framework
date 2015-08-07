package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.resolver.KResolver;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.meta.KMetaModel;

public class NoopChunkSpaceManager implements KChunkSpaceManager {

    private KChunkSpace _space;

    public NoopChunkSpaceManager(KChunkSpace p_space) {
        this._space = p_space;
    }

    @Override
    public KChunk getAndMark(long universe, long time, long obj) {
        return this._space.get(universe, time, obj);
    }

    @Override
    public void unmark(long universe, long time, long obj) {

    }

    @Override
    public KChunk unsafeGet(long universe, long time, long obj) {
        return this._space.get(universe, time, obj);
    }

    @Override
    public KChunk createAndMark(long universe, long time, long obj, short type) {
        return this._space.create(universe, time, obj, type);
    }

    @Override
    public void unmarkMemoryElement(KChunk element) {

    }

    @Override
    public void unmarkAllMemoryElements(KChunk[] elements) {

    }

    @Override
    public KObjectChunk cloneMarkAndUnmark(KObjectChunk previous, long newUniverse, long newTime, long obj, KMetaModel metaModel) {
        return this._space.clone(previous, newUniverse, newTime, obj, metaModel);
    }

    @Override
    public void clear() {

    }

    @Override
    public void register(KObject object) {

    }

    @Override
    public void registerAll(KObject[] objects) {

    }

    @Override
    public void setResolver(KResolver resolver) {

    }

    @Override
    public void notifySaved(KChunk[] savedChunks) {

    }

}

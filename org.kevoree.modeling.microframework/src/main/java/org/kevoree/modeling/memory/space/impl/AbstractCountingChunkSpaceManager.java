package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.meta.KMetaModel;

public abstract class AbstractCountingChunkSpaceManager implements KChunkSpaceManager {

    protected KChunkSpace _space;

    public AbstractCountingChunkSpaceManager(KChunkSpace p_storage) {
        this._space = p_storage;
    }

    @Override
    public KChunk getAndMark(long universe, long time, long obj) {
        KChunk resolvedElement = _space.get(universe, time, obj);
        if (resolvedElement != null) {
            resolvedElement.inc();
        }
        return resolvedElement;
    }

    @Override
    public void unmark(long universe, long time, long obj) {
        KChunk resolvedElement = _space.get(universe, time, obj);
        if (resolvedElement != null) {
            resolvedElement.dec();
        }
    }

    @Override
    public KChunk unsafeGet(long universe, long time, long obj) {
        return _space.get(universe, time, obj);
    }

    @Override
    public KChunk createAndMark(long universe, long time, long obj, short type) {
        KChunk newCreatedElement = _space.create(universe, time, obj, type);
        if (newCreatedElement != null) {
            newCreatedElement.inc();
        }
        return newCreatedElement;
    }

    @Override
    public void unmarkMemoryElement(KChunk element) {
        element.dec();
    }

    @Override
    public void unmarkAllMemoryElements(KChunk[] elements) {
        for (int i = 0; i < elements.length; i++) {
            elements[i].dec();
        }
    }

    @Override
    public KObjectChunk cloneMarkAndUnmark(KObjectChunk previous, long newUniverse, long newTime, long obj, KMetaModel metaModel) {
        KObjectChunk newCreatedElement = _space.clone(previous, newUniverse, newTime, obj, metaModel);
        newCreatedElement.inc();
        previous.dec();
        return newCreatedElement;
    }

    @Override
    public void clear() {

    }

    abstract public void register(KObject object);

    abstract public void registerAll(KObject[] objects);

}

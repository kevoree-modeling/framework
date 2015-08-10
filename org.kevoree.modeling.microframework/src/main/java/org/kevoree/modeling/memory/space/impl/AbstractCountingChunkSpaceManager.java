package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.KChunk;
import org.kevoree.modeling.memory.KChunkFlags;
import org.kevoree.modeling.memory.resolver.KResolver;
import org.kevoree.modeling.memory.space.KChunkSpaceManager;
import org.kevoree.modeling.memory.chunk.KObjectChunk;
import org.kevoree.modeling.memory.space.KChunkSpace;
import org.kevoree.modeling.meta.KMetaModel;

public abstract class AbstractCountingChunkSpaceManager implements KChunkSpaceManager {

    protected KChunkSpace _space;

    protected KMetaModel _metaModel;

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
            int newCount = resolvedElement.dec();
            if (newCount == 0 && (resolvedElement.getFlags() & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
                _space.remove(resolvedElement.universe(), resolvedElement.time(), resolvedElement.obj(), _metaModel);
            }
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
        int newCount = element.dec();
        if (newCount == 0 && (element.getFlags() & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
            _space.remove(element.universe(), element.time(), element.obj(), _metaModel);
        }
    }

    @Override
    public void unmarkAllMemoryElements(KChunk[] elements) {
        for (int i = 0; i < elements.length; i++) {
            KChunk loopChunk = elements[i];
            int newCount = elements[i].dec();
            if (newCount == 0 && (loopChunk.getFlags() & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
                _space.remove(loopChunk.universe(), loopChunk.time(), loopChunk.obj(), _metaModel);
            }
        }
    }

    @Override
    public KObjectChunk cloneMarkAndUnmark(KObjectChunk previous, long newUniverse, long newTime, long obj, KMetaModel metaModel) {
        KObjectChunk newCreatedElement = _space.clone(previous, newUniverse, newTime, obj, metaModel);
        newCreatedElement.inc();
        int newCount = previous.dec();
        if (newCount == 0 && (previous.getFlags() & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
            _space.remove(previous.universe(), previous.time(), previous.obj(), _metaModel);
        }
        return newCreatedElement;
    }

    @Override
    public void clear() {

    }

    abstract public void register(KObject object);

    abstract public void registerAll(KObject[] objects);

    abstract public void setResolver(KResolver resolver);

    abstract public void notifySaved(KChunk[] savedChunks);

}

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
        if (resolvedElement != null && (resolvedElement.getFlags() & KChunkFlags.REMOVED_BIT) != KChunkFlags.REMOVED_BIT) {
            resolvedElement.inc();
        }
        return resolvedElement;
    }

    @Override
    public void unmark(long universe, long time, long obj) {
        KChunk resolvedElement = _space.get(universe, time, obj);
        if (resolvedElement != null) {
            int newCount = resolvedElement.dec();
            if (newCount == 0) {
                cleanDependenciesAndPotentiallyRemoveChunk(resolvedElement);
            }
        }
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
        if (newCount == 0) {
            cleanDependenciesAndPotentiallyRemoveChunk(element);
        }
    }

    @Override
    public void markMemoryElement(KChunk element) {
        element.inc();
    }


    @Override
    public void unmarkAllMemoryElements(KChunk[] elements) {
        for (int i = 0; i < elements.length; i++) {
            KChunk loopChunk = elements[i];
            int newCount = elements[i].dec();
            if (newCount == 0) {
                cleanDependenciesAndPotentiallyRemoveChunk(loopChunk);
            }
        }
    }

    private final void cleanDependenciesAndPotentiallyRemoveChunk(KChunk toRemoveChunk) {
        long[] dependencies = toRemoveChunk.dependencies();
        if (dependencies != null && dependencies.length > 0) {
            for (int i = 0; i < dependencies.length; i = i + 3) {
                unmark(dependencies[i], dependencies[i + 1], dependencies[i + 2]);
            }
        }
        if ((toRemoveChunk.getFlags() & KChunkFlags.DIRTY_BIT) != KChunkFlags.DIRTY_BIT) {
            toRemoveChunk.setFlags(KChunkFlags.REMOVED_BIT, 0);
            _space.remove(toRemoveChunk.universe(), toRemoveChunk.time(), toRemoveChunk.obj(), _metaModel);
        }
    }

    @Override
    public KObjectChunk cloneAndMark(KObjectChunk previous, long newUniverse, long newTime, long obj, KMetaModel metaModel) {
        KObjectChunk newCreatedElement = _space.clone(previous, newUniverse, newTime, obj, metaModel);
        newCreatedElement.inc();
        return newCreatedElement;
    }

    @Override
    public void clear() {

    }

    abstract public void register(KObject object);

    abstract public void registerAll(KObject[] objects);

    abstract public void setResolver(KResolver resolver);

}

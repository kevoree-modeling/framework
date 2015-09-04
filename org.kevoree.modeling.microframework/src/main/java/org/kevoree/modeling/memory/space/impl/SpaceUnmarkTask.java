package org.kevoree.modeling.memory.space.impl;

import org.kevoree.modeling.memory.resolver.KResolver;
import org.kevoree.modeling.memory.space.KChunkSpaceManager;

/**
 * @ignore ts
 */
public class SpaceUnmarkTask implements Runnable {

    private final KChunkSpaceManager spaceManager;

    private final long[] collected;

    private final KResolver resolver;

    public SpaceUnmarkTask(KChunkSpaceManager spaceManager, long[] collected, KResolver resolver) {
        this.spaceManager = spaceManager;
        this.collected = collected;
        this.resolver = resolver;
    }

    @Override
    public void run() {
        int nbRelated = resolver.getRelatedKeysResultSize();
        long[] relatedResult = new long[nbRelated * 3];
        int collectedElem = collected.length / 3;
        for (int i = 0; i < collectedElem; i++) {
            this.resolver.getRelatedKeys(collected[i * 3], collected[i * 3 + 1], collected[i * 3 + 2], relatedResult);
            for (int j = 0; j < nbRelated; j++) {
                this.spaceManager.unmark(relatedResult[j * 3], relatedResult[j * 3 + 1], relatedResult[j * 3 + 2]);
            }
        }
    }
}

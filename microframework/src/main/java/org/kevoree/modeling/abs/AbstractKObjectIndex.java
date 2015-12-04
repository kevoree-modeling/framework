package org.kevoree.modeling.abs;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KObjectIndex;
import org.kevoree.modeling.memory.chunk.KObjectIndexChunk;
import org.kevoree.modeling.memory.chunk.KStringLongMapCallBack;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.impl.MetaClassIndex;

public class AbstractKObjectIndex extends AbstractKObject implements KObjectIndex {

    public AbstractKObjectIndex(long p_universe, long p_time, long p_uuid, KInternalDataManager p_manager, long p_actualUniverse, long p_actualTime) {
        super(p_universe, p_time, p_uuid, MetaClassIndex.INSTANCE, p_manager, p_actualUniverse, p_actualTime);
    }

    @Override
    public long getIndex(String key) {
        KObjectIndexChunk chunk = (KObjectIndexChunk) _manager.closestChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
        return chunk.get(key);
    }

    @Override
    public void setIndex(String key, long value) {
        KObjectIndexChunk chunk = (KObjectIndexChunk) _manager.preciseChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
        chunk.put(key, value);
    }

    @Override
    public long[] values() {
        KObjectIndexChunk chunk = (KObjectIndexChunk) _manager.closestChunk(_universe, _time, _uuid, _metaClass, _previousResolveds);
        long[] result = new long[chunk.size()];
        final int[] i = {0};
        chunk.each(new KStringLongMapCallBack() {
            @Override
            public void on(String key, long value) {
                if (value != KConfig.NULL_LONG) {
                    result[i[0]] = value;
                    i[0]++;
                }

            }
        });
        if (result.length == i[0]) {
            return result;
        } else {
            long[] trimmedResult = new long[i[0]];
            System.arraycopy(result,0,trimmedResult,0,i[0]);
            return trimmedResult;
        }
    }
}

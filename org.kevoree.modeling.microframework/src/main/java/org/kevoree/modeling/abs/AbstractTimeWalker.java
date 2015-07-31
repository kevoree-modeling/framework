package org.kevoree.modeling.abs;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KTimeWalker;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;

public class AbstractTimeWalker implements KTimeWalker {

    private AbstractKObject _origin = null;

    public AbstractTimeWalker(AbstractKObject p_origin) {
        this._origin = p_origin;
    }

    private void internal_times(final long start, final long end, KCallback<long[]> cb) {
        final KInternalDataManager manager = _origin._manager;
        manager.resolveTimes(_origin.universe(), _origin.uuid(), start, end, cb);
    }

    @Override
    public void allTimes(KCallback<long[]> cb) {
        internal_times(KConfig.BEGINNING_OF_TIME, KConfig.END_OF_TIME, cb);
    }

    @Override
    public void timesBefore(long endOfSearch, KCallback<long[]> cb) {
        internal_times(KConfig.BEGINNING_OF_TIME, endOfSearch, cb);
    }

    @Override
    public void timesAfter(long beginningOfSearch, KCallback<long[]> cb) {
        internal_times(beginningOfSearch, KConfig.END_OF_TIME, cb);
    }

    @Override
    public void timesBetween(long beginningOfSearch, long endOfSearch, KCallback<long[]> cb) {
        internal_times(beginningOfSearch, endOfSearch, cb);
    }

}
package org.kevoree.modeling.defer.impl;

import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.defer.KDefer;

public class Defer implements KDefer {

    private volatile KCallback<Object[]> _end;
    private volatile int _nbExpectedResult = 0;
    private volatile int _nbRecResult = 0;
    private volatile Object[] _results = null;
    private volatile int _resultSize = 0;

    @Override
    public KCallback waitResult() {
        return informEndOrRegister(-1, null, null);
    }

    @Override
    public void then(KCallback<Object[]> cb) {
        informEndOrRegister(-1, null, cb);
    }

    private synchronized KCallback informEndOrRegister(int p_indexToInsert, Object p_result, KCallback<Object[]> p_end) {
        if (p_end == null) {
            if (p_indexToInsert == -1) {
                final int toInsert = this._nbExpectedResult;
                this._nbExpectedResult++;
                if (this._results == null || this._resultSize < this._nbExpectedResult) {
                    int newResultSize = (this._nbExpectedResult == 0 ? 1 : this._nbExpectedResult << 1);
                    Object[] newResults = new Object[newResultSize];
                    if (this._results != null) {
                        System.arraycopy(this._results, 0, newResults, 0, this._resultSize);
                    }
                    this._resultSize = newResultSize;
                    this._results = newResults;
                }
                return new KCallback() {
                    @Override
                    public void on(Object o) {
                        informEndOrRegister(toInsert, o, null);
                    }
                };
            } else {
                _results[p_indexToInsert] = p_result;
                _nbRecResult++;
                if (this._end != null && (this._nbExpectedResult == this._nbRecResult)) {
                    Object[] finalResults = this._results;
                    if (this._resultSize != this._nbExpectedResult) {
                        Object[] newResults = new Object[this._resultSize];
                        System.arraycopy(_results, 0, newResults, 0, this._nbExpectedResult);
                        finalResults = newResults;
                    }
                    this._end.on(finalResults);
                }
            }
        } else {
            this._end = p_end;
            if (this._nbExpectedResult == this._nbRecResult) {
                Object[] finalResults = this._results;
                if (this._resultSize != this._nbExpectedResult) {
                    Object[] newResults = new Object[this._resultSize];
                    System.arraycopy(_results, 0, newResults, 0, this._nbExpectedResult);
                    finalResults = newResults;
                }
                this._end.on(finalResults);
            }
        }
        return null;
    }

}

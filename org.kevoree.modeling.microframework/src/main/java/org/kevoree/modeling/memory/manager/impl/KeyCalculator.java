package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.KConfig;

/**
 * Created by gregory.nain on 02/12/14.
 */
public class KeyCalculator {

    /**
     * @native ts
     * private _prefix: string;
     */
    private long _prefix;
    private long _currentIndex;

    /**
     * @param currentIndex
     * @param prefix
     * @native ts
     * this._prefix = "0x" + prefix.toString(org.kevoree.modeling.KConfig.PREFIX_SIZE);
     * this._currentIndex = currentIndex;
     */
    public KeyCalculator(short prefix, long currentIndex) {
        this._prefix = ((long) prefix) << KConfig.LONG_SIZE - KConfig.PREFIX_SIZE;
        this._currentIndex = currentIndex;
    }

    /**
     * @native ts
     * if (this._currentIndex == org.kevoree.modeling.KConfig.KEY_PREFIX_MASK) {
     * throw new java.lang.IndexOutOfBoundsException("Object Index could not be created because it exceeded the capacity of the current prefix. Ask for a new prefix.");
     * }
     * this._currentIndex++;
     * var indexHex = this._currentIndex.toString(org.kevoree.modeling.KConfig.PREFIX_SIZE);
     * var objectKey = parseInt(this._prefix + "000000000".substring(0,9-indexHex.length) + indexHex, org.kevoree.modeling.KConfig.PREFIX_SIZE);
     * if (objectKey >= org.kevoree.modeling.KConfig.NULL_LONG) {
     * throw new java.lang.IndexOutOfBoundsException("Object Index exceeds teh maximum JavaScript number capacity. (2^"+org.kevoree.modeling.KConfig.LONG_SIZE+")");
     * }
     * return objectKey;
     */
    public long nextKey() {
        if (_currentIndex == KConfig.KEY_PREFIX_MASK) {
            throw new IndexOutOfBoundsException("Object Index could not be created because it exceeded the capacity of the current prefix. Ask for a new prefix.");
        }
        _currentIndex++;
        //moves the prefix 53-size(short) times to the left;
        long objectKey = _prefix + _currentIndex;
        if (objectKey >= KConfig.NULL_LONG) {
            throw new IndexOutOfBoundsException("Object Index exceeds teh maximum JavaScript number capacity. (2^"+ KConfig.LONG_SIZE+")");
        }
        return objectKey;
    }

    public long lastComputedIndex() {
        return _currentIndex;
    }

    /**
     * @native ts
     * return parseInt(this._prefix,org.kevoree.modeling.KConfig.PREFIX_SIZE);
     */
    public short prefix() {
        return (short) (_prefix >> KConfig.LONG_SIZE - KConfig.PREFIX_SIZE);
    }

}

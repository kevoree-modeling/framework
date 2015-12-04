package org.kevoree.modeling.memory.manager.impl;

import org.kevoree.modeling.KConfig;

import java.util.concurrent.atomic.AtomicLong;

public class KeyCalculator {

    /**
     * @native ts
     * private _prefix: string;
     */
    private final long _prefix;
    /**
     * @native ts
     * private _currentIndex: number;
     */
    private final AtomicLong _currentIndex;

    /**
     * @param currentIndex
     * @param prefix
     * @native ts
     * this._prefix = "0x" + prefix.toString(org.kevoree.modeling.KConfig.PREFIX_SIZE);
     * this._currentIndex = currentIndex;
     */
    public KeyCalculator(short prefix, long currentIndex) {
        this._prefix = ((long) prefix) << KConfig.LONG_SIZE - KConfig.PREFIX_SIZE;
        this._currentIndex = new AtomicLong(currentIndex);
    }

    /**
     * @native ts
     * if (this._currentIndex == org.kevoree.modeling.KConfig.KEY_PREFIX_MASK) {
     * throw new Error("Object Index could not be created because it exceeded the capacity of the current prefix. Ask for a new prefix.");
     * }
     * this._currentIndex++;
     * var indexHex = this._currentIndex.toString(org.kevoree.modeling.KConfig.PREFIX_SIZE);
     * var objectKey = parseInt(this._prefix + "000000000".substring(0,9-indexHex.length) + indexHex, org.kevoree.modeling.KConfig.PREFIX_SIZE);
     * if (objectKey >= org.kevoree.modeling.KConfig.NULL_LONG) {
     * throw new Error("Object Index exceeds teh maximum JavaScript number capacity. (2^"+org.kevoree.modeling.KConfig.LONG_SIZE+")");
     * }
     * return objectKey;
     */
    public long nextKey() {
        long nextIndex = _currentIndex.incrementAndGet();
        if (_currentIndex.get() == KConfig.KEY_PREFIX_MASK) {
            throw new IndexOutOfBoundsException("Object Index could not be created because it exceeded the capacity of the current prefix. Ask for a new prefix.");
        }
        //moves the prefix 53-size(short) times to the left;
        long objectKey = _prefix + nextIndex;
        if (objectKey >= KConfig.NULL_LONG) {
            throw new IndexOutOfBoundsException("Object Index exceeds teh maximum JavaScript number capacity. (2^" + KConfig.LONG_SIZE + ")");
        }
        return objectKey;
    }

    /**
     * @native ts
     * return this._currentIndex;
     */
    public long lastComputedIndex() {
        return _currentIndex.get();
    }

    /**
     * @native ts
     * return parseInt(this._prefix,org.kevoree.modeling.KConfig.PREFIX_SIZE);
     */
    public short prefix() {
        return (short) (_prefix >> KConfig.LONG_SIZE - KConfig.PREFIX_SIZE);
    }

}

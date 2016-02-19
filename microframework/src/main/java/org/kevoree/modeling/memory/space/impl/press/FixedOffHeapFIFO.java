package org.kevoree.modeling.memory.space.impl.press;

import org.kevoree.modeling.memory.chunk.impl.UnsafeUtil;
import sun.misc.Unsafe;

/**
 * @ignore ts
 * <p/>
 * memory structure:<br/>
 * | max elem (4) | head (4) | tail (4) | next (max elem * 4) |
 */
public class FixedOffHeapFIFO implements PressFIFO {
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    private static final int ATT_MAX_ELEM_LEN = 4;
    private static final int ATT_HEAD_LEN = 4;
    private static final int ATT_TAIL_LEN = 4;
    private static final int ATT_NEXT_LEN = 4;

    private static final long OFFSET_MAX_ELEM = 0;
    private static final long OFFSET_HEAD = OFFSET_MAX_ELEM + ATT_MAX_ELEM_LEN;
    private static final long OFFSET_TAIL = OFFSET_HEAD + ATT_HEAD_LEN;
    private static final long OFFSET_NEXT = OFFSET_TAIL + ATT_TAIL_LEN;

    private volatile long _start_address;

    private long internal_offset_next(int index) {
        return OFFSET_NEXT + index * ATT_NEXT_LEN;
    }

    public FixedOffHeapFIFO(long mem_addr, int maxElem) {
        if (mem_addr == -1) {
            long mem = ATT_MAX_ELEM_LEN + ATT_HEAD_LEN + ATT_TAIL_LEN + maxElem * ATT_NEXT_LEN;
            this._start_address = UNSAFE.allocateMemory(mem);

            UNSAFE.putInt(this._start_address + OFFSET_MAX_ELEM, maxElem);
            UNSAFE.putInt(this._start_address + OFFSET_HEAD, -1);
            UNSAFE.putInt(this._start_address + OFFSET_TAIL, -1);
            for (int i = 0; i < maxElem; i++) {
                if (i != maxElem - 1) {
                    UNSAFE.putInt(this._start_address + internal_offset_next(i), i + 1);

                }
            }
            UNSAFE.putInt(this._start_address + OFFSET_HEAD, 0);
            UNSAFE.putInt(this._start_address + OFFSET_TAIL, maxElem - 1);

        } else {
            this._start_address = mem_addr;
        }


    }

    @Override
    public int dequeue() {
        int currentHead;
        int currentHeadNext;
        do {
            currentHead = UNSAFE.getInt(this._start_address + OFFSET_HEAD);
            currentHeadNext = UNSAFE.getInt(this._start_address + internal_offset_next(currentHead));
        } while (!UNSAFE.compareAndSwapInt(null, this._start_address + OFFSET_HEAD, currentHead, currentHeadNext));

        //cleanup link to avoid pollution
        UNSAFE.putInt(this._start_address + internal_offset_next(currentHead), -1);

        return currentHead;
    }

    @Override
    public void enqueue(int index) {
        int currentTail;
        do {
            currentTail = UNSAFE.getInt(this._start_address + OFFSET_TAIL);
        } while (!UNSAFE.compareAndSwapInt(null, this._start_address + OFFSET_TAIL, currentTail, index));
        UNSAFE.putInt(this._start_address + internal_offset_next(currentTail), index);
    }

    public long getMemoryAddress() {
        return this._start_address;
    }

}

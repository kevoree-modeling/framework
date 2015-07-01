package org.kevoree.modeling.memory.struct.tree.impl;

import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.memory.KOffHeapMemoryElement;
import org.kevoree.modeling.memory.struct.tree.KTreeWalker;
import org.kevoree.modeling.meta.KMetaModel;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @ignore ts
 * <p/>
 * OffHeap implementation of AbstractOffHeapTree
 * - memory structure:  | root index (8) | size (4) | dirty (1) | counter (4) | back (size * node size * 8) |
 * - back:              | key (8)        | left (8) | right (8) | parent (8)  | color (8)   | value (8)     |
 */
public abstract class AbstractOffHeapTree2 implements KOffHeapMemoryElement {
    protected static final Unsafe UNSAFE = getUnsafe();

    public abstract int getNodeSize();

    private static final char BLACK_LEFT = '{';
    private static final char BLACK_RIGHT = '}';
    private static final char RED_LEFT = '[';
    private static final char RED_RIGHT = ']';

    protected long _start_address;
    protected int _threshold;
    protected float _loadFactor;

    private void internal_allocate(int size) {
        long bytes = 17 + size * 8 * getNodeSize();

        _start_address = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(_start_address, bytes, (byte) 0);

        UNSAFE.putLong(_start_address, -1);
        UNSAFE.putInt(_start_address + 8, size);

        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (UNSAFE.getInt(_start_address + 8) * _loadFactor);
    }

    public int size() {
        return UNSAFE.getInt(_start_address + 8);
    }

    public long sibling(long currentIndex) {
        long p_currentIndex = currentIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + currentIndex * 8 * getNodeSize() + 24);
        if (p_currentIndex == -1) {
            return -1;
        } else {
            //long p_currentIndex = parent(currentIndex);
            long l_p_currentIndex = p_currentIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p_currentIndex * 8 * getNodeSize() + 8);
            if (currentIndex == l_p_currentIndex) {
                long r_p_currentIndex = p_currentIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p_currentIndex * 8 * getNodeSize() + 16);
                return r_p_currentIndex;
            } else {
                return l_p_currentIndex;
            }
        }
    }

    public long uncle(long currentIndex) {
        long p_currentIndex = currentIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + currentIndex * 8 * getNodeSize() + 24);
        if (p_currentIndex != -1) {
            return sibling(p_currentIndex);
        } else {
            return -1;
        }
    }

    private long previous(long p_index) {
        long p = p_index;
        long l_p = p == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p * 8 * getNodeSize() + 8);
        if (l_p != -1) {
            p = l_p;
            long r_p = p == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p * 8 * getNodeSize() + 16);
            while (r_p != -1) {
                p = r_p;
                r_p = p == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p * 8 * getNodeSize() + 16);
            }
            return p;
        } else {
            long p_p = p == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p * 8 * getNodeSize() + 24);
            if (p_p != -1) {
                long r_p_p = p_p == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p_p * 8 * getNodeSize() + 16);
                if (p == r_p_p) {
                    return p_p;
                } else {
                    //long p_p = parent(p);
                    long l_p_p = p_p == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p_p * 8 * getNodeSize() + 8);
                    while (p_p != -1 && p == l_p_p) {
                        p = p_p;
                    }
                    return p_p;
                }
            } else {
                return -1;
            }
        }
    }

    /* Time never use direct lookup, sadly for performance, anyway this method is private to ensure the correctness of caching mechanism */
    public long lookup(long p_key) {
        //long n = UNSAFE.getLong(internal_ptr_root_index());
        long n = UNSAFE.getLong(_start_address);
        if (n == -1) {
            return KConfig.NULL_LONG;
        }
        while (n != -1) {
            long key_n = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize());
            if (p_key == key_n) {
                return key_n;
            } else {
                if (p_key < key_n) {
                    long l_n = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 8);
                    n = l_n;
                } else {
                    long r_n = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 16);
                    n = r_n;
                }
            }
        }
        return n;
    }

    public void range(long startKey, long endKey, KTreeWalker walker) {
        long indexEnd = internal_previousOrEqual_index(endKey);
        long key_indexEnd = indexEnd == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + indexEnd * 8 * getNodeSize());
        while (indexEnd != -1 && key_indexEnd >= startKey) {
            walker.elem(key_indexEnd);
            indexEnd = previous(indexEnd);
            key_indexEnd = indexEnd == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + indexEnd * 8 * getNodeSize());
        }
    }

    protected long internal_previousOrEqual_index(long p_key) {
        long p = UNSAFE.getLong(_start_address);
        if (p == -1) {
            return p;
        }
        while (p != -1) {
            long key_p = p == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p * 8 * getNodeSize());
            if (p_key == key_p) {
                return p;
            }
            if (p_key > key_p) {
                long r_p = p == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p * 8 * getNodeSize() + 16);
                if (r_p != -1) {
                    p = r_p;
                } else {
                    return p;
                }
            } else {
                long l_p = p == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p * 8 * getNodeSize() + 8);
                if (l_p != -1) {
                    p = l_p;
                } else {
                    long parent = p == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p * 8 * getNodeSize() + 24);
                    long ch = p;
                    long left_parent = parent == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + parent * 8 * getNodeSize() + 8);
                    while (parent != -1 && ch == left_parent) {
                        ch = parent;
                        parent = parent == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + parent * 8 * getNodeSize() + 24);
                    }
                    return parent;
                }
            }
        }
        return -1;
    }

    private void rotateLeft(long n) {
        long r = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 16);
        long l_r = r == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + r * 8 * getNodeSize() + 8);
        replaceNode(n, r);
        //setRight(n, l_r);
        UNSAFE.putLong(_start_address + 17 + n * 8 * getNodeSize() + 16, l_r);
        if (l_r != -1) {
            //setParent(l_r, n);
            UNSAFE.putLong(_start_address + 17 + l_r * 8 * getNodeSize() + 24, n);
        }
        //setLeft(r, n);
        UNSAFE.putLong(_start_address + 17 + r * 8 * getNodeSize() + 8, n);
        //setParent(n, r);
        UNSAFE.putLong(_start_address + 17 + n * 8 * getNodeSize() + 24, r);

    }

    private void rotateRight(long n) {
        long l = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 8);
        replaceNode(n, l);
        long r_l = l == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + l * 8 * getNodeSize() + 16);
        //setLeft(n, right(l));
        UNSAFE.putLong(_start_address + 17 + n * 8 * getNodeSize() + 8, r_l);
        if (r_l != -1) {
            //setParent(r_l, n);
            UNSAFE.putLong(_start_address + 17 + r_l * 8 * getNodeSize() + 24, n);

        }
        //setRight(l, n);
        UNSAFE.putLong(_start_address + 17 + l * 8 * getNodeSize() + 16, n);
        //setParent(n, l);
        UNSAFE.putLong(_start_address + 17 + n * 8 * getNodeSize() + 24, l);

    }

    private void replaceNode(long oldn, long newn) {
        long p_oldn = oldn == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + oldn * 8 * getNodeSize() + 24);
        if (p_oldn == -1) {
            UNSAFE.putLong(_start_address, newn);
        } else {
            long parent_oldn = p_oldn;
            long l_parent_oldn = parent_oldn == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + parent_oldn * 8 * getNodeSize() + 8);
            if (oldn == l_parent_oldn) {
                //setLeft(parent_oldn, newn);
                UNSAFE.putLong(_start_address + 17 + parent_oldn * 8 * getNodeSize() + 8, newn);

            } else {
                //setRight(parent_oldn, newn);
                UNSAFE.putLong(_start_address + 17 + parent_oldn * 8 * getNodeSize() + 16, newn);
            }
        }
        if (newn != -1) {
            //setParent(newn, p_oldn);
            UNSAFE.putLong(_start_address + 17 + newn * 8 * getNodeSize() + 24, p_oldn);

        }
    }

    protected void insertCase1(long n) {
        long p_n = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 24);
        if (p_n == -1) {
            //setColor(n, 1);
            UNSAFE.putLong(_start_address + 17 + n * 8 * getNodeSize() + 32, 1);
        } else {
            insertCase2(n);
        }
    }

    private void insertCase2(long n) {
        long p_n = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 24);
        boolean c_p_n = p_n == -1 ? true : (UNSAFE.getLong(_start_address + 17 + p_n * 8 * getNodeSize() + 32) == 1);
        if (c_p_n == true) {
            return;
        } else {
            insertCase3(n);
        }
    }

    private void insertCase3(long n) {
        long p_n = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 24);
        boolean c_u_n = uncle(n) == -1 ? true : (UNSAFE.getLong(_start_address + 17 + uncle(n) * 8 * getNodeSize() + 32) == 1);
        if (c_u_n == false) {
            long gp_n = p_n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p_n * 8 * getNodeSize() + 24);

            //setColor(p_n, 1);
            UNSAFE.putLong(_start_address + 17 + p_n * 8 * getNodeSize() + 32, 1);
            //setColor(uncle(n), 1);
            UNSAFE.putLong(_start_address + 17 + uncle(n) * 8 * getNodeSize() + 32, 1);
            //setColor(grandParent(n), 0);
            UNSAFE.putLong(_start_address + 17 + gp_n * 8 * getNodeSize() + 32, 0);

            insertCase1(gp_n);
        } else {
            insertCase4(n);
        }
    }

    private void insertCase4(long n_n) {
        long n = n_n;
        long p_n = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 24);
        long r_p_n = p_n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p_n * 8 * getNodeSize() + 16);
        long gp_n = p_n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p_n * 8 * getNodeSize() + 24);

        long l_gp_n = gp_n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + gp_n * 8 * getNodeSize() + 8);
        if (n == r_p_n && p_n == l_gp_n) {
            rotateLeft(p_n);
            n = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 8);
        } else {
            long l_p_n = p_n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p_n * 8 * getNodeSize() + 8);
            long r_gp_n = gp_n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + gp_n * 8 * getNodeSize() + 16);
            if (n == l_p_n && p_n == r_gp_n) {
                long r_n = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 16);
                rotateRight(p_n);
                n = r_n;
            }
        }
        insertCase5(n);
    }

    private void insertCase5(long n) {
        long p_n = n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + n * 8 * getNodeSize() + 24);
        long gp_n = p_n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p_n * 8 * getNodeSize() + 24);
        long l_p_n = p_n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + p_n * 8 * getNodeSize() + 8);
        long l_gp_n = gp_n == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + gp_n * 8 * getNodeSize() + 8);

        //setColor(p_n, 1);
        UNSAFE.putLong(_start_address + 17 + p_n * 8 * getNodeSize() + 32, 1);
        //setColor(gp_n, 0);
        UNSAFE.putLong(_start_address + 17 + gp_n * 8 * getNodeSize() + 32, 0);

        if (n == l_p_n && p_n == l_gp_n) {
            rotateRight(gp_n);
        } else {
            rotateLeft(gp_n);
        }
    }


    public void delete(long p_key) {
        //TODO
    }

    public String serialize(KMetaModel metaModel) {
        StringBuilder builder = new StringBuilder();

        long rootIndex = UNSAFE.getLong(_start_address);
        int size = UNSAFE.getInt(_start_address + 8);
        if (rootIndex == -1) {
            builder.append("0");
        } else {
            builder.append(size);
            builder.append(',');
            int elemSize = getNodeSize();
            builder.append(rootIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + rootIndex * 8 * getNodeSize()));
            for (int i = 0; i < size; i++) {
                long nextNodeIndex = i; /*i * elemSize;*/
                long parentNodeIndex = nextNodeIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + nextNodeIndex * 8 * getNodeSize() + 24);
                boolean isOnLeft = false;
                if (parentNodeIndex != -1) {
                    long l_parentNodeIndex = parentNodeIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + parentNodeIndex * 8 * getNodeSize() + 8);
                    isOnLeft = l_parentNodeIndex == nextNodeIndex;
                }
                long c_nextNodeIndex = nextNodeIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + nextNodeIndex * 8 * getNodeSize() + 32);
                if (c_nextNodeIndex == 0) {
                    if (isOnLeft) {
                        builder.append(BLACK_LEFT);
                    } else {
                        builder.append(BLACK_RIGHT);
                    }
                } else {
                    //red
                    if (isOnLeft) {
                        builder.append(RED_LEFT);
                    } else {
                        builder.append(RED_RIGHT);
                    }
                }
                builder.append(nextNodeIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + nextNodeIndex * 8 * getNodeSize()));
                builder.append(',');
                if (parentNodeIndex != -1) {
                    builder.append(parentNodeIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + parentNodeIndex * 8 * getNodeSize()));
                }
                if (elemSize > 5) {
                    builder.append(',');
                    builder.append(nextNodeIndex == -1 ? -1 : UNSAFE.getLong(_start_address + 17 + nextNodeIndex * 8 * getNodeSize() + 40));
                }
            }
        }
        return builder.toString();
    }

    public void init(String payload, KMetaModel metaModel) {
        if (payload == null || payload.length() == 0) {
            internal_allocate(0);
            return;
        }
        int elemSize = getNodeSize();
        int initPos = 0;
        int cursor = 0;
        while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }

        int s = Integer.parseInt(payload.substring(initPos, cursor));
        internal_allocate(s);

        if (payload.charAt(cursor) == ',') {//className to parse
            UNSAFE.putInt(_start_address + 8, s);
            cursor++;
            initPos = cursor;
        }
        while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
            cursor++;
        }

        UNSAFE.putLong(_start_address, Integer.parseInt(payload.substring(initPos, cursor)));
        UNSAFE.setMemory(_start_address + 17, s * 8 * elemSize, (byte) -1);

        int _back_index = 0;
        while (cursor < payload.length()) {
            while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                cursor++;
            }
            if (cursor < payload.length()) {
                char elem = payload.charAt(cursor);

                boolean isOnLeft = false;
                if (elem == BLACK_LEFT || elem == RED_LEFT) {
                    isOnLeft = true;
                }
                if (elem == BLACK_LEFT || elem == BLACK_RIGHT) {
                    //setColor(_back_index, 0);
                    UNSAFE.putLong(_start_address + 17 + _back_index * 8 * getNodeSize() + 32, 0);
                } else {
                    //setColor(_back_index, 1);
                    UNSAFE.putLong(_start_address + 17 + _back_index * 8 * getNodeSize() + 32, 1);
                }
                cursor++;
                int beginChunk = cursor;
                while (cursor < payload.length() && payload.charAt(cursor) != ',') {
                    cursor++;
                }
                long loopKey = Long.parseLong(payload.substring(beginChunk, cursor));
                UNSAFE.putLong(_start_address + 17 + _back_index * 8 * getNodeSize(), loopKey); // key
                cursor++;
                beginChunk = cursor;
                while (cursor < payload.length() && payload.charAt(cursor) != ',' && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                    cursor++;
                }
                if (cursor > beginChunk) {
                    long parentRaw = Long.parseLong(payload.substring(beginChunk, cursor));
                    long parentValue = parentRaw; //* elemSize;
                    //setParent(_back_index, parentValue);
                    UNSAFE.putLong(_start_address + 17 + _back_index * 8 * getNodeSize() + 24, parentValue);

                    if (isOnLeft) {
                        //setLeft(parentValue, _back_index);
                        UNSAFE.putLong(_start_address + 17 + parentValue * 8 * getNodeSize() + 8, _back_index);

                    } else {
                        //setRight(parentValue, _back_index);
                        UNSAFE.putLong(_start_address + 17 + parentValue * 8 * getNodeSize() + 16, _back_index);
                    }
                }
                if (cursor < payload.length() && payload.charAt(cursor) == ',') {
                    cursor++;
                    beginChunk = cursor;
                    while (cursor < payload.length() && payload.charAt(cursor) != BLACK_LEFT && payload.charAt(cursor) != BLACK_RIGHT && payload.charAt(cursor) != RED_LEFT && payload.charAt(cursor) != RED_RIGHT) {
                        cursor++;
                    }
                    if (cursor > beginChunk) {
                        long currentValue = Long.parseLong(payload.substring(beginChunk, cursor));
                        //setValue(_back_index, currentValue);
                        UNSAFE.putLong(_start_address + 17 + _back_index * 8 * getNodeSize() + 40, currentValue);
                    }
                }
                _back_index++;
            }
        }
    }

    public boolean isDirty() {
        return UNSAFE.getByte(_start_address + 12) != 0;
    }

    public void setClean(KMetaModel p_metaModel) {
        UNSAFE.putByte(_start_address + 12, (byte) 0);
    }

    public void setDirty() {
        UNSAFE.putByte(_start_address + 12, (byte) 1);
    }

    public int counter() {
        return UNSAFE.getInt(_start_address + 13);
    }

    public void inc() {
        int c = UNSAFE.getInt(_start_address + 13);
        UNSAFE.putInt(_start_address + 13, c + 1);
    }

    public void dec() {
        int c = UNSAFE.getInt(_start_address + 13);
        UNSAFE.putInt(_start_address + 13, c - 1);
    }

    public void free(KMetaModel p_metaModel) {
        UNSAFE.freeMemory(_start_address);
    }


    @SuppressWarnings("restriction")
    protected static Unsafe getUnsafe() {
        try {

            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);

        } catch (Exception e) {
            throw new RuntimeException("ERROR: unsafe operations are not available");
        }
    }

    @Override
    public long getMemoryAddress() {
        return _start_address;
    }

    @Override
    public void setMemoryAddress(long address) {
        _start_address = address;

        _loadFactor = KConfig.CACHE_LOAD_FACTOR;
        _threshold = (int) (UNSAFE.getInt(_start_address + 8) * _loadFactor);
    }

}

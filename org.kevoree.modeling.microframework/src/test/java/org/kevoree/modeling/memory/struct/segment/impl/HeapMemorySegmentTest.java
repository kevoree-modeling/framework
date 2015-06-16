package org.kevoree.modeling.memory.struct.segment.impl;

import org.kevoree.modeling.memory.struct.segment.BaseKMemorySegmentTest;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;

public class HeapMemorySegmentTest extends BaseKMemorySegmentTest {

    @Override
    public KMemorySegment createKMemorySegment() {
        return new HeapMemorySegment();
    }

}

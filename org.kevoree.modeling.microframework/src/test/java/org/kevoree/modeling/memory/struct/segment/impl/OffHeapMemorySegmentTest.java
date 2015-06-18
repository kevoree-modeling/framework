package org.kevoree.modeling.memory.struct.segment.impl;

import org.kevoree.modeling.memory.struct.segment.BaseKMemorySegmentTest;
import org.kevoree.modeling.memory.struct.segment.KMemorySegment;

/** @ignore ts*/
public class OffHeapMemorySegmentTest extends BaseKMemorySegmentTest {

    @Override
    public KMemorySegment createKMemorySegment() {
        return new OffHeapMemorySegment();
    }
}

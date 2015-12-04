package org.kevoree.modeling.message;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.message.impl.Message;

public class MessageTest {

    @Test
    public void test() {

        KMessage hello = new Message();
        hello.setType(3);
        hello.setID(150500);
        hello.setClassName("MySimpleClass");
        hello.setKeys(new long[]{3, 4, 5, 6, 7, 100000});
        hello.setOperationName("MySimpleOp");
        hello.setPeer("MySimplePeer");
        hello.setValues(new String[]{"v", "MySimpleValue", "MySuperLongSimpleValue"});
        hello.setValues2(new String[]{"v",""});

        String hello_saved = hello.save();
        KMessage hello2 = Message.load(hello_saved);
        String hello2_saved = hello2.save();
        Assert.assertEquals(hello_saved, hello2_saved);



    }

}

package org.kevoree.modeling.message;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.message.impl.*;

public class KMessageTest {

    @Test
    public void test() {

        Events event = new Events(new long[]{0, 1, 2});

        Assert.assertEquals("{\n" +
                "\"type\":\"0\"\n" +
                ",\"keys\":[\"A\",\"C\",\"E\"]\n" +
                "}\n", event.json());
        KMessage parsed = KMessageLoader.load(event.json());
        Assert.assertEquals(parsed.json(), event.json());

        GetRequest msgGet = new GetRequest();
        msgGet.id = 0l;
        msgGet.keys = new long[]{
                0, 1, 2,
                3, 4, 5,
                6, 7, 8};

        Assert.assertEquals("{\n" +
                "\"type\":\"1\"\n" +
                ",\"id\":\"0\"\n" +
                ",\"keys\":[\"A\",\"C\",\"E\",\"G\",\"I\",\"K\",\"M\",\"O\",\"Q\"]\n" +
                "}\n", msgGet.json());
        KMessage parsedGet = KMessageLoader.load(msgGet.json());
        Assert.assertEquals(parsedGet.json(), msgGet.json());

        PutRequest msgPut = new PutRequest();
        msgPut.id = 0l;
        msgPut.keys = new long[]{
                0, 1, 2,
                3, 4, 5};
        msgPut.values = new String[2];
        msgPut.values[0] = "hello0";
        msgPut.values[1] = "hello1";

        Assert.assertEquals("{\n" +
                "\"type\":\"3\"\n" +
                ",\"id\":\"0\"\n" +
                ",\"keys\":[\"A\",\"C\",\"E\",\"G\",\"I\",\"K\"]\n" +
                ",\"values\":[\"hello0\",\"hello1\"]\n" +
                "}\n", msgPut.json());
        KMessage parsedPut = KMessageLoader.load(msgPut.json());
        Assert.assertEquals(parsedPut.json(), msgPut.json());

        /*
        OperationCallMessage msgCall = new OperationCallMessage();
        msgCall.id = 1l;
        msgCall.key = new long[]{0,1,2};
        msgCall.opIndex = 0;
        msgCall.classIndex = 0;
        msgCall.params = new String[3];
        msgCall.params[0] = "param0";
        msgCall.params[1] = "param1";
        msgCall.params[2] = "param2";
        Assert.assertEquals("{\n" +
                "\"type\":\"5\"\n" +
                ",\"id\":\"1\"\n" +
                ",\"key\":\"A" + KConfig.KEY_SEP + "C" + KConfig.KEY_SEP + "E\"\n" +
                ",\"class\":\"0\",\"op\":\"0\",\"params\":[\"param0\",\"param1\",\"param2\"]\n" +
                "}\n", msgCall.json());

        KMessage parsedCall = KMessageLoader.load(msgCall.json());
        Assert.assertEquals(parsedCall.json(), msgCall.json());

        OperationResultMessage msgResult = new OperationResultMessage();
        msgResult.id = 1l;
        msgResult.key = KContentKey.createObject(0l, 1l, 2l);
        msgResult.value = "hello";
        Assert.assertEquals("{\n" +
                "\"type\":\"6\"\n" +
                ",\"id\":\"1\"\n" +
                ",\"key\":\"A" + KConfig.KEY_SEP + "C" + KConfig.KEY_SEP + "E\"\n" +
                ",\"value\":\"hello\"\n" +
                "}\n", msgResult.json());

        KMessage parsedResult = KMessageLoader.load(msgResult.json());
        Assert.assertEquals(parsedResult.json(), msgResult.json());
*/
    }
}

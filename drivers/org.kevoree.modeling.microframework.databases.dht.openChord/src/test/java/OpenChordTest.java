import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kevoree.modeling.api.Callback;
import org.kevoree.modeling.api.ThrowableCallback;
import org.kevoree.modeling.api.memory.cache.KContentKey;
import org.kevoree.modeling.api.memory.cdn.KContentPutRequest;

/**
 * Created by cyril on 27/05/15.
 */
public class OpenChordTest {

    private OpenchordContentDeliveryDriver occdd=null;

    @Before
    public void init(){
        String address = null;
        //address = InetAddress.getLocalHost().toString();
        //address = address.substring(address.indexOf("/") + 1, address.length());
        this.occdd=new OpenchordContentDeliveryDriver();
        this.occdd.start(true, address, 0);
    }

    @Test
    public void test(){
        this.occdd.start(false, null, 0);
        this.occdd.connect(new Callback<Throwable>() {
            @Override
            public void on(Throwable throwable) {

                KContentPutRequest request = new KContentPutRequest(3);

                KContentKey k0 = KContentKey.createObject(0l, 1l, 2l);
                KContentKey k1 = KContentKey.createObject(3l, 4l, 5l);

                request.put(k0, "K0");
                request.put(k1, "K1");
                occdd.put(request, new Callback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        if (throwable != null) {
                            throwable.printStackTrace();
                        }
                    }
                });

                KContentKey[] keys = new KContentKey[2];
                keys[0] = k0;
                keys[1] = k1;

                occdd.get(keys, new ThrowableCallback<String[]>() {
                    @Override
                    public void on(String[] strings, Throwable error) {
                        Assert.assertEquals(strings.length, 2);
                        Assert.assertEquals(strings[0], "K0");
                        Assert.assertEquals(strings[1], "K1");
                    }
                });
            }
        });
    }
}

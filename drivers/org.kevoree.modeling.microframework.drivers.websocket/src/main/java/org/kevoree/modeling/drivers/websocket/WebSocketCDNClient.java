package org.kevoree.modeling.drivers.websocket;

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSocketVersion;
import io.undertow.websockets.core.WebSockets;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.memory.chunk.KIntMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayLongMap;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.KMessageLoader;
import org.kevoree.modeling.message.impl.AtomicGetIncrementRequest;
import org.kevoree.modeling.message.impl.AtomicGetIncrementResult;
import org.kevoree.modeling.message.impl.Events;
import org.kevoree.modeling.message.impl.GetRequest;
import org.kevoree.modeling.message.impl.GetResult;
import org.kevoree.modeling.message.impl.PutRequest;
import org.kevoree.modeling.message.impl.PutResult;
import org.xnio.BufferAllocator;
import org.xnio.ByteBufferSlicePool;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class WebSocketCDNClient extends AbstractReceiveListener implements KContentDeliveryDriver {

    private static final int CALLBACK_SIZE = 100000;

    private UndertowWSClient _client;

    private AtomicInteger _atomicInteger = null;

    private final ArrayLongMap<Object> _callbacks = new ArrayLongMap<Object>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);

    public WebSocketCDNClient(String url) {
        _client = new UndertowWSClient(url);
    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        _client.connect(this);
        _atomicInteger = new AtomicInteger();
        callback.on(null);
    }

    @Override
    public void close(KCallback<Throwable> callback) {
        _client.close();
        callback.on(null);
    }

    private long nextKey() {
        return _atomicInteger.getAndUpdate(new IntUnaryOperator() {
            @Override
            public int applyAsInt(int operand) {
                if (operand == CALLBACK_SIZE) {
                    return 0;
                } else {
                    return operand + 1;
                }
            }
        });
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        String payload = message.getData();
        KMessage msg = KMessageLoader.load(payload);
        switch (msg.type()) {
            case KMessageLoader.GET_RES_TYPE: {
                GetResult getResult = (GetResult) msg;
                Object callbackRegistered = _callbacks.get(getResult.id);
                if (callbackRegistered != null) {
                    ((KCallback) callbackRegistered).on(getResult.values);
                } else {
                    System.err.println();
                }
                _callbacks.remove(getResult.id);
            }
            break;
            case KMessageLoader.PUT_RES_TYPE: {
                PutResult putResult = (PutResult) msg;
                Object callbackRegistered = _callbacks.get(putResult.id);
                if (callbackRegistered != null) {
                    ((KCallback) callbackRegistered).on(null);
                } else {
                    System.err.println();
                }
            }
            break;
            case KMessageLoader.ATOMIC_GET_INC_RESULT_TYPE: {
                AtomicGetIncrementResult atomicGetResult = (AtomicGetIncrementResult) msg;
                Object callbackRegistered = _callbacks.get(atomicGetResult.id);
                if (callbackRegistered != null) {
                    ((KCallback) callbackRegistered).on(atomicGetResult.value);
                } else {
                    System.err.println();
                }
            }
            break;
            case KMessageLoader.EVENTS_TYPE: {
                Events eventsMessage = (Events) msg;
                if (additionalInterceptors != null) {
                    additionalInterceptors.each(new KIntMapCallBack<KContentUpdateListener>() {
                        @Override
                        public void on(int key, KContentUpdateListener value) {
                            value.on(eventsMessage.allKeys());
                        }
                    });
                }
            }
            break;
            default: {
                System.err.println("MessageType not supported:" + msg.type());
            }
        }
    }

    @Override
    public void atomicGetIncrement(KContentKey key, KCallback<Short> callback) {
        AtomicGetIncrementRequest atomicGetRequest = new AtomicGetIncrementRequest();
        atomicGetRequest.id = nextKey();
        atomicGetRequest.key = key;
        _callbacks.put(atomicGetRequest.id, callback);
        WebSockets.sendText(atomicGetRequest.json(), _client.getChannel(), null);
    }

    @Override
    public void get(KContentKey[] keys, KCallback<String[]> callback) {
        GetRequest getRequest = new GetRequest();
        getRequest.keys = keys;
        getRequest.id = nextKey();
        _callbacks.put(getRequest.id, callback);
        WebSockets.sendText(getRequest.json(), _client.getChannel(), null);
    }

    @Override
    public synchronized void put(KContentKey[] p_keys, String[] p_values, KCallback<Throwable> p_callback, int excludeListener) {
        PutRequest putRequest = new PutRequest();
        putRequest.keys = p_keys;
        putRequest.values = p_values;
        putRequest.id = nextKey();
        _callbacks.put(putRequest.id, p_callback);
        WebSockets.sendText(putRequest.json(), _client.getChannel(), null);
        if (additionalInterceptors != null) {
            additionalInterceptors.each(new KIntMapCallBack<KContentUpdateListener>() {
                @Override
                public void on(int key, KContentUpdateListener value) {
                    if (value != null && key != excludeListener) {
                        value.on(p_keys);
                    }
                }
            });
        }
    }

    @Override
    public void remove(String[] keys, KCallback<Throwable> error) {
        //TODO
    }


    private ArrayIntMap<KContentUpdateListener> additionalInterceptors = null;

    private Random random = new Random();

    private int nextListenerID() {
        return random.nextInt();
    }

    @Override
    public synchronized int addUpdateListener(KContentUpdateListener p_interceptor) {
        if (additionalInterceptors == null) {
            additionalInterceptors = new ArrayIntMap<KContentUpdateListener>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        }
        int newID = nextListenerID();
        additionalInterceptors.put(newID, p_interceptor);
        return newID;
    }

    @Override
    public synchronized void removeUpdateListener(int id) {
        if (additionalInterceptors != null) {
            additionalInterceptors.remove(id);
        }
    }

    class UndertowWSClient {

        private ByteBufferSlicePool _buffer;
        private XnioWorker _worker;
        private WebSocketChannel _webSocketChannel = null;
        private String _url;

        public UndertowWSClient(String url) {
            this._url = url;
            try {
                Xnio xnio = Xnio.getInstance(io.undertow.websockets.client.WebSocketClient.class.getClassLoader());
                _worker = xnio.createWorker(OptionMap.builder()
                        .set(Options.WORKER_IO_THREADS, 2)
                        .set(Options.CONNECTION_HIGH_WATER, 1000000)
                        .set(Options.CONNECTION_LOW_WATER, 1000000)
                        .set(Options.WORKER_TASK_CORE_THREADS, 30)
                        .set(Options.WORKER_TASK_MAX_THREADS, 30)
                        .set(Options.TCP_NODELAY, true)
                        .set(Options.CORK, true)
                        .getMap());

                _buffer = new ByteBufferSlicePool(BufferAllocator.BYTE_BUFFER_ALLOCATOR, 1024, 1024);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void connect(AbstractReceiveListener listener) {
            try {

                _webSocketChannel = io.undertow.websockets.client.WebSocketClient.connect(_worker, _buffer, OptionMap.EMPTY, new URI(_url), WebSocketVersion.V13).get();
                _webSocketChannel.getReceiveSetter().set(listener);
                _webSocketChannel.resumeReceives();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                _webSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public WebSocketChannel getChannel() {
            return _webSocketChannel;
        }

    }

}

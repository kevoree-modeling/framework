package org.kevoree.modeling.databases.websocket;

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.kevoree.modeling.*;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentPutRequest;
import org.kevoree.modeling.event.KEventListener;
import org.kevoree.modeling.event.KEventMultiListener;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.memory.struct.map.impl.ArrayLongMap;
import org.kevoree.modeling.message.*;
import org.kevoree.modeling.message.impl.*;
import org.kevoree.modeling.event.impl.LocalEventListeners;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * Created by duke on 24/02/15.
 */
public class WebSocketClient extends AbstractReceiveListener implements KContentDeliveryDriver {

    private static final int CALLBACK_SIZE = 100000;

    private UndertowWSClient _client;

    private LocalEventListeners _localEventListeners = new LocalEventListeners();
    private KMemoryManager _manager;
    private AtomicInteger _atomicInteger = null;

    private final ArrayLongMap<Object> _callbacks = new ArrayLongMap<Object>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);

    public WebSocketClient(String url) {
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
                this._manager.reload(eventsMessage.allKeys(), new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        WebSocketClient.this._localEventListeners.dispatch(eventsMessage);
                    }
                });
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
    public void put(KContentPutRequest request, KCallback<Throwable> error) {
        PutRequest putRequest = new PutRequest();
        putRequest.request = request;
        putRequest.id = nextKey();
        _callbacks.put(putRequest.id, error);
        WebSockets.sendText(putRequest.json(), _client.getChannel(), null);
    }

    @Override
    public void remove(String[] keys, KCallback<Throwable> error) {
        //TODO
    }

    @Override
    public void registerListener(long p_groupId, KObject p_origin, KEventListener p_listener) {
        _localEventListeners.registerListener(p_groupId, p_origin, p_listener);
    }

    @Override
    public void registerMultiListener(long p_groupId, KUniverse p_origin, long[] p_objects, KEventMultiListener p_listener) {
        _localEventListeners.registerListenerAll(p_groupId, p_origin.key(), p_objects, p_listener);
    }

    @Override
    public void unregisterGroup(long p_groupId) {
        _localEventListeners.unregister(p_groupId);
    }

    @Override
    public void send(KMessage msg) {
        _localEventListeners.dispatch(msg);
        WebSockets.sendText(msg.json(), _client.getChannel(), null);
    }

    @Override
    public void setManager(KMemoryManager p_manager) {
        _manager = p_manager;
        _localEventListeners.setManager(p_manager);
    }

}

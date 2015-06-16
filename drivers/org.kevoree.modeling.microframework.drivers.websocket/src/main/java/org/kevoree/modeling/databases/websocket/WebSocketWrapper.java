package org.kevoree.modeling.databases.websocket;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.StreamSourceFrameChannel;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.kevoree.modeling.*;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentPutRequest;
import org.kevoree.modeling.event.KEventListener;
import org.kevoree.modeling.event.KEventMultiListener;
import org.kevoree.modeling.memory.manager.KMemoryManager;
import org.kevoree.modeling.message.*;
import org.kevoree.modeling.message.impl.*;

import java.io.IOException;
import java.util.ArrayList;

import static io.undertow.Handlers.websocket;

public class WebSocketWrapper extends AbstractReceiveListener implements KContentDeliveryDriver, WebSocketConnectionCallback {

    private KContentDeliveryDriver wrapped = null;
    private ArrayList<WebSocketChannel> _connectedChannels = new ArrayList<WebSocketChannel>();
    private KMemoryManager _manager;

    private Undertow _server = null;
    private String _address = "0.0.0.0";
    private int _port = 8080;
    private ClassLoader _exposedClassLoader = null;

    public WebSocketWrapper(KContentDeliveryDriver p_wrapped, int p_port) {
        this.wrapped = p_wrapped;
        this._port = p_port;
    }

    public WebSocketWrapper exposeResourcesOf(ClassLoader classLoader) {
        this._exposedClassLoader = classLoader;
        return this;
    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        if (wrapped != null) {
            if (_exposedClassLoader != null) {
                _server = Undertow.builder().addHttpListener(_port, _address)
                        .setHandler(Handlers.path().addPrefixPath("/cdn", websocket(this)).addPrefixPath("/", Handlers.resource(new ClassPathResourceManager(_exposedClassLoader))))
                        .build();
            } else {
                _server = Undertow.builder().addHttpListener(_port, _address).setHandler(websocket(this)).build();
            }
            _server.start();
            wrapped.connect(callback);
        } else {
            if (callback != null) {
                callback.on(new Exception("Wrapped must not be null."));
            }
        }
    }

    @Override
    public void close(final KCallback<Throwable> callback) {
        if (wrapped != null) {
            wrapped.close(new KCallback<Throwable>() {
                @Override
                public void on(Throwable throwable) {
                    if (_server != null) {
                        _server.stop();
                    }
                    callback.on(throwable);
                }
            });
        } else {
            if (callback != null) {
                callback.on(null);
            }
        }
    }

    public void onConnect(WebSocketHttpExchange webSocketHttpExchange, WebSocketChannel webSocketChannel) {
        webSocketChannel.getReceiveSetter().set(this);
        webSocketChannel.resumeReceives();
        _connectedChannels.add(webSocketChannel);
    }

    @Override
    protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
        _connectedChannels.remove(webSocketChannel);
    }

    @Override
    protected void onFullTextMessage(final WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        String payload = message.getData();
        KMessage msg = KMessageLoader.load(payload);
        switch (msg.type()) {
            case KMessageLoader.GET_REQ_TYPE: {
                final GetRequest getRequest = (GetRequest) msg;
                wrapped.get(getRequest.keys, new KCallback<String[]>() {
                    public void on(String[] strings) {
                            GetResult getResultMessage = new GetResult();
                            getResultMessage.id = getRequest.id;
                            getResultMessage.values = strings;
                            WebSockets.sendText(getResultMessage.json(), channel, null);
                    }
                });
            }
            break;
            case KMessageLoader.PUT_REQ_TYPE: {
                final PutRequest putRequest = (PutRequest) msg;
                wrapped.put(putRequest.request, new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        if (throwable == null) {
                            PutResult putResultMessage = new PutResult();
                            putResultMessage.id = putRequest.id;
                            WebSockets.sendText(putResultMessage.json(), channel, null);
                        }
                    }
                });
            }
            break;
            case KMessageLoader.ATOMIC_GET_INC_REQUEST_TYPE: {
                final AtomicGetIncrementRequest atomicGetRequest = (AtomicGetIncrementRequest) msg;
                wrapped.atomicGetIncrement(atomicGetRequest.key, new KCallback<Short>() {
                    @Override
                    public void on(Short s) {
                        if (s != null) {
                            AtomicGetIncrementResult atomicGetResultMessage = new AtomicGetIncrementResult();
                            atomicGetResultMessage.id = atomicGetRequest.id;
                            atomicGetResultMessage.value = s;
                            WebSockets.sendText(atomicGetResultMessage.json(), channel, null);
                        }
                    }
                });
            }
            break;
            case KMessageLoader.OPERATION_CALL_TYPE:
            case KMessageLoader.OPERATION_RESULT_TYPE: {
                _manager.operationManager().operationEventReceived(msg);
            }
            break;
            case KMessageLoader.EVENTS_TYPE: {
                Events events = (Events) msg;
                if (_manager != null) {
                    _manager.reload(events.allKeys(), null);
                }
                //local listeners dispatch
                wrapped.send(events);
                //forward to remote listeners
                ArrayList<WebSocketChannel> channels = new ArrayList<>(_connectedChannels);
                for (int i = 0; i < channels.size(); i++) {
                    WebSocketChannel chan = channels.get(i);
                    if (chan != channel) {
                        WebSockets.sendText(payload, chan, null);
                    }
                }
            }
            break;
            default: {
                System.err.println("Uh !. MessageType not supported:" + msg.type());
            }
        }
    }

    @Override
    public void atomicGetIncrement(KContentKey key, KCallback<Short> callback) {
        wrapped.atomicGetIncrement(key, callback);
    }

    @Override
    public void get(KContentKey[] keys, KCallback<String[]> callback) {
        wrapped.get(keys, callback);
    }

    @Override
    public void put(KContentPutRequest request, KCallback<Throwable> error) {
        wrapped.put(request, error);
    }

    @Override
    public void remove(String[] keys, KCallback<Throwable> error) {
        wrapped.remove(keys, error);
    }

    @Override
    public void registerListener(long p_groupId, KObject p_origin, KEventListener p_listener) {
        wrapped.registerListener(p_groupId, p_origin, p_listener);
    }

    @Override
    public void registerMultiListener(long groupId, KUniverse origin, long[] objects, KEventMultiListener listener) {
        wrapped.registerMultiListener(groupId, origin, objects, listener);
    }

    @Override
    public void unregisterGroup(long groupId) {
        wrapped.unregisterGroup(groupId);
    }

    @Override
    public void send(KMessage msg) {
        //send locally
        wrapped.send(msg);
        //Send to remotes
        String payload = msg.json();
        for (int i = 0; i < _connectedChannels.size(); i++) {
            WebSocketChannel channel = _connectedChannels.get(i);
            WebSockets.sendText(payload, channel, null);
        }
    }

    @Override
    public void setManager(KMemoryManager manager) {
        this._manager = manager;
        wrapped.setManager(manager);
    }

}

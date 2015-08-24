package org.kevoree.modeling.drivers.websocket;

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
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.memory.manager.internal.KInternalDataManager;
import org.kevoree.modeling.memory.chunk.KIntMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.message.*;
import org.kevoree.modeling.message.impl.*;

import java.io.IOException;
import java.util.HashMap;

import static io.undertow.Handlers.websocket;

public class WebSocketGateway extends AbstractReceiveListener implements WebSocketConnectionCallback {

    private WebSocketGateway(KModel p_wrapped, int p_port, ClassLoader classLoader) {
        this.wrapped = p_wrapped;
        this._port = p_port;
        this._exposedClassLoader = classLoader;
    }

    public static WebSocketGateway exposeModel(KModel cdn, int port) {
        return exposeModelAndResources(cdn, port, null);
    }

    public static WebSocketGateway exposeModelAndResources(KModel cdn, int port, ClassLoader classLoader) {
        return new WebSocketGateway(cdn, port, classLoader);
    }

    private KModel wrapped = null;
    private ArrayIntMap<WebSocketChannel> _connectedChannels_hash = new ArrayIntMap<WebSocketChannel>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    private HashMap<Integer, Short> _hash_prefix = new HashMap<Integer, Short>();
    private Undertow _server = null;
    private String _address = "0.0.0.0";
    private int _port = 8080;
    private ClassLoader _exposedClassLoader = null;
    private int interceptorId = -1;

    public void start() {
        if (_exposedClassLoader != null) {
            _server = Undertow.builder().addHttpListener(_port, _address)
                    .setHandler(Handlers.path().addPrefixPath("/cdn", websocket(this)).addPrefixPath("/", Handlers.resource(new ClassPathResourceManager(_exposedClassLoader))))
                    .build();
        } else {
            _server = Undertow.builder().addHttpListener(_port, _address).setHandler(websocket(this)).build();
        }
        _server.start();
        interceptorId = ((KInternalDataManager) wrapped.manager()).cdn().addUpdateListener(new KContentUpdateListener() {
            @Override
            public void on(long[] updatedKeys) {
                KMessage message = new Message();
                message.setType(Message.EVENTS_TYPE);
                message.setKeys(updatedKeys);
                String payload = message.json();
                _connectedChannels_hash.each(new KIntMapCallBack<WebSocketChannel>() {
                    @Override
                    public void on(int key, WebSocketChannel channel) {
                        WebSockets.sendText(payload, channel, null);
                    }
                });
            }
        });
    }

    public void stop() {
        ((KInternalDataManager) wrapped.manager()).cdn().removeUpdateListener(interceptorId);
        _server.stop();
    }

    public void onConnect(WebSocketHttpExchange webSocketHttpExchange, WebSocketChannel webSocketChannel) {
        webSocketChannel.getReceiveSetter().set(this);
        webSocketChannel.resumeReceives();
        _connectedChannels_hash.put(webSocketChannel.hashCode(), webSocketChannel);
    }


    @Override
    protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
        _connectedChannels_hash.remove(webSocketChannel.hashCode());
        _hash_prefix.remove(webSocketChannel.hashCode());
        super.onClose(webSocketChannel, channel);
    }

    @Override
    protected void onFullTextMessage(final WebSocketChannel p_channel, BufferedTextMessage message) throws IOException {
        String payload = message.getData();
        KMessage msg = Message.load(payload);
        if(msg == null){
            System.err.println("ignored message:"+payload);
            return;
        }
        switch (msg.type()) {
            case Message.GET_REQ_TYPE: {
                ((KInternalDataManager) wrapped.manager()).cdn().get(msg.keys(), new KCallback<String[]>() {
                    public void on(String[] strings) {
                        KMessage getResultMessage = new Message();
                        getResultMessage.setType(Message.GET_RES_TYPE);
                        getResultMessage.setID(msg.id());
                        getResultMessage.setValues(strings);
                        WebSockets.sendText(getResultMessage.json(), p_channel, null);
                    }
                });
            }
            break;
            case Message.PUT_REQ_TYPE: {
                ((KInternalDataManager) wrapped.manager()).cdn().put(msg.keys(), msg.values(), new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        if (throwable == null) {
                            KMessage putResultMessage = new Message();
                            putResultMessage.setType(Message.PUT_RES_TYPE);
                            putResultMessage.setID(msg.id());
                            WebSockets.sendText(putResultMessage.json(), p_channel, null);

                            //inform everybody that somebody has written in the CDN
                            KMessage events = new Message();
                            events.setType(Message.EVENTS_TYPE);
                            events.setKeys(msg.keys());
                            String payload = events.json();
                            _connectedChannels_hash.each(new KIntMapCallBack<WebSocketChannel>() {
                                @Override
                                public void on(int key, WebSocketChannel pp_channel) {
                                    if (p_channel != pp_channel) {
                                        WebSockets.sendText(payload, pp_channel, null);
                                    }
                                }
                            });

                        }
                    }
                }, interceptorId);
            }
            break;
            case Message.ATOMIC_GET_INC_REQUEST_TYPE: {
                ((KInternalDataManager) wrapped.manager()).cdn().atomicGetIncrement(msg.keys(), new KCallback<Short>() {
                    @Override
                    public void on(Short s) {
                        if (s != null) {
                            KMessage atomicGetResultMessage = new Message();
                            atomicGetResultMessage.setType(Message.ATOMIC_GET_INC_RESULT_TYPE);
                            atomicGetResultMessage.setID(msg.id());
                            atomicGetResultMessage.setValues(new String[]{s.toString()});
                            _hash_prefix.put(p_channel.hashCode(), s);
                            WebSockets.sendText(atomicGetResultMessage.json(), p_channel, null);
                        }
                    }
                });
            }
            break;
            case Message.OPERATION_CALL_TYPE:
            case Message.OPERATION_RESULT_TYPE: {
                ((KInternalDataManager) wrapped.manager()).operationManager().dispatch(null, msg);
            }
            break;
            /*
            case KMessageLoader.EVENTS_TYPE: {
                Events events = (Events) msg;
                wrapped.manager().reload(events.allKeys(), null);
                //local listeners dispatch
                wrapped.manager().cdn().send(events);
            }
            break;
            */
            default: {
                System.err.println("Uh !. MessageType not supported:" + msg.type());
            }
        }
    }

}

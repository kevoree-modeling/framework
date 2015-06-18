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
import org.kevoree.modeling.cdn.KMessageInterceptor;
import org.kevoree.modeling.message.*;
import org.kevoree.modeling.message.impl.*;

import java.io.IOException;
import java.util.ArrayList;

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
    private ArrayList<WebSocketChannel> _connectedChannels = new ArrayList<WebSocketChannel>();
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
        interceptorId = wrapped.manager().cdn().addMessageInterceptor(new KMessageInterceptor() {
            @Override
            public boolean on(KMessage msg) {
                String payload = msg.json();
                for (int i = 0; i < _connectedChannels.size(); i++) {
                    WebSocketChannel channel = _connectedChannels.get(i);
                    WebSockets.sendText(payload, channel, null);
                }
                return false;
            }
        });
    }

    public void stop() {
        wrapped.manager().cdn().removeMessageInterceptor(interceptorId);
        _server.stop();
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
                wrapped.manager().cdn().get(getRequest.keys, new KCallback<String[]>() {
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
                wrapped.manager().cdn().put(putRequest.request, new KCallback<Throwable>() {
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
                wrapped.manager().cdn().atomicGetIncrement(atomicGetRequest.key, new KCallback<Short>() {
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
                wrapped.manager().operationManager().operationEventReceived(msg);
            }
            break;
            case KMessageLoader.EVENTS_TYPE: {
                Events events = (Events) msg;
                wrapped.manager().reload(events.allKeys(), null);
                //local listeners dispatch
                wrapped.manager().cdn().send(events);
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

}

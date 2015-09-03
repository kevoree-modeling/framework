package org.kevoree.modeling.drivers.websocket.gateway;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.kevoree.modeling.*;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.cdn.KContentUpdateListener;
import org.kevoree.modeling.cdn.impl.MemoryContentDeliveryDriver;
import org.kevoree.modeling.memory.chunk.KIntMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.message.*;
import org.kevoree.modeling.message.impl.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class WebSocketGateway implements WebSocketConnectionCallback, HttpHandler {

    private KContentDeliveryDriver wrapped = null;
    private ArrayIntMap<WebSocketChannel> _connectedChannels_hash = new ArrayIntMap<WebSocketChannel>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    private Undertow _server = null;
    private int _port = 8080;
    private int interceptorId = -1;
    private HashMap<String, GatewayRoom> _rooms;
    private AtomicInteger _keyGenerator = new AtomicInteger();

    private WebSocketGateway(KContentDeliveryDriver p_wrapped, int p_port) {
        this.wrapped = p_wrapped;
        this._port = p_port;
        this._rooms = new HashMap<String, GatewayRoom>();
    }

    public static WebSocketGateway expose(KContentDeliveryDriver cdn, int port) {
        if (cdn == null) {
            throw new RuntimeException("Bad usage of API, CDN parameter must be defined");
        }
        return new WebSocketGateway(cdn, port);
    }

    public static void main(String[] args) {
        KContentDeliveryDriver memoryDriver = new MemoryContentDeliveryDriver();
        expose(memoryDriver, 8080).start();
    }

    public void start() {
        HttpHandler handler = Handlers.websocket(this, this);
        _server = Undertow.builder().addHttpListener(_port, "0.0.0.0").setHandler(handler).build();
        _server.start();
        interceptorId = wrapped.addUpdateListener(new KContentUpdateListener() {
            @Override
            public void onKeysUpdate(long[] updatedKeys) {
                KMessage message = new Message();
                message.setType(Message.EVENTS_TYPE);
                message.setKeys(updatedKeys);
                String payload = message.save();
                _connectedChannels_hash.each(new KIntMapCallBack<WebSocketChannel>() {
                    @Override
                    public void on(int key, WebSocketChannel channel) {
                        WebSockets.sendText(payload, channel, null);
                    }
                });
            }

            @Override
            public void onOperationCall(KMessage operationCallMessage) {
                //TODO dispatch from one CDN to other, warning for loop
            }
        });
    }

    public void stop() {
        wrapped.removeUpdateListener(interceptorId);
        _server.stop();
    }

    @Override
    public void onConnect(WebSocketHttpExchange webSocketHttpExchange, WebSocketChannel webSocketChannel) {
        String rawURI = webSocketHttpExchange.getRequestURI();
        String query = webSocketHttpExchange.getQueryString();
        int querySize = 0;
        if (query.length() > 0) {
            querySize = query.length() + 1;
        }
        String roomId = rawURI.substring(1, rawURI.length() - querySize);
        if (roomId.length() == 0) {
            webSocketChannel.setCloseReason("RoomID should be defined!");
            try {
                webSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GatewayRoom resolvedRoom = _rooms.get(roomId);
        if (resolvedRoom == null) {
            resolvedRoom = createRoom(roomId);
        }
        webSocketChannel.getReceiveSetter().set(resolvedRoom);
        webSocketChannel.resumeReceives();
        List<String> peerIdList = webSocketHttpExchange.getRequestParameters().get("peerId");
        if (peerIdList != null && peerIdList.size() > 0) {
            resolvedRoom.attach(webSocketChannel, peerIdList.get(0));
        } else {
            resolvedRoom.attach(webSocketChannel, generatePeerID());
        }
    }

    private synchronized GatewayRoom createRoom(String roomId) {
        if (_rooms.containsKey(roomId)) {
            return _rooms.get(roomId);
        } else {
            GatewayRoom newRoom = new GatewayRoom(roomId, interceptorId, wrapped);
            _rooms.put(roomId, newRoom);
            return newRoom;
        }
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        httpServerExchange.getResponseSender().send("GeneratedRoomID=" + generateRoomID());
    }

    private String generateRoomID() {
        return "0";
    }

    private String generatePeerID() {
        return _keyGenerator.getAndUpdate(new IntUnaryOperator() {
            @Override
            public int applyAsInt(int operand) {
                if (operand == Integer.MAX_VALUE) {
                    return 0;
                } else {
                    return operand + 1;
                }
            }
        }) + "";
    }

}

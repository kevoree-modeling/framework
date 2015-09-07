package org.kevoree.modeling.drivers.websocket.gateway;

import io.undertow.websockets.core.*;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.memory.chunk.KStringMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayIntMap;
import org.kevoree.modeling.memory.chunk.impl.ArrayStringMap;
import org.kevoree.modeling.message.KMessage;
import org.kevoree.modeling.message.impl.Message;

import java.io.IOException;

public class GatewayRoom extends AbstractReceiveListener {

    private final String _name;
    private final KContentDeliveryDriver _cdn;
    private int _interceptorId;
    private ArrayStringMap<WebSocketChannel> _peerId_to_channel;
    private ArrayIntMap<String> _channelHash_to_peerId;
    private ArrayStringMap<String[]> _concatKey_to_peerIds;
    private ArrayStringMap<String[]> _peerId_to_keys;

    public GatewayRoom(String p_name, int p_interceptorId, KContentDeliveryDriver wrapperCdn) {
        this._name = p_name;
        this._peerId_to_channel = new ArrayStringMap<WebSocketChannel>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        this._channelHash_to_peerId = new ArrayIntMap<String>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        this._interceptorId = p_interceptorId;
        this._cdn = wrapperCdn;
        this._concatKey_to_peerIds = new ArrayStringMap<String[]>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
        this._peerId_to_keys = new ArrayStringMap<String[]>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
    }

    public String name() {
        return this._name;
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        onMessage(channel, message.getData());
    }

    private void onMessage(WebSocketChannel currentChannel, String payload) {
        KMessage msg = Message.load(payload);
        if (msg == null) {
            System.err.println("ignored message:" + payload);
            return;
        }
        switch (msg.type()) {
            case Message.GET_REQ_TYPE: {
                _cdn.get(msg.keys(), new KCallback<String[]>() {
                    public void on(String[] strings) {
                        KMessage getResultMessage = new Message();
                        getResultMessage.setType(Message.GET_RES_TYPE);
                        getResultMessage.setID(msg.id());
                        getResultMessage.setValues(strings);
                        WebSockets.sendText(getResultMessage.save(), currentChannel, null);
                    }
                });
            }
            break;
            case Message.PUT_REQ_TYPE: {
                _cdn.put(msg.keys(), msg.values(), new KCallback<Throwable>() {
                    @Override
                    public void on(Throwable throwable) {
                        if (throwable == null) {
                            KMessage putResultMessage = new Message();
                            putResultMessage.setType(Message.PUT_RES_TYPE);
                            putResultMessage.setID(msg.id());
                            WebSockets.sendText(putResultMessage.save(), currentChannel, null);
                            //inform everybody that somebody has written in the CDN
                            KMessage events = new Message();
                            events.setType(Message.EVENTS_TYPE);
                            events.setKeys(msg.keys());
                            String payload = events.save();
                            _peerId_to_channel.each(new KStringMapCallBack<WebSocketChannel>() {
                                @Override
                                public void on(String key, WebSocketChannel value) {
                                    if (currentChannel != value) {
                                        WebSockets.sendText(payload, value, null);
                                    }
                                }
                            });
                        }
                    }
                }, _interceptorId);
            }
            break;
            case Message.ATOMIC_GET_INC_REQUEST_TYPE: {
                _cdn.atomicGetIncrement(msg.keys(), new KCallback<Short>() {
                    @Override
                    public void on(Short s) {
                        if (s != null) {
                            KMessage atomicGetResultMessage = new Message();
                            atomicGetResultMessage.setType(Message.ATOMIC_GET_INC_RESULT_TYPE);
                            atomicGetResultMessage.setID(msg.id());
                            atomicGetResultMessage.setValues(new String[]{s.toString()});
                            WebSockets.sendText(atomicGetResultMessage.save(), currentChannel, null);
                        }
                    }
                });
            }
            break;
            case Message.OPERATION_CALL_TYPE: {
                String peerId = _channelHash_to_peerId.get(currentChannel.hashCode());
                msg.setPeer(peerId);
                //lookup for targets (first reply win!)
                String concatKey = msg.className() + "," + msg.operationName();
                String[] targets = _concatKey_to_peerIds.get(concatKey);
                String[] additionalClassNames = msg.values2();
                if (additionalClassNames != null && additionalClassNames.length > 0) {
                    int i = 0;
                    int additionaClassNamesSize = additionalClassNames.length;
                    while (targets == null && i < additionaClassNamesSize) {
                        concatKey = additionalClassNames[i] + "," + msg.operationName();
                        targets = _concatKey_to_peerIds.get(concatKey);
                        if (targets != null) {
                            msg.setClassName(additionalClassNames[i]);
                        }
                        i++;
                    }
                }
                if (targets == null || targets.length == 0) {
                    KMessage msgReply = new Message();
                    msgReply.setType(Message.OPERATION_RESULT_TYPE);
                    msgReply.setID(msg.id());
                    msgReply.setValues(null);
                    WebSockets.sendText(msgReply.save(), currentChannel, null);
                } else {
                    String newMsg = msg.save();
                    for (int i = 0; i < targets.length; i++) {
                        WebSocketChannel targetChannel = _peerId_to_channel.get(targets[i]);
                        if (targetChannel != null) {
                            WebSockets.sendText(newMsg, targetChannel, null);
                        }
                    }
                }
            }
            break;
            case Message.OPERATION_RESULT_TYPE: {
                WebSocketChannel targetChannel = _peerId_to_channel.get(msg.peer());
                WebSockets.sendText(payload, targetChannel, null);
            }
            break;
            case Message.OPERATION_MAPPING: {
                String[] values = msg.values();
                if(values != null) {
                    String[] concats = new String[values.length / 2];
                    for (int i = 0; i < values.length; i = i + 2) {
                        concats[i / 2] = values[i] + "," + values[i + 1];
                    }
                    String peerId = _channelHash_to_peerId.get(currentChannel.hashCode());
                    declareMapping(concats, peerId);
                }
            }
            break;
            default: {
                //IGNORE THE MESSAGE
            }
        }
    }

    private synchronized void declareMapping(String[] values, String originPeer) {
        for (int i = 0; i < values.length; i++) {
            String[] already = this._concatKey_to_peerIds.get(values[i]);
            if (already == null) {
                String[] newKeys = new String[1];
                newKeys[0] = originPeer;
                this._concatKey_to_peerIds.put(values[i], newKeys);
            } else {
                String[] newKeys = new String[already.length + 1];
                System.arraycopy(already, 0, newKeys, 0, already.length);
                newKeys[already.length] = originPeer;
                this._concatKey_to_peerIds.put(values[i], newKeys);
            }
        }
        this._peerId_to_keys.put(originPeer, values);
    }

    @Override
    protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
        String peerId = this._channelHash_to_peerId.get(webSocketChannel.hashCode());
        if (peerId != null) {
            this._peerId_to_channel.remove(peerId);
        }
        this._channelHash_to_peerId.remove(webSocketChannel.hashCode());
        //unDeclare mapping for operation
        //TODO

        super.onClose(webSocketChannel, channel);
    }

    public void attach(WebSocketChannel webSocketChannel, String peerId) {
        this._peerId_to_channel.put(peerId, webSocketChannel);
        this._channelHash_to_peerId.put(webSocketChannel.hashCode(), peerId);
    }

}

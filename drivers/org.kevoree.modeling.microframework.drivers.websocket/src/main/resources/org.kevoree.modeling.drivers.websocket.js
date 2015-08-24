///<reference path='../../../target/jsdeps/org.kevoree.modeling.microframework.browser.d.ts'/>
var org;
(function (org) {
    var kevoree;
    (function (kevoree) {
        var modeling;
        (function (modeling) {
            var drivers;
            (function (drivers) {
                var websocket;
                (function (websocket) {
                    var WebSocketCDNClient = (function () {
                        function WebSocketCDNClient(connectionUri) {
                            this._callbackId = 0;
                            this._reconnectionDelay = 3000;
                            this._getCallbacks = {};
                            this._putCallbacks = {};
                            this._atomicGetCallbacks = {};
                            this.listeners = [];
                            this.shouldBeConnected = false;
                            this._connectionUri = connectionUri;
                        }
                        WebSocketCDNClient.prototype.addUpdateListener = function (listener) {
                            var i = Math.random();
                            this.listeners[i] = listener;
                            return i;
                        };
                        WebSocketCDNClient.prototype.removeUpdateListener = function (id) {
                            delete this.listeners[id];
                        };
                        WebSocketCDNClient.prototype.connect = function (callback) {
                            var self = this;
                            this.shouldBeConnected = true;
                            if (typeof require !== "undefined") {
                                var wsNodeJS = require('ws');
                                this._clientConnection = new wsNodeJS(this._connectionUri);
                            }
                            else {
                                this._clientConnection = new WebSocket(this._connectionUri);
                            }
                            this._clientConnection.onmessage = function (message) {
                                var msg = org.kevoree.modeling.message.impl.Message.load(message.data);
                                switch (msg.type()) {
                                    case org.kevoree.modeling.message.impl.Message.GET_RES_TYPE:
                                        {
                                            var foundCB = self._getCallbacks[msg.id()];
                                            if (foundCB != null && foundCB != undefined) {
                                                foundCB(msg.values(), null);
                                            }
                                            delete self._getCallbacks[msg.id()];
                                        }
                                        break;
                                    case org.kevoree.modeling.message.impl.Message.PUT_RES_TYPE:
                                        {
                                            var foundCB = self._putCallbacks[msg.id()];
                                            if (foundCB != null && foundCB != undefined) {
                                                foundCB(null);
                                            }
                                            delete self._putCallbacks[msg.id()];
                                        }
                                        break;
                                    case org.kevoree.modeling.message.impl.Message.ATOMIC_GET_INC_RESULT_TYPE:
                                        {
                                            var foundCB = self._atomicGetCallbacks[msg.id()];
                                            if (foundCB != null && foundCB != undefined) {
                                                foundCB(msg.values()[0], null);
                                            }
                                            delete self._atomicGetCallbacks[msg.id()];
                                        }
                                        break;
                                    case org.kevoree.modeling.message.impl.Message.OPERATION_CALL_TYPE:
                                    case org.kevoree.modeling.message.impl.Message.OPERATION_RESULT_TYPE:
                                        {
                                        }
                                        break;
                                    case org.kevoree.modeling.message.impl.Message.EVENTS_TYPE:
                                        {
                                            for (var id in self.listeners) {
                                                var listener = self.listeners[id];
                                                listener(msg.keys());
                                            }
                                        }
                                        break;
                                    default:
                                        {
                                            console.log("MessageType not supported:" + msg.type());
                                        }
                                }
                            };
                            this._clientConnection.onerror = function (error) {
                                console.log(error);
                            };
                            this._clientConnection.onclose = function (error) {
                                if (self.shouldBeConnected) {
                                    console.log("Try reconnection in " + self._reconnectionDelay + " milliseconds.");
                                    //try to reconnect
                                    setTimeout(function () {
                                        self.connect(null);
                                    }, self._reconnectionDelay);
                                }
                            };
                            this._clientConnection.onopen = function () {
                                if (callback != null) {
                                    callback(null);
                                }
                            };
                        };
                        WebSocketCDNClient.prototype.close = function (callback) {
                            this.shouldBeConnected = false;
                            this._clientConnection.close();
                            if (callback != null) {
                                callback(null);
                            }
                        };
                        WebSocketCDNClient.prototype.nextKey = function () {
                            if (this._callbackId == 1000000) {
                                this._callbackId = 0;
                            }
                            else {
                                this._callbackId = this._callbackId + 1;
                            }
                            return this._callbackId;
                        };
                        WebSocketCDNClient.prototype.put = function (keys, values, error, ignoreInterceptor) {
                            var putRequest = new org.kevoree.modeling.message.impl.Message();
                            putRequest.setType(org.kevoree.modeling.message.impl.Message.PUT_REQ_TYPE);
                            putRequest.setID(this.nextKey());
                            putRequest.setKeys(keys);
                            putRequest.setValues(values);
                            this._putCallbacks[putRequest.id()] = error;
                            this._clientConnection.send(putRequest.json());
                        };
                        WebSocketCDNClient.prototype.get = function (keys, callback) {
                            var getRequest = new org.kevoree.modeling.message.impl.Message();
                            getRequest.setType(org.kevoree.modeling.message.impl.Message.GET_REQ_TYPE);
                            getRequest.setID(this.nextKey());
                            getRequest.setKeys(keys);
                            this._getCallbacks[getRequest.id()] = callback;
                            this._clientConnection.send(getRequest.json());
                        };
                        WebSocketCDNClient.prototype.atomicGetIncrement = function (keys, callback) {
                            var atomicGetRequest = new org.kevoree.modeling.message.impl.Message();
                            atomicGetRequest.setType(org.kevoree.modeling.message.impl.Message.ATOMIC_GET_INC_REQUEST_TYPE);
                            atomicGetRequest.setID(this.nextKey());
                            atomicGetRequest.setKeys(keys);
                            this._atomicGetCallbacks[atomicGetRequest.id()] = callback;
                            this._clientConnection.send(atomicGetRequest.json());
                        };
                        WebSocketCDNClient.prototype.remove = function (keys, error) {
                            console.error("Not implemented yet");
                        };
                        WebSocketCDNClient.prototype.peers = function () {
                            return ["Server"];
                        };
                        WebSocketCDNClient.prototype.sendToPeer = function (peer, msg) {
                            this._clientConnection.send(msg.json());
                        };
                        return WebSocketCDNClient;
                    })();
                    websocket.WebSocketCDNClient = WebSocketCDNClient;
                })(websocket = drivers.websocket || (drivers.websocket = {}));
            })(drivers = modeling.drivers || (modeling.drivers = {}));
        })(modeling = kevoree.modeling || (kevoree.modeling = {}));
    })(kevoree = org.kevoree || (org.kevoree = {}));
})(org || (org = {}));
//# sourceMappingURL=org.kevoree.modeling.drivers.websocket.js.map
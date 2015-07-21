///<reference path='../../../target/jsdeps/java.d.ts'/>
///<reference path='../../../target/jsdeps/org.kevoree.modeling.microframework.typescript.d.ts'/>
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
                            this._getCallbacks = new java.util.HashMap();
                            this._putCallbacks = new java.util.HashMap();
                            this._atomicGetCallbacks = new java.util.HashMap();
                            this.listeners = new Array();
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
                            var _this = this;
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
                                var msg = org.kevoree.modeling.message.KMessageLoader.load(message.data);
                                switch (msg.type()) {
                                    case org.kevoree.modeling.message.KMessageLoader.GET_RES_TYPE:
                                        {
                                            var getResult = msg;
                                            _this._getCallbacks.remove(getResult.id)(getResult.values, null);
                                        }
                                        break;
                                    case org.kevoree.modeling.message.KMessageLoader.PUT_RES_TYPE:
                                        {
                                            var putResult = msg;
                                            _this._putCallbacks.remove(putResult.id)(null);
                                        }
                                        break;
                                    case org.kevoree.modeling.message.KMessageLoader.ATOMIC_GET_INC_RESULT_TYPE:
                                        {
                                            var atomicGetResult = msg;
                                            _this._atomicGetCallbacks.remove(atomicGetResult.id)(atomicGetResult.value, null);
                                        }
                                        break;
                                    case org.kevoree.modeling.message.KMessageLoader.OPERATION_CALL_TYPE:
                                    case org.kevoree.modeling.message.KMessageLoader.OPERATION_RESULT_TYPE:
                                        {
                                        }
                                        break;
                                    case org.kevoree.modeling.message.KMessageLoader.EVENTS_TYPE:
                                        {
                                            var eventsMsg = msg;
                                            for (var id in _this.listeners) {
                                                var listener = _this.listeners[id];
                                                listener(eventsMsg.allKeys());
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
                            var putRequest = new org.kevoree.modeling.message.impl.PutRequest();
                            putRequest.id = this.nextKey();
                            putRequest.keys = keys;
                            putRequest.values = values;
                            this._putCallbacks.put(putRequest.id, error);
                            this._clientConnection.send(putRequest.json());
                        };
                        WebSocketCDNClient.prototype.get = function (keys, callback) {
                            var getRequest = new org.kevoree.modeling.message.impl.GetRequest();
                            getRequest.id = this.nextKey();
                            getRequest.keys = keys;
                            this._getCallbacks.put(getRequest.id, callback);
                            this._clientConnection.send(getRequest.json());
                        };
                        WebSocketCDNClient.prototype.atomicGetIncrement = function (key, callback) {
                            var atomicGetRequest = new org.kevoree.modeling.message.impl.AtomicGetIncrementRequest();
                            atomicGetRequest.id = this.nextKey();
                            atomicGetRequest.key = key;
                            this._atomicGetCallbacks.put(atomicGetRequest.id, callback);
                            this._clientConnection.send(atomicGetRequest.json());
                        };
                        WebSocketCDNClient.prototype.remove = function (keys, error) {
                            console.error("Not implemented yet");
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
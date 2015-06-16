/*
///<reference path='java.d.ts'/>
///<reference path='org.kevoree.modeling.microframework.typescript.d.ts'/>
*/
var org;
(function (org) {
    var kevoree;
    (function (kevoree) {
        var modeling;
        (function (modeling) {
            var database;
            (function (database) {
                var websocket;
                (function (websocket) {
                    var WebSocketClient = (function () {
                        function WebSocketClient(connectionUri) {
                            this._callbackId = 0;
                            this._reconnectionDelay = 3000;
                            this._localEventListeners = new org.kevoree.modeling.util.LocalEventListeners();
                            this._getCallbacks = new java.util.HashMap();
                            this._putCallbacks = new java.util.HashMap();
                            this._atomicGetCallbacks = new java.util.HashMap();
                            this._connectionUri = connectionUri;
                        }
                        WebSocketClient.prototype.connect = function (callback) {
                            var _this = this;
                            var self = this;
                            this._clientConnection = new WebSocket(this._connectionUri);
                            this._clientConnection.onmessage = function (message) {
                                var msg = org.kevoree.modeling.msg.KMessageLoader.load(message.data);
                                switch (msg.type()) {
                                    case org.kevoree.modeling.msg.KMessageLoader.GET_RES_TYPE:
                                        {
                                            var getResult = msg;
                                            _this._getCallbacks.remove(getResult.id)(getResult.values, null);
                                        }
                                        break;
                                    case org.kevoree.modeling.msg.KMessageLoader.PUT_RES_TYPE:
                                        {
                                            var putResult = msg;
                                            _this._putCallbacks.remove(putResult.id)(null);
                                        }
                                        break;
                                    case org.kevoree.modeling.msg.KMessageLoader.ATOMIC_GET_INC_RESULT_TYPE:
                                        {
                                            var atomicGetResult = msg;
                                            _this._atomicGetCallbacks.remove(atomicGetResult.id)(atomicGetResult.value, null);
                                        }
                                        break;
                                    case org.kevoree.modeling.msg.KMessageLoader.OPERATION_CALL_TYPE:
                                    case org.kevoree.modeling.msg.KMessageLoader.OPERATION_RESULT_TYPE:
                                        {
                                            _this._manager.operationManager().operationEventReceived(msg);
                                        }
                                        break;
                                    case org.kevoree.modeling.msg.KMessageLoader.EVENTS_TYPE:
                                        {
                                            var eventsMsg = msg;
                                            _this._manager.reload(eventsMsg.allKeys(), (function (error) {
                                                if (error != null) {
                                                    error.printStackTrace();
                                                }
                                                else {
                                                    this._localEventListeners.dispatch(eventsMsg);
                                                }
                                            }).bind(_this));
                                        }
                                        break;
                                    default:
                                        {
                                            console.log("MessageType not supported:" + msg.type());
                                        }
                                }
                            };
                            this._clientConnection.onerror = function (error) {
                                //console.log(error);
                            };
                            this._clientConnection.onclose = function (error) {
                                console.log("Try reconnection in " + self._reconnectionDelay + " milliseconds.");
                                //try to reconnect
                                setTimeout(function () {
                                    self.connect(null);
                                }, self._reconnectionDelay);
                            };
                            this._clientConnection.onopen = function () {
                                if (callback != null) {
                                    callback(null);
                                }
                            };
                        };
                        WebSocketClient.prototype.close = function (callback) {
                            this._clientConnection.close();
                            if (callback != null) {
                                callback(null);
                            }
                        };
                        WebSocketClient.prototype.nextKey = function () {
                            if (this._callbackId == 1000000) {
                                this._callbackId = 0;
                            }
                            else {
                                this._callbackId = this._callbackId + 1;
                            }
                            return this._callbackId;
                        };
                        WebSocketClient.prototype.put = function (request, error) {
                            var putRequest = new org.kevoree.modeling.msg.KPutRequest();
                            putRequest.id = this.nextKey();
                            putRequest.request = request;
                            this._putCallbacks.put(putRequest.id, error);
                            this._clientConnection.send(putRequest.json());
                        };
                        WebSocketClient.prototype.get = function (keys, callback) {
                            var getRequest = new org.kevoree.modeling.msg.KGetRequest();
                            getRequest.id = this.nextKey();
                            getRequest.keys = keys;
                            this._getCallbacks.put(getRequest.id, callback);
                            this._clientConnection.send(getRequest.json());
                        };
                        WebSocketClient.prototype.atomicGetMutate = function (key, operation, callback) {
                            var atomicGetRequest = new org.kevoree.modeling.msg.KAtomicGetRequest();
                            atomicGetRequest.id = this.nextKey();
                            atomicGetRequest.key = key;
                            atomicGetRequest.operation = operation;
                            this._atomicGetCallbacks.put(atomicGetRequest.id, callback);
                            this._clientConnection.send(atomicGetRequest.json());
                        };
                        WebSocketClient.prototype.remove = function (keys, error) {
                            console.error("Not implemented yet");
                        };
                        WebSocketClient.prototype.registerListener = function (groupId, origin, listener) {
                            this._localEventListeners.registerListener(groupId, origin, listener);
                        };
                        WebSocketClient.prototype.registerMultiListener = function (groupId, origin, objects, listener) {
                            this._localEventListeners.registerListenerAll(groupId, origin.key(), objects, listener);
                        };
                        WebSocketClient.prototype.unregisterGroup = function (groupId) {
                            this._localEventListeners.unregister(groupId);
                        };
                        WebSocketClient.prototype.setManager = function (manager) {
                            this._manager = manager;
                            this._localEventListeners.setManager(manager);
                        };
                        WebSocketClient.prototype.send = function (msg) {
                            //Send to remote
                            this._localEventListeners.dispatch(msg);
                            this._clientConnection.send(msg.json());
                        };
                        return WebSocketClient;
                    })();
                    websocket.WebSocketClient = WebSocketClient;
                })(websocket = database.websocket || (database.websocket = {}));
            })(database = modeling.database || (modeling.database = {}));
        })(modeling = kevoree.modeling || (kevoree.modeling = {}));
    })(kevoree = org.kevoree || (org.kevoree = {}));
})(org || (org = {}));
//# sourceMappingURL=org.kevoree.modeling.database.websocket.WebSocket.js.map
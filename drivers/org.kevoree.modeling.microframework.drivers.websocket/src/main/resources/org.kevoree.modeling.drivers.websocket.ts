///<reference path='../../../target/jsdeps/org.kevoree.modeling.microframework.browser.d.ts'/>

declare function require(p);

module org {
    export module kevoree {
        export module modeling {
            export module drivers {
                export module websocket {
                    export class WebSocketPeer implements org.kevoree.modeling.cdn.KContentDeliveryDriver {
                        private _callbackId = 0;
                        private _reconnectionDelay = 3000;
                        private _clientConnection:WebSocket;
                        private _connectionUri:string;
                        private _callbacks = {};
                        constructor(connectionUri) {
                            this._connectionUri = connectionUri;
                        }
                        listeners = [];
                        shouldBeConnected = false;
                        public addUpdateListener(listener):number {
                            var i = Math.random();
                            this.listeners[i] = listener;
                            return i;
                        }

                        public removeUpdateListener(id):void {
                            delete this.listeners[id];
                        }

                        public connect(callback:org.kevoree.modeling.KCallback<Error>):void {
                            var self = this;
                            this.shouldBeConnected = true;

                            if (typeof require !== "undefined") {
                                var wsNodeJS = require('ws');
                                this._clientConnection = new wsNodeJS(this._connectionUri);
                            } else {
                                this._clientConnection = new WebSocket(this._connectionUri);
                            }

                            this._clientConnection.onmessage = (message) => {
                                var msg = org.kevoree.modeling.message.impl.Message.load(message.data);
                                switch (msg.type()) {
                                    case org.kevoree.modeling.message.impl.Message.GET_RES_TYPE:
                                    {
                                        var foundCB = self._callbacks[msg.id()];
                                        if (foundCB != null && foundCB != undefined) {
                                            foundCB(msg.values(), null);
                                        }
                                        delete self._callbacks[msg.id()];
                                    }
                                        break;
                                    case org.kevoree.modeling.message.impl.Message.PUT_RES_TYPE:
                                    {
                                        var foundCB = self._callbacks[msg.id()];
                                        if (foundCB != null && foundCB != undefined) {
                                            foundCB(null);
                                        }
                                        delete self._callbacks[msg.id()];
                                    }
                                        break;
                                    case org.kevoree.modeling.message.impl.Message.ATOMIC_GET_INC_RESULT_TYPE:
                                    {
                                        var foundCB = self._callbacks[msg.id()];
                                        if (foundCB != null && foundCB != undefined) {
                                            foundCB(msg.values()[0], null);
                                        }
                                        delete self._callbacks[msg.id()];
                                    }
                                        break;
                                    case org.kevoree.modeling.message.impl.Message.OPERATION_CALL_TYPE:{
                                        for (var id in self.listeners) {
                                            var listener = self.listeners[id];
                                            listener.onOperationCall(msg.keys());
                                        }
                                    } break;
                                    case org.kevoree.modeling.message.impl.Message.OPERATION_RESULT_TYPE:
                                    {
                                        var foundCB = self._callbacks[msg.id()];
                                        if (foundCB != null) {
                                            foundCB.on(msg);
                                        }
                                    }
                                        break;
                                    case org.kevoree.modeling.message.impl.Message.EVENTS_TYPE:
                                    {
                                        for (var id in self.listeners) {
                                            var listener = self.listeners[id];
                                            listener.onKeysUpdate(msg.keys());
                                        }
                                    }
                                        break;
                                    default:
                                    {
                                        console.log("MessageType not supported:" + msg.type())
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
                                        self.connect(null)
                                    }, self._reconnectionDelay);
                                }
                            };
                            this._clientConnection.onopen = function () {
                                if (callback != null) {
                                    callback(null);
                                }
                            };
                        }

                        public close(callback:(p:Error) => void):void {
                            this.shouldBeConnected = false;
                            this._clientConnection.close();
                            if (callback != null) {
                                callback(null);
                            }
                        }

                        private nextKey():number {
                            if (this._callbackId == 1000000) {
                                this._callbackId = 0;
                            } else {
                                this._callbackId = this._callbackId + 1;
                            }
                            return this._callbackId;
                        }

                        public put(keys:Float64Array, values:string[], error:org.kevoree.modeling.KCallback<Error>, ignoreInterceptor):void {
                            var putRequest = new org.kevoree.modeling.message.impl.Message();
                            putRequest.setType(org.kevoree.modeling.message.impl.Message.PUT_REQ_TYPE);
                            putRequest.setID(this.nextKey());
                            putRequest.setKeys(keys);
                            putRequest.setValues(values);
                            this._callbacks[putRequest.id()] = error;
                            this._clientConnection.send(putRequest.save());
                        }

                        public get(keys:Float64Array, callback:(p:string[], p1:Error) => void):void {
                            var getRequest = new org.kevoree.modeling.message.impl.Message();
                            getRequest.setType(org.kevoree.modeling.message.impl.Message.GET_REQ_TYPE);
                            getRequest.setID(this.nextKey());
                            getRequest.setKeys(keys);
                            this._callbacks[getRequest.id()] = callback;
                            this._clientConnection.send(getRequest.save());
                        }

                        public atomicGetIncrement(keys:Float64Array, callback:(p:number, p1:Error) => void):void {
                            var atomicGetRequest = new org.kevoree.modeling.message.impl.Message();
                            atomicGetRequest.setType(org.kevoree.modeling.message.impl.Message.ATOMIC_GET_INC_REQUEST_TYPE);
                            atomicGetRequest.setID(this.nextKey());
                            atomicGetRequest.setKeys(keys);
                            this._callbacks[atomicGetRequest.id()] = callback;
                            this._clientConnection.send(atomicGetRequest.save());
                        }

                        public remove(keys:Float64Array, error:(p:Error) => void):void {
                            console.error("Not implemented yet");
                        }

                        public peers():string[] {
                            return ["Server"];
                        }

                        public sendToPeer(peer:string, msg:org.kevoree.modeling.message.KMessage, callback:org.kevoree.modeling.KCallback<org.kevoree.modeling.message.KMessage>) {
                            if(callback != null){
                                msg.setID(this.nextKey());
                                this._callbacks[msg.id()] = callback;
                            }
                            this._clientConnection.send(msg.save());
                        }

                    }
                }
            }
        }
    }
}
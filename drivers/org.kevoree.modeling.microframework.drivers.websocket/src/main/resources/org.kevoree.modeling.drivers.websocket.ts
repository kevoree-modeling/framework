///<reference path='../../../target/jsdeps/org.kevoree.modeling.microframework.typescript.d.ts'/>

declare function require(p);

module org {
    export module kevoree {
        export module modeling {
            export module drivers {
                export module websocket {
                    export class WebSocketCDNClient implements org.kevoree.modeling.cdn.KContentDeliveryDriver {

                        private _callbackId = 0;
                        private _reconnectionDelay = 3000;
                        private _clientConnection:WebSocket;
                        private _connectionUri:string;

                        private _getCallbacks = {};
                        private _putCallbacks = {};
                        private _atomicGetCallbacks = {};

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


                        public connect(callback:(p:java.lang.Throwable) => void):void {
                            var self = this;

                            this.shouldBeConnected = true;

                            if (typeof require !== "undefined") {
                                var wsNodeJS = require('ws');
                                this._clientConnection = new wsNodeJS(this._connectionUri);
                            } else {
                                this._clientConnection = new WebSocket(this._connectionUri);
                            }

                            this._clientConnection.onmessage = (message) => {
                                var msg = org.kevoree.modeling.message.KMessageLoader.load(message.data);
                                switch (msg.type()) {
                                    case org.kevoree.modeling.message.KMessageLoader.GET_RES_TYPE:
                                    {
                                        var getResult = <org.kevoree.modeling.message.impl.GetResult>msg;
                                        var foundCB = self._getCallbacks[getResult.id];
                                        if (foundCB != null && foundCB != undefined) {
                                            foundCB(getResult.values, null);
                                        }
                                        delete self._getCallbacks[getResult.id];
                                    }
                                        break;
                                    case org.kevoree.modeling.message.KMessageLoader.PUT_RES_TYPE:
                                    {
                                        var putResult = <org.kevoree.modeling.message.impl.PutResult>msg;
                                        var foundCB = self._putCallbacks[putResult.id];
                                        if (foundCB != null && foundCB != undefined) {
                                            foundCB(null);
                                        }
                                        delete self._putCallbacks[putResult.id];
                                    }
                                        break;
                                    case org.kevoree.modeling.message.KMessageLoader.ATOMIC_GET_INC_RESULT_TYPE:
                                    {
                                        var atomicGetResult = <org.kevoree.modeling.message.impl.AtomicGetIncrementResult>msg;
                                        var foundCB = self._atomicGetCallbacks[atomicGetResult.id];
                                        if (foundCB != null && foundCB != undefined) {
                                            foundCB(atomicGetResult.value, null);
                                        }
                                        delete self._atomicGetCallbacks[atomicGetResult.id];
                                    }
                                        break;
                                    case org.kevoree.modeling.message.KMessageLoader.OPERATION_CALL_TYPE:
                                    case org.kevoree.modeling.message.KMessageLoader.OPERATION_RESULT_TYPE:
                                    {
                                        //this._manager.operationManager().operationEventReceived(<org.kevoree.modeling.message.KMessage>msg);
                                    }
                                        break;
                                    case org.kevoree.modeling.message.KMessageLoader.EVENTS_TYPE:
                                    {
                                        var eventsMsg = <org.kevoree.modeling.message.impl.Events>msg;
                                        for (var id in self.listeners) {
                                            var listener = self.listeners[id];
                                            listener(eventsMsg.allKeys());
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

                        public close(callback:(p:java.lang.Throwable) => void):void {
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

                        public put(keys:Float64Array, values:string[], error:(p:java.lang.Throwable) => void, ignoreInterceptor):void {
                            var putRequest = new org.kevoree.modeling.message.impl.PutRequest();
                            putRequest.id = this.nextKey();
                            putRequest.keys = keys;
                            putRequest.values = values;
                            this._putCallbacks[putRequest.id] = error;
                            this._clientConnection.send(putRequest.json());
                        }

                        public get(keys:Float64Array, callback:(p:string[], p1:java.lang.Throwable) => void):void {
                            var getRequest = new org.kevoree.modeling.message.impl.GetRequest();
                            getRequest.id = this.nextKey();
                            getRequest.keys = keys;
                            this._getCallbacks[getRequest.id] = callback;
                            this._clientConnection.send(getRequest.json());
                        }

                        public atomicGetIncrement(keys:Float64Array, callback:(p:number, p1:java.lang.Throwable) => void):void {
                            var atomicGetRequest = new org.kevoree.modeling.message.impl.AtomicGetIncrementRequest();
                            atomicGetRequest.id = this.nextKey();
                            atomicGetRequest.keys = keys;
                            this._atomicGetCallbacks[atomicGetRequest.id] = callback;
                            this._clientConnection.send(atomicGetRequest.json());
                        }

                        public remove(keys:Float64Array, error:(p:java.lang.Throwable) => void):void {
                            console.error("Not implemented yet");
                        }

                    }
                }
            }
        }
    }
}
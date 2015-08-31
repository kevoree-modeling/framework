var fs = require('fs');

// file is included here:
eval(fs.readFileSync(__dirname + '/../jsdeps/org.kevoree.modeling.microframework.browser.js') + '');
eval(fs.readFileSync(__dirname + '/../classes/org.kevoree.modeling.drivers.websocket.js') + '');

var dynamicMM = new org.kevoree.modeling.meta.impl.MetaModel("mock");
var dynamicSensorClass = dynamicMM.addMetaClass("sensor");
dynamicSensorClass.addAttribute("name", org.kevoree.modeling.meta.KPrimitiveTypes.STRING);
dynamicSensorClass.addAttribute("value", org.kevoree.modeling.meta.KPrimitiveTypes.CONTINUOUS);
var model = dynamicMM.createModel(org.kevoree.modeling.memory.manager.DataManagerBuilder.create()
    .withContentDeliveryDriver(new org.kevoree.modeling.drivers.websocket.WebSocketPeer("ws://localhost:9000/testRoomId?peerId=jspeer"))
    .build());
model.connect(function () {
    model.lookup(0, 0, 1, function (obj) {
        if ('{"universe":0,"time":0,"uuid":1,"data":{"name":"MyName","value":[0,1,0,0,42.42]}}' != obj.toJSON()) {
            throw Exception("Bad Result");
        } else {
            console.log(obj.toJSON());
        }

        var obj = model.createByName("sensor", 0, 0);
        obj.setByName("name", "sensor#3");
        obj.setByName("value", 42.52);
        console.log(obj.toJSON());

        model.save(function () {
            model.disconnect(function () {
            });
        });

    });
});

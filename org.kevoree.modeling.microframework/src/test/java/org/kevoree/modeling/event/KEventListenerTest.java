package org.kevoree.modeling.event;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.*;
import org.kevoree.modeling.KContentKey;
import org.kevoree.modeling.event.KEventListener;
import org.kevoree.modeling.event.KEventMultiListener;
import org.kevoree.modeling.event.impl.LocalEventListeners;
import org.kevoree.modeling.extrapolation.impl.DiscreteExtrapolation;
import org.kevoree.modeling.meta.KMeta;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.message.impl.Events;
import org.kevoree.modeling.meta.impl.MetaModel;

/**
 * Created by duke on 22/03/15.
 */
public class KEventListenerTest {

    private KModel _model;

    private LocalEventListeners localEventListeners;

    private KView t0;

    public KEventListenerTest() {
        MetaModel metaModel = new MetaModel("TestMM");
        KMetaClass metaClass = metaModel.addMetaClass("TestMC");
        metaClass.addAttribute("name", KPrimitiveTypes.STRING);
        _model = metaModel.model();
        localEventListeners = new LocalEventListeners();
        localEventListeners.setManager(_model.manager());
        _model.connect(null);
        t0 = _model.universe(0).time(0);
    }

    @Test
    public void eventListenerTest() {
        KObject obj = t0.create(_model.metaModel().metaClassByName("TestMC"));
        int[] counter = new int[]{0};
        KEventListener listener = new KEventListener() {
            @Override
            public void on(KObject src, KMeta[] modifications) {
                counter[0]++;
            }
        };
        localEventListeners.registerListener(0, obj, listener);
        Events events = new Events(1);
        int metaNameIndex = obj.metaClass().attribute("name").index();
        int[] metas = new int[]{metaNameIndex};
        events.setEvent(0, KContentKey.createObject(obj.universe(), obj.now(), obj.uuid()), metas);
        localEventListeners.dispatch(events);
        Assert.assertEquals(counter[0], 1);
        localEventListeners.registerListener(0, obj, listener);
        //test the double registration
        localEventListeners.dispatch(events);
        Assert.assertEquals(counter[0], 3);
        //drop group 0
        localEventListeners.unregister(0);
        localEventListeners.dispatch(events);
        Assert.assertEquals(counter[0], 3);
    }

    @Test
    public void multiEventListenerTest() {
        KObject obj = t0.create(_model.metaModel().metaClassByName("TestMC"));
        KObject obj2 = t0.create(_model.metaModel().metaClassByName("TestMC"));
        int[] counter = new int[]{0};
        KEventMultiListener multiListener = new KEventMultiListener() {
            @Override
            public void on(KObject[] objects) {
                counter[0] = counter[0] + objects.length;
            }
        };
        long[] toListen = new long[1];
        toListen[0] = obj.uuid();
        localEventListeners.registerListenerAll(0, t0.universe(), toListen, multiListener);
        Events events = new Events(1);
        int metaNameIndex = obj.metaClass().attribute("name").index();
        int[] metas = new int[]{metaNameIndex};
        events.setEvent(0, KContentKey.createObject(obj.universe(), obj.now(), obj.uuid()), metas);
        localEventListeners.dispatch(events);
        Assert.assertEquals(counter[0], 1);
        counter[0] = 0;
        localEventListeners.unregister(0);
        localEventListeners.dispatch(events);
        Assert.assertEquals(counter[0], 0);
        Events events2 = new Events(2);
        events2.setEvent(0, KContentKey.createObject(obj.universe(), obj.now(), obj.uuid()), metas);
        events2.setEvent(1, KContentKey.createObject(obj2.universe(), obj.now(), obj.uuid()), metas);
        long[] toListen2 = new long[2];
        toListen2[0] = obj.uuid();
        toListen2[1] = obj2.uuid();
        localEventListeners.registerListenerAll(0, t0.universe(), toListen2, multiListener);
        localEventListeners.dispatch(events2);
        Assert.assertEquals(counter[0], 2);
    }


}

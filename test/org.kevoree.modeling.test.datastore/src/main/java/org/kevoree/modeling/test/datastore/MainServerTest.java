package org.kevoree.modeling.test.datastore;

import geometry.*;
import geometry.meta.MetaLibrary;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.drivers.websocket.WebSocketGateway;
import org.kevoree.modeling.memory.chunk.KMemorySegment;
import org.kevoree.modeling.operation.KOperation;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by gregory.nain on 10/11/14.
 */
public class MainServerTest {
    public static String[] colors = new String[]{"red", "green", "blue"};

    public static void main(String[] args) {

        Long originOfTime = 0L;

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        //MemoryKContentDeliveryDriver.DEBUG = true;

        GeometryModel geoModel = new GeometryModel();
        WebSocketGateway.exposeModel(geoModel, 23664).start();


        geoModel.setOperation(MetaLibrary.OP_ADDSHAPE, new KOperation() {
            public void on(KObject source, Object[] params, KCallback<Object> result) {
                GeometryUniverse dimension = geoModel.universe(0);
                GeometryView geoFactory = dimension.time(originOfTime);
                geoFactory.getRoot(new KCallback<KObject>() {
                    @Override
                    public void on(KObject kObject) {
                        if (kObject != null) {
                            Library lib = (Library) kObject;
                            lib.addShapes(geoFactory.createShape().setName("Shape" + params[0]).setColor("grey"));
                            geoModel.save(Utils.DefaultPrintStackTraceCallback);

                            System.out.println("Shape added by operation");
                            result.on("true");
                        } else {
                            System.out.println("Shape not added, root not found");
                            result.on("false");
                        }
                    }
                });
            }
        });


        geoModel.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    GeometryUniverse dimension = geoModel.universe(0);
                    GeometryView geoFactory = dimension.time(originOfTime);
                    geoFactory.getRoot(new KCallback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            if (kObject == null) {
                                Library lib = geoFactory.createLibrary();
                                KMemorySegment libEntry = lib.manager().segment(lib.universe(), lib.now(), lib.uuid(), false, lib.metaClass(), null);
                                long[] uuids = (long[]) libEntry.get(MetaLibrary.REF_SHAPES.index(), lib.metaClass());
                                geoFactory.setRoot(lib, new KCallback<Throwable>() {
                                    @Override
                                    public void on(Throwable throwable) {
                                        if (throwable != null) {
                                            throwable.printStackTrace();
                                        }
                                    }
                                });
                                for (int i = 0; i < 200; i++) {
                                    lib.addShapes(geoFactory.createShape().setName("ShapeO" + i).setColor(colors[i % 3]));
                                }
                                geoModel.save(Utils.DefaultPrintStackTraceCallback);

                                System.out.println("Base model committed");
                            }
                        }
                    });
                }
            }
        });



/*

        Semaphore s = new Semaphore(0);
        try {
            s.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        Runnable task = new Runnable() {
            int turn = 0, i = 0;

            public void run() {
                try {
                    GeometryUniverse dimension = geoModel.universe(0);
                    GeometryView geoFactory = dimension.time(originOfTime);
                    geoFactory.getRoot(new KCallback<KObject>() {
                        @Override
                        public void on(KObject kObject) {
                            if (kObject == null) {
                                System.err.println("Root not found");
                            } else {
                                Library root = (Library) kObject;
                                KMemorySegment entry = root.manager().segment(root.universe(), root.now(), root.uuid(), false, root.metaClass(), null);
                                root.getShapes((shapes) -> {
                                    System.out.println("Shapes:" + shapes.length);
                                    if (shapes != null) {
                                        for (Shape shape : shapes) {
                                            i++;
                                            shape.setColor(colors[(turn + i) % 3]);
                                        }
                                    }
                                });
                                i = 0;
                                geoModel.save(Utils.DefaultPrintStackTraceCallback);
                            }
                        }
                    });
                    turn++;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        executor.scheduleWithFixedDelay(task, 3000, 2000, TimeUnit.MILLISECONDS);
    }
}

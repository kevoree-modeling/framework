package org.kevoree.modeling.databases.websocket;

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSocketVersion;
import org.xnio.BufferAllocator;
import org.xnio.ByteBufferSlicePool;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;
import io.undertow.websockets.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by gregory.nain on 24/02/15.
 */
class UndertowWSClient {

    private ByteBufferSlicePool _buffer;
    private XnioWorker _worker;
    private WebSocketChannel _webSocketChannel = null;
    private String _url;

    public UndertowWSClient(String url) {
        this._url = url;
        try {
            Xnio xnio = Xnio.getInstance(io.undertow.websockets.client.WebSocketClient.class.getClassLoader());
            _worker = xnio.createWorker(OptionMap.builder()
                    .set(Options.WORKER_IO_THREADS, 2)
                    .set(Options.CONNECTION_HIGH_WATER, 1000000)
                    .set(Options.CONNECTION_LOW_WATER, 1000000)
                    .set(Options.WORKER_TASK_CORE_THREADS, 30)
                    .set(Options.WORKER_TASK_MAX_THREADS, 30)
                    .set(Options.TCP_NODELAY, true)
                    .set(Options.CORK, true)
                    .getMap());

            _buffer = new ByteBufferSlicePool(BufferAllocator.BYTE_BUFFER_ALLOCATOR, 1024, 1024);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(AbstractReceiveListener listener) {
        try {

            _webSocketChannel = WebSocketClient.connect(_worker, _buffer, OptionMap.EMPTY, new URI(_url), WebSocketVersion.V13).get();
            _webSocketChannel.getReceiveSetter().set(listener);
            _webSocketChannel.resumeReceives();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            _webSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WebSocketChannel getChannel() {
        return _webSocketChannel;
    }

}

package com.twitter.sdk.organic.core.test;

import com.github.mike10004.twitter.organic.Uri;
import com.google.common.net.HostAndPort;
import com.google.common.net.MediaType;
import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class NanoServer implements java.io.Closeable {

    private final NanoHTTPD server;

    private final List<RequestHandler> requestHandlers = Collections.synchronizedList(new ArrayList<>());
    private final RequestHandler defaultRequestHandler = RequestHandler.getDefault();
    private final AtomicLong numRequestsMatched = new AtomicLong(0L);
    private final AtomicLong numRequestsHeard = new AtomicLong(0L);

    public interface RequestHandler {
        @Nullable
        NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) throws IOException;

        NanoHTTPD.Response NOT_FOUND_RESPONSE = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", "404 Not Found");

        static RequestHandler getDefault() {
            return (session) -> {
                LoggerFactory.getLogger(RequestHandler.class.getName() + ".default").debug("404 {} {}", session.getUri(), StringUtils.abbreviate(session.getQueryParameterString(), 128));
                return NOT_FOUND_RESPONSE;
            };
        }

    }

    public static NanoHTTPD.Response response(int status, MediaType contentType, String contentText) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.lookup(status), contentType.toString(), contentText);
    }

    public NanoServer() throws IOException {
        this(findUnusedPort());
    }

    private static int findUnusedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    public NanoServer(int port) throws IOException {
        checkArgument( port > 0 && port < 65536, "port %s", port);
        server = new NanoHTTPD(port) {
            @Override
            public Response serve(IHTTPSession session) {
                numRequestsHeard.incrementAndGet();
                for (RequestHandler handler : requestHandlers) {
                    Response response;
                    try {
                        response = handler.serve(session);
                    } catch (IOException e) {
                        LoggerFactory.getLogger(getClass()).error("handler threw exception", e);
                        response = response(500, MediaType.PLAIN_TEXT_UTF_8, e.toString());
                    }
                    if (response != null) {
                        numRequestsMatched.incrementAndGet();
                        return response;
                    }
                }
                try {
                    return defaultRequestHandler.serve(session);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        server.start();
    }

    @Override
    public void close() throws IOException {
        if (server.wasStarted()) {
            server.stop();
        }
    }

    @SuppressWarnings("unused")
    public NanoHTTPD getDaemon() {
        return server;
    }

    public HostAndPort getSocketAddress() {
        checkState(server != null, "server not instantiated yet");
        return HostAndPort.fromParts("localhost", server.getListeningPort());
    }

    @SuppressWarnings("UnusedReturnValue")
    public NanoServer handle(RequestHandler requestHandler) {
        requestHandlers.add(requestHandler);
        return this;
    }

    public Uri.Builder buildUri() {
        return new Uri.Builder()
                .scheme("http")
                .authority(getSocketAddress().toString())
                .path("/");
    }

    public long getNumRequestsHeard() {
        return numRequestsHeard.get();
    }

    public long getNumRequestsMatched() {
        return numRequestsMatched.get();
    }
}

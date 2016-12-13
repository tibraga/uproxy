package io.galeb.validate.uproxy;

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


import io.undertow.Undertow;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.ExclusivityChecker;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.Headers;
import org.xnio.Options;

import java.net.URI;
import java.net.URISyntaxException;

public class ReverseProxyServer {

    private static final String BACKEND_URI             = System.getProperty("client.backend", "http://127.0.0.1:80");
    private static final int    ROUTER_PORT             = Integer.parseInt(System.getProperty("server.port", "8000"));
    private static final int    MAX_REQUEST_TIME        = Integer.parseInt(System.getProperty("server.maxRequestTime", "30000"));
    private static final int    CONNECTIONS_PER_THREAD  = Integer.parseInt(System.getProperty("client.connectionsPerThread", "20"));
    private static final int    IO_THREADS              = Integer.parseInt(System.getProperty("server.ioThread", "4"));
    private static final int    WORKER_THREADS          = Integer.parseInt(System.getProperty("server.workerThreads", Integer.toString(Runtime.getRuntime().availableProcessors()*8)));
    private static final int    WORKER_TASK_MAX_THREADS = Integer.parseInt(System.getProperty("server.workerTaskMaxThreads", Integer.toString(WORKER_THREADS)));
    private static final int    BACKLOG                 = Integer.parseInt(System.getProperty("server.backlog", "1000"));
    private static final boolean REWRITE_HOST_HEADER    = Boolean.valueOf(System.getProperty("server.rewriteHostHeader", Boolean.toString(false)));
    private static final boolean REUSE_X_FORWARDED      = Boolean.valueOf(System.getProperty("server.reuseXForwarded", Boolean.toString(true)));

    public static void main(final String[] args) {

        if (args.length > 0 && "-h".equals(args[0])) {
            System.out.println(
                "client.backend,              default: http://127.0.0.1:80\n" +
                "server.port,                 default: 8000\n" +
                "server.maxRequestTime,       default: 30000\n" +
                "client.connectionsPerThread, default: 20\n" +
                "server.ioThread,             default: 4\n" +
                "server.workerThreads,        default: Runtime.getRuntime().availableProcessors()*8\n" +
                "server.workerTaskMaxThreads, default: server.workerThreads\n" +
                "server.backlog,              default: 1000\n" +
                "server.rewriteHostHeader,    default: false\n" +
                "server.reuseXForwarded,      default: true"
            );
            System.exit(0);
        }

        try {
            final ExclusivityChecker exclusivityChecker = exchange -> {
                // we always create a new connection for upgrade requests
                return exchange.getRequestHeaders().contains(Headers.UPGRADE);
            };

            LoadBalancingProxyClient loadBalancer = new LoadBalancingProxyClient(exclusivityChecker)
                    .addHost(new URI(BACKEND_URI))
                    .setConnectionsPerThread(CONNECTIONS_PER_THREAD);

            Undertow reverseProxy = Undertow.builder()
                    .addHttpListener(ROUTER_PORT, "0.0.0.0")
                    .setIoThreads(IO_THREADS)
                    .setWorkerThreads(WORKER_THREADS)
                    .setWorkerOption(Options.WORKER_TASK_MAX_THREADS, WORKER_TASK_MAX_THREADS)
                    .setSocketOption(Options.BACKLOG, BACKLOG)
                    .setHandler(new ProxyHandler(loadBalancer, MAX_REQUEST_TIME, ResponseCodeHandler.HANDLE_404, REWRITE_HOST_HEADER, REUSE_X_FORWARDED))
                    .build();

            reverseProxy.start();

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}

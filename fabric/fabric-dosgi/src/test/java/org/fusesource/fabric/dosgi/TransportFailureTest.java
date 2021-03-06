/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.fabric.dosgi;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.fusesource.fabric.dosgi.api.AsyncCallback;
import org.fusesource.fabric.dosgi.api.AsyncCallbackFuture;
import org.fusesource.fabric.dosgi.api.ProtobufSerializationStrategy;
import org.fusesource.fabric.dosgi.api.SerializationStrategy;
import org.fusesource.fabric.dosgi.io.ServerInvoker;
import org.fusesource.fabric.dosgi.tcp.ClientInvokerImpl;
import org.fusesource.fabric.dosgi.tcp.ServerInvokerImpl;
import org.fusesource.hawtdispatch.Dispatch;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TransportFailureTest {

    @Test
    public void testInvoke() throws Exception {

        DispatchQueue queue = Dispatch.createQueue();
        HashMap<String, SerializationStrategy> map = new HashMap<String, SerializationStrategy>();
        map.put("protobuf", new ProtobufSerializationStrategy());

        ServerInvokerImpl server = new ServerInvokerImpl("tcp://localhost:0", queue, map);
        server.start();

        ClientInvokerImpl client = new ClientInvokerImpl(queue, map);
        client.start();

        try {
            server.registerService("service-id", new ServerInvoker.ServiceFactory() {
                public Object get() {
                    return new HelloImpl();
                }
                public void unget() {
                }
            }, HelloImpl.class.getClassLoader());


            InvocationHandler handler = client.getProxy(server.getConnectAddress(), "service-id", HelloImpl.class.getClassLoader());
            Hello hello  = (Hello) Proxy.newProxyInstance(HelloImpl.class.getClassLoader(), new Class[]{Hello.class}, handler);

            AsyncCallbackFuture<String> future1 = new AsyncCallbackFuture<String>();
            hello.hello("Guillaume", future1);

            long t0 = System.currentTimeMillis();
            try {
                assertEquals("Hello Guillaume!", future1.get(2, TimeUnit.SECONDS));
                fail("Should have thrown an exception");
            } catch (Exception e) {
                // Expected
                long t1 = System.currentTimeMillis();
                assertTrue(t1 - t0 > 50);
                assertTrue(t1 - t0 < 150);
            }

        }
        finally {
            server.stop();
            client.stop();
        }
    }

    public static interface Hello {
        void hello(String name, AsyncCallback<String> callback) throws Exception;
    }

    public static class HelloImpl implements Hello {
        public void hello(final String name, final AsyncCallback<String> callback) throws Exception {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                        // Big introspection call to access the transport channel and close it, simulating
                        // a disconnect on the client side.
                        ((SocketChannel) get(get(get(get(get(callback, "val$helper"), "onComplete"), "this$1"), "val$transport"), "channel")).close();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    callback.onSuccess("Hello " + name + "!");
                }
            }).start();
        }
    }

    private static Object get(Object obj, String field) throws Exception {
        for (Class cl = obj.getClass(); cl != Object.class; cl = cl.getSuperclass()) {
            try {
                Field f = obj.getClass().getDeclaredField(field);
                f.setAccessible(true);
                return f.get(obj);
            } catch (Throwable t) {
                // Ignore
            }
        }
        throw new NoSuchFieldException(field);
    }

}

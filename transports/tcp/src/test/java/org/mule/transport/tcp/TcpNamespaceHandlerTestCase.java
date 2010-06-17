/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.tcp;

import org.mule.ResponseOutputStream;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.tcp.protocols.CustomClassLoadingLengthProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

/**
 * TODO
 */
public class TcpNamespaceHandlerTestCase extends FunctionalTestCase
{
    
    public static class StubSocketPoolFactory implements SocketPoolFactory
    {
        public KeyedObjectPool createSocketPool(TcpConnector connector)
        {
            return new GenericKeyedObjectPool();
        }
    }
    
    protected String getConfigResources()
    {
        return "tcp-namespace-config.xml";
    }

    public void testConfig() throws Exception
    {
        TcpConnector c = lookupTcpConnector("tcpConnector");
        assertNotNull(c);
        assertEquals(1024, c.getReceiveBufferSize());
        assertEquals(2048, c.getSendBufferSize());
        assertEquals(50, c.getReceiveBacklog());
        assertFalse(c.isReuseAddress().booleanValue());
        // this is what we want - i was worried that the client was used as default if the server
        // wasn't set, but that's not the case
        assertEquals(-1, c.getServerSoTimeout());
        assertEquals(3000, c.getClientSoTimeout());
        assertTrue(c.isKeepAlive());
        assertTrue(c.isConnected());
        assertTrue(c.isStarted());
        assertEquals(c.getNextMessageExceptionPolicy().getClass(), DefaultMessageExceptionPolicy.class);
        assertEquals(c.getSocketPoolFactory().getClass(), DefaultSocketPoolFactory.class);
    }
    
    public void testSeparateTimeouts() throws Exception
    {
        TcpConnector c = lookupTcpConnector("separateTimeouts");
        assertNotNull(c);
        assertEquals(4000, c.getServerSoTimeout());
        assertEquals(3000, c.getClientSoTimeout());
        assertTrue(c.isConnected());
        assertTrue(c.isStarted());
        assertEquals(c.getNextMessageExceptionPolicy().getClass(), DefaultMessageExceptionPolicy.class);
        assertEquals(c.getSocketPoolFactory().getClass(), StubSocketPoolFactory.class);
    }
    
    public void testTcpProtocolWithClass()
    {
        TcpConnector connector = lookupTcpConnector("connectorWithProtocolClass");
        assertTrue(connector.getTcpProtocol() instanceof MockTcpProtocol);
    }
    
    public void testTcpProtocolWithRef()
    {
        TcpConnector connector = lookupTcpConnector("connectorWithProtocolRef");
        assertTrue(connector.getTcpProtocol() instanceof MockTcpProtocol);
    }
    
    private TcpConnector lookupTcpConnector(String name)
    {
        TcpConnector connector = (TcpConnector)muleContext.getRegistry().lookupConnector(name);
        assertNotNull(connector);
        return connector;
    }
    
    public static class MockTcpProtocol implements TcpProtocol
    {
        public ResponseOutputStream createResponse(Socket socket) throws IOException
        {
            throw new UnsupportedOperationException("createResponse");
        }

        public Object read(InputStream is) throws IOException
        {
            throw new UnsupportedOperationException("read");
        }

        public void write(OutputStream os, Object data) throws IOException
        {
            throw new UnsupportedOperationException("write");
        }
    }
    
    public void testPollingConnector()
    {
        PollingTcpConnector c = (PollingTcpConnector)muleContext.getRegistry().lookupConnector("pollingConnector");
        assertNotNull(c);
        assertEquals(4000, c.getPollingFrequency());
        assertEquals(3000, c.getClientSoTimeout());
        assertTrue(c.isConnected());
        assertTrue(c.isStarted());
        assertEquals(c.getNextMessageExceptionPolicy().getClass(), DefaultMessageExceptionPolicy.class);
    }
    
    public void testDefaultExceptionPolicy() throws Exception
    {
        TcpConnector c = (TcpConnector)muleContext.getRegistry().lookupConnector("defaultExceptionPolicyConnector");
        assertNotNull(c);
        assertEquals(c.getNextMessageExceptionPolicy().getClass(), DefaultMessageExceptionPolicy.class);
    }

    public void testRewriteExceptionPolicy() throws Exception
    {
        TcpConnector c = (TcpConnector)muleContext.getRegistry().lookupConnector("rewriteExceptionPolicyConnector");
        assertNotNull(c);
        assertEquals(c.getNextMessageExceptionPolicy().getClass(), RewriteMessageExceptionPolicy.class);
    }
    
    public void testCustomClassLoadingProtocol() throws Exception
    {
        TcpConnector c = (TcpConnector)muleContext.getRegistry().lookupConnector("custom-class-loading-protocol-connector");
        assertNotNull(c);
        CustomClassLoadingLengthProtocol protocol = (CustomClassLoadingLengthProtocol) c.getTcpProtocol();
        assertEquals(protocol.getClass(), CustomClassLoadingLengthProtocol.class);
        assertEquals(protocol.getClassLoader(), muleContext.getRegistry().get("classLoader"));
    }
    
    public void testMessageDispatcherFactoryConnector() throws Exception {
        TcpConnector c = (TcpConnector)muleContext.getRegistry().lookupConnector("messageDispatcherFactoryConnector");
        assertNotNull(c);
        assertEquals(LocalSocketTcpMessageDispatcherFactory.class, c.getDispatcherFactory().getClass());
    }
}

package botto.xmpp.cm;

import es.udc.pfc.xmpp.handler.XMLElementDecoder;
import es.udc.pfc.xmpp.handler.XMLFrameDecoder;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Connects to an XMPP server and speaks the Connection Manager Protocol
 * ass described in docs/CM_JEP
 */
public class ConnectionManagerClient {

    private final String domain;
    private final String host;
    private final int port;
    private final String password;

    public ConnectionManagerClient(String domain, String host, int port, String password) {
        this.domain = domain;
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public void start() {


        final XmppConnectionManager manager = new XmppConnectionManager("fuzzo/conn1", domain);

        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("xmlFramer", new XMLFrameDecoder());
                pipeline.addLast("xmlDecoder", new XMLElementDecoder());
                pipeline.addLast("connectionManagerDecoder", new ConnectionManagerDecoder(domain, password, manager.getName()));
                pipeline.addLast("xmppHandler", new ConnectionManagerHandler(manager));
                return pipeline;
            }
        });

        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        future.addListener(new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                System.out.println("Connected handler ");
            }
        });

        // Wait until the connection is closed or the connection attempt fails.
        //future.getChannel().getCloseFuture().awaitUninterruptibly();
        Channel channel = future.getChannel();


        manager.init(channel);
    }

}

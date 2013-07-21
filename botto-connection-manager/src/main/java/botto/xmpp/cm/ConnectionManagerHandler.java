package botto.xmpp.cm;

import es.udc.pfc.xmpp.stanza.IQ;
import es.udc.pfc.xmpp.stanza.Message;
import es.udc.pfc.xmpp.stanza.Presence;
import es.udc.pfc.xmpp.stanza.Stanza;
import es.udc.pfc.xmpp.xml.XMLElement;
import es.udc.pfc.xmpp.xml.XMLElementImpl;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.util.CharsetUtil;

public class ConnectionManagerHandler extends SimpleChannelHandler {

    private final XmppConnectionManager manager;

    public ConnectionManagerHandler(XmppConnectionManager manager) {
        this.manager = manager;

    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

        if (e.getMessage() instanceof Route) {
            manager.receiveRoute((Route) e.getMessage());
        }
        else if (e.getMessage() instanceof  Stanza) {
            final Stanza stanza = (Stanza) e.getMessage();
            if (stanza instanceof Message) {
                manager.receivedMessage((Message) stanza);
            } else if (stanza instanceof Presence) {
                manager.receivedPresence((Presence) stanza);
            } else if (stanza instanceof IQ) {
                manager.receivedIQ((IQ) stanza);
            }
        }
        else {
            ctx.sendUpstream(e);
        }

        if (!(e.getMessage() instanceof Stanza)) {
            ctx.sendUpstream(e);
            return;
        }
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof Stanza) {
            Channels.write(ctx, e.getFuture(), ChannelBuffers.copiedBuffer(e.getMessage().toString(), CharsetUtil.UTF_8));
            return;
        }
        else if (e.getMessage() instanceof XMLElementImpl) {
            Channels.write(ctx, e.getFuture(), ChannelBuffers.copiedBuffer(e.getMessage().toString(), CharsetUtil.UTF_8));
            return;
        }
        else if (e.getMessage() instanceof String) {
            System.out.println(e.getMessage().toString());
            Channels.write(ctx, e.getFuture(), ChannelBuffers.copiedBuffer(e.getMessage().toString(), CharsetUtil.UTF_8));
            return;
        }

        ctx.sendDownstream(e);
    }

    @Override
    public void disconnectRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        manager.willDisconnect();
        ctx.sendDownstream(e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        manager.disconnected();
        ctx.sendUpstream(e);
    }

    public void loggedIn() {
        manager.connected();
    }
}

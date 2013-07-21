package botto.xmpp.cm;

import com.google.common.hash.Hashing;
import es.udc.pfc.xmpp.handler.XMPPStreamHandler;
import es.udc.pfc.xmpp.stanza.Stanza;
import es.udc.pfc.xmpp.stanza.XMPPNamespaces;
import es.udc.pfc.xmpp.xml.XMLElement;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import java.io.StringWriter;



import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectionManagerDecoder  extends SimpleChannelHandler {

    private final Logger Log = LoggerFactory.getLogger(ConnectionManagerDecoder.class);

    public static final java.lang.String CONNECTION_MANAGER = "jabber:connectionmanager";

    private static final QName STREAM_NAME = new QName(XMPPNamespaces.STREAM, "stream", "stream");

    private final String serverName;
    private final String secret;
    private Status status;
    private String streamID;

    private final String managerName;

    private static enum Status {
        CONNECT, AUTHENTICATE, READY, DISCONNECTED;
    }

    public ConnectionManagerDecoder(String serverName, String secret, String managerName) throws XMLStreamException {
        super();
        this.managerName = managerName;

        this.serverName = checkNotNull(serverName);
        this.secret = checkNotNull(secret);

        status = Status.CONNECT;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        String open = "<stream:stream xmlns:stream=\"http://etherx.jabber.org/streams\" xmlns=\"jabber:connectionmanager\" to=\"" + managerName +"\" version=\"1.0\">";

        Channels.write(ctx.getChannel(), ChannelBuffers.copiedBuffer(open, CharsetUtil.UTF_8));

        Log.info("Sent {}", open);
        //ctx.sendUpstream(e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof XMLEvent) {

            final XMLEvent event = (XMLEvent) e.getMessage();

            StringWriter stringWriter = new StringWriter();
            event.writeAsEncodedUnicode(stringWriter);
            Log.info("received {}", stringWriter.toString());

            switch (status) {
                case CONNECT:
                    if (event.isStartElement()) {
                        final StartElement element = event.asStartElement();

                        boolean sName = STREAM_NAME.equals(element.getName());
                        boolean sNamespace = CONNECTION_MANAGER.equals(element.getNamespaceURI(null));
                        String ns = element.getNamespaceURI(null);

                        if (STREAM_NAME.equals(element.getName()) && CONNECTION_MANAGER.equals(element.getNamespaceURI(null))) {
                            if (!managerName.equals(element.getAttributeByName(new QName("from")).getValue())) {
                                throw new Exception("server name mismatch");
                            }
                            streamID = element.getAttributeByName(new QName("id")).getValue();

                            status = Status.AUTHENTICATE;

                            String handshake = "<handshake>" + Hashing.sha1().hashString(streamID + secret, CharsetUtil.UTF_8).toString() + "</handshake>";

                            Channels.write(ctx.getChannel(), ChannelBuffers.copiedBuffer(handshake, CharsetUtil.UTF_8));

                            Log.info("Sent {}", handshake);
                        }
                    } else {
                        throw new Exception("Expected stream:stream element");
                    }
                    break;
                case AUTHENTICATE:
                case READY:
                    if (event.isEndElement()) {
                        final EndElement element = event.asEndElement();

                        if (STREAM_NAME.equals(element.getName())) {
                            Channels.disconnect(ctx.getChannel());
                            return;
                        }
                    }
                    break;
                case DISCONNECTED:
                    throw new Exception("received DISCONNECTED");
            }
        }
        else if (e.getMessage() instanceof XMLElement) {
            final XMLElement element = (XMLElement) e.getMessage();

            //StringWriter stringWriter = new StringWriter();
            //element.writeAsEncodedUnicode(stringWriter);
            Log.info("received {}", element.toString());

            switch (status) {
                case AUTHENTICATE:

                    //final Stanza stanza = Stanza.fromElement(element);
                    //if (stanza == null)
                    //    throw new Exception("Unknown stanza");

                    if("handshake".equals(element.getTagName())) {
                        status = Status.READY;
                        System.out.println("logged in");
                        ctx.getPipeline().get(ConnectionManagerHandler.class).loggedIn();
                    }

                    //if (!"handshake".equals(element.getTagName()))
                    //    throw new Exception("expected handshake");


                    break;
                case READY:

                    if (element.getTagName().equals("route")) {
                        Channels.fireMessageReceived(ctx, new Route(element));
                    }
                    else {
                        final Stanza stanza = Stanza.fromElement(element);
                        if (stanza == null)
                            throw new Exception("Unknown stanza");

                        Channels.fireMessageReceived(ctx, stanza);
                    }
                    break;
                default:
                    throw new Exception("unexpected handleElement");
            }
        }
        else {
            ctx.sendUpstream(e);
        }
    }

    @Override
    public void disconnectRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        Channels.write(ctx, e.getFuture(), ChannelBuffers.copiedBuffer("</stream:stream>", CharsetUtil.UTF_8));
    }

}

package botto.xmpp.cm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import es.udc.pfc.xmpp.stanza.*;
import es.udc.pfc.xmpp.xml.XMLBuilder;
import es.udc.pfc.xmpp.xml.XMLElement;
import es.udc.pfc.xmpp.xml.XMLUtil;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.UUID;

public class XmppConnectionManager {

    private final Logger Log = LoggerFactory.getLogger(XmppConnectionManager.class);

    private final Map<String, SettableFuture<IQ>> futureHandlers = Maps.newHashMap();

    private Channel channel;
    private String name;
    private final String domain;

    public XmppConnectionManager(String name, String domain) {
        this.name = name;
        this.domain = domain;
    }

    // initialize
    void init(Channel channel) {
        this.channel = channel;
    }

    public String getName() {
        return name;
    }

    private int count = 1000;

    final String sessionId = "streamID";

    void connected() {
        Log.info("Connected.");



        // request new session for client
        IQ iq = new IQ(IQ.Type.set);
        iq.setId(Integer.toString(count++));

        XMLElement session = iq.addExtension("session", "http://jabber.org/protocol/connectionmanager");
        session.setAttribute("id", sessionId);
        session.addChild("create");

        Log.info("Sending session create request: {}", iq);

        //send("<route from=\"fuzzo/conn1\" streamid=\"streamID\" to=\"caprazzi.net\"><stream:stream xmlns:stream=\"http://etherx.jabber.org/streams\" version=\"1.0\" xmlns=\"jabber:client\" to=\"caprazzi.net\" xml:lang=\"en\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"></route>");



        Futures.addCallback(sendIQ(iq), new FutureCallback<IQ>() {
            @Override
            public void onSuccess(IQ iq) {
                if (iq.getType() == IQ.Type.result) {
                    // request plain auth
                    XMLElement route = XMLBuilder.create("route")
                        .attribute("id", Integer.toString(count++))
                        .attribute("from", name)
                        .attribute("streamid", sessionId)
                        .attribute("to", domain).getXML();
                    route.addChild(XMLBuilder.create("auth", "urn:ietf:params:xml:ns:xmpp-sasl").attribute("mechanism", "PLAIN").getXML());

                    Log.info("Sending auth method {}", route);

                    send(route);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    public void receiveRoute(Route routed) {
        Log.info("received routed stanza {}", routed);
        XMLElement routedX = routed.getXML();
        String bindIqId = "bind-iq";

        {
            XMLElement stanza = routed.getExtension("challenge", "urn:ietf:params:xml:ns:xmpp-sasl");
            if (stanza != null && stanza.getTagName().equals("challenge")) {
                Log.info("Routed stanza is {}", stanza);
                XMLElement route = route();
                String challenge = new String(Base64.encode("\0bot\0bot".getBytes()));
                route.addChild(XMLBuilder.create("response", "urn:ietf:params:xml:ns:xmpp-sasl").text(challenge));
                send(route);
                return;
            }

        }
        {
            XMLElement stanza = routed.getExtension("success", "urn:ietf:params:xml:ns:xmpp-sasl");
            if (stanza != null && stanza.getTagName().equals("success")) {

                //send("<route from=\"fuzzo/conn1\" streamid=\"streamID\" to=\"caprazzi.net\"><?xml version='1.0' encoding='UTF-8'?><stream:stream xmlns:stream=\"http://etherx.jabber.org/streams\" version=\"1.0\" xmlns=\"jabber:client\" to=\"caprazzi.net\" xml:lang=\"en\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"></route>");



                Log.info("Routed stanza is {}", stanza);
                IQ bindIq = new IQ(IQ.Type.set);
                //bindIqId = "" + (count++);
                bindIq.setId(bindIqId);
                XMLElement bind = bindIq.addExtension("bind", "urn:ietf:params:xml:ns:xmpp-bind");
                XMLElement resource = bind.addChild("resource");
                resource.setText("bot");

                XMLElement route = route();
                route.addChild(bindIq);

                send(route);

                return;
            }
        }

        String sessionIq = "" + (count++);
        {
            XMLElement stanza = routedX.getFirstChild("iq");
            if (stanza != null && stanza.getTagName().equals("iq")) {
                Log.info("BindIdIq: {}, stanza.id: {}", bindIqId, stanza.getAttribute("id"));
                Log.info("Routed stanza is {}. " + stanza.getAttribute("id").equals(bindIqId) + " :: " + stanza.getAttribute("type").equals("result"), stanza);

                XMLElement bindResponse = stanza.getFirstChild("bind");

                if (bindResponse != null) {

                }
                if (stanza.getAttribute("id").equals(bindIqId) && stanza.getAttribute("type").equals("result")) {
                    Log.info("Bind successful");


                    {
                        XMLElement route = route();
                        route.addChild(new Presence());
                        send(route);
                    }


                    XMLElement route = route();

                    Message message = new Message();
                    message.setTo(JID.jid("matteo@caprazzi.net"));
                    message.setBody("hello teo");
                    route.addChild(message);
                    //route.addChild(new Presence());
                    send(route);


                      /*
                    IQ bindIq = new IQ(IQ.Type.set);

                    bindIq.setId(sessionIq);
                    bindIq.addExtension("session", "urn:ietf:params:xml:ns:xmpp-session");

                    XMLElement route = route();
                    route.addChild(bindIq);

                    send(route);
                    */

                }

            }
        }
    }



    private XMLElement route() {
        return XMLBuilder.create("route")
            //.attribute("id", Integer.toString(count++))
            .attribute("from", name)
            .attribute("streamid", sessionId)
            .attribute("to", domain).getXML();
    }

    void disconnected() {
        Log.info("Disconnected.");
    }

    public void receivedMessage(Message stanza) {

    }

    public void receivedPresence(Presence stanza) {

    }

    protected ListenableFuture<IQ> handleIQ(IQ iq) {
        Log.info("Handing IQ {}", iq);
        final SettableFuture<IQ> future = SettableFuture.create();
        return future;
    }

    public void receivedIQ(IQ iq) {
        checkNotNull(iq);
        Log.debug("Received iq: " + iq.toString());
        if (iq.isRequest()) {
            Futures.addCallback(handleIQ(iq), new FutureCallback<IQ>() {

                @Override
                public void onSuccess(IQ result) {
                    send(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    // TODO: send an error
                }
            });
        }
        else if (iq.isResponse()) {
            final SettableFuture<IQ> future = futureHandlers.remove(iq.getId());
            if (future == null) {
                Log.warn("No handler for ID " + iq.getId());
                return;
            }

            if (iq.getType() == IQ.Type.result) {
                future.set(iq);
            }
            else if (iq.getType() == IQ.Type.error) {
                future.setException(new Exception("Error IQ: " + iq.toString()));
            }
        }
        else {
            Log.warn("IQ not request or response");
        }
    }

    public ListenableFuture<IQ> sendIQ(final IQ iq) {
        checkNotNull(iq);
        checkArgument(iq.isRequest() && !Strings.isNullOrEmpty(iq.getId()));

        if (futureHandlers.containsKey(iq.getId())) {
            Log.warn("ID " + iq.getId() + " already being handled.");
            return futureHandlers.get(iq.getId());
        }

        final SettableFuture<IQ> future = SettableFuture.create();
        futureHandlers.put(iq.getId(), future);
        send(iq);
        return future;
    }

    /**
     * Send a Stanza to the server.
     *
     * @param stanza the Stanza to be sent
     */

    private void send(String s) {
        Log.debug("Sending stanza: " + s);
        Channels.write(channel, s);
    }

    public void send(XMLElement stanza) {
        checkNotNull(stanza);
        if (channel == null || !channel.isConnected()) {
            Log.warn("Disconnected, can't send stanza: " + stanza.toString());
            return;
        }

        Log.debug("Sending stanza: " + stanza.toString());
        Channels.write(channel, stanza);
    }

    public final void send(final Stanza stanza) {
        checkNotNull(stanza);
        if (channel == null || !channel.isConnected()) {
            Log.warn("Disconnected, can't send stanza: " + stanza.toString());
            return;
        }

        Log.debug("Sending stanza: " + stanza.toString());
        Channels.write(channel, stanza);
    }

    public void willDisconnect() {


    }


}

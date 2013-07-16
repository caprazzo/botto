package botto.xmpp.utils;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import java.io.StringReader;

public class PacketTypeConverter {
    // this method converts from smack (xmpp bot library) packet types
    // to tinder/whack packet types (xmpp component library)
    public static Packet converttoTinder(org.jivesoftware.smack.packet.Packet packet) {
        SAXReader saxReader = new SAXReader();
        try {
            Document read = saxReader.read(packet.toXML());
            Element element = read.getRootElement();
            if (element.getName().equalsIgnoreCase("iq")) {
                return new IQ(element);
            }
            else if (element.getName().equalsIgnoreCase("presence")) {
                return new Presence(element);
            }
            else if(element.getName().equalsIgnoreCase("message")) {
                return new Message(element);
            }
            else {
                throw new RuntimeException("Unknown packet type " + element.getName() + ": " + element.toString());
            }
        } catch (DocumentException e) {
            throw new RuntimeException("exception while parsing packet " + packet.toXML(), e);
        }
    }

    public static org.jivesoftware.smack.packet.Packet convertFromTinder(Packet packet, Connection connection) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(packet.toXML()));

            if (packet.getElement().getName().equalsIgnoreCase("iq")) {
                return PacketParserUtils.parseIQ(xpp, connection);
            }
            else if (packet.getElement().getName().equalsIgnoreCase("presence")) {
                return PacketParserUtils.parsePresence(xpp);
            }
            else if(packet.getElement().getName().equalsIgnoreCase("message")) {
                return PacketParserUtils.parseMessage(xpp);
            }
            else {
                throw new RuntimeException("Unknown packet type " + packet.getElement().getName() + ": " + packet.toXML());
            }
        }
        catch (Exception e) {
            throw new RuntimeException("exception while parsing packet " + packet.toXML(), e);
        }
    }
}

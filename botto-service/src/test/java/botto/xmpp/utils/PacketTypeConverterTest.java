package botto.xmpp.utils;


import botto.xmpp.connectors.smack.PacketTypeConverter;
import org.junit.Assert;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class PacketTypeConverterTest {

    @Test
    public void testTinderPresence() {
        org.xmpp.packet.Presence tinder = new org.xmpp.packet.Presence();
        tinder.setStatus("available");
        tinder.setTo(new JID("someone@somewhre.com"));
        tinder.setFrom(new JID("someoneelse@somewhre.com"));
        tinder.setID("id");
        tinder.setShow(Presence.Show.chat);
        tinder.setType(Presence.Type.unavailable);

        XMPPConnection connection = new XMPPConnection("foo");

        Packet smack = PacketTypeConverter.convertFromTinder(tinder, connection);

        org.xmpp.packet.Presence reTinder = (org.xmpp.packet.Presence) PacketTypeConverter.converttoTinder(smack);

        Assert.assertEquals(tinder.getFrom(), reTinder.getFrom());
        Assert.assertEquals(tinder.getTo(), reTinder.getTo());
        Assert.assertEquals(tinder.getID(), reTinder.getID());
        Assert.assertEquals(tinder.getShow(), reTinder.getShow());
        Assert.assertEquals(tinder.getType(), reTinder.getType());
    }

}

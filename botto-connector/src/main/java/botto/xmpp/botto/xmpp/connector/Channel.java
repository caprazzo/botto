package botto.xmpp.botto.xmpp.connector;

import org.xmpp.packet.JID;

// TODO: make usable as key
// TODO: add open/close status
// TODO: add connection status
public class Channel {
    private JID address;

    private Channel(JID address) {
        this.address = address;
    }

    public JID getAddress() {
        return address;
    }

    public static Channel from(JID address) {
        return new Channel(address);
    }
}

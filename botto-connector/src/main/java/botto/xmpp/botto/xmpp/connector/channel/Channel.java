package botto.xmpp.botto.xmpp.connector.channel;

import com.google.common.base.Objects;
import org.xmpp.packet.JID;

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

    @Override
    public String toString() {
        return "Channel(" + address + ")";
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Channel other = (Channel) obj;
        return Objects.equal(this.address, other.address);
    }
}

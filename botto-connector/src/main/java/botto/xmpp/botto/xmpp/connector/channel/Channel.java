package botto.xmpp.botto.xmpp.connector.channel;

import botto.xmpp.botto.xmpp.connector.ConnectorId;
import com.google.common.base.Objects;
import org.xmpp.packet.JID;

public class Channel {
    private final ConnectorId connectorId;
    private final JID address;
    private final int hashCode;

    public static Channel from(ConnectorId connectorId, JID address) {
        return new Channel(connectorId, address);
    }

    private Channel(ConnectorId connectorId, JID address) {
        this.connectorId = connectorId;
        this.address = address;
        this.hashCode = Objects.hashCode(connectorId, address);
    }

    public JID getAddress() {
        return address;
    }

    public ConnectorId getConnectorId() {
        return connectorId;
    }

    @Override
    public String toString() {
        return "Channel(" + connectorId + ", " + address + ")";
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Channel other = (Channel) obj;
        return Objects.equal(this.address, other.address)
            && Objects.equal(this.connectorId, other.connectorId);
    }


}

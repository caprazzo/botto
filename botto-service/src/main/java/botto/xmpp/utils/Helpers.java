package botto.xmpp.utils;

import com.google.common.base.Objects;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

public class Helpers {

    public static String toString(Packet packet) {

        if (packet instanceof Message) {
            return toString((Message)packet);
        }
        if (packet instanceof IQ) {
            return toString((IQ)packet);
        }
        if (packet instanceof Presence) {
            return toString((Presence)packet);
        }

        return Objects.toStringHelper(packet.getClass())
            .add("from", packet.getFrom())
            .add("to", packet.getTo())
             .add("id", packet.getID())
            .toString();
    }

    public static String toString(Message packet) {
        return Objects.toStringHelper(packet.getClass())
            .add("from", packet.getFrom())
            .add("to", packet.getTo())
            .add("body", packet.getBody())
            .add("id", packet.getID())
            .toString();
    }

    public static String toString(IQ packet) {
        return Objects.toStringHelper(packet.getClass())
            .add("from", packet.getFrom())
            .add("to", packet.getTo())
            .add("type", packet.getType())
            .add("id", packet.getID())
            .toString();
    }

    public static String toString(Presence packet) {
        return Objects.toStringHelper(packet.getClass())
            .add("from", packet.getFrom())
            .add("to", packet.getTo())
            .add("type", packet.getType())
            .add("id", packet.getID())
            .toString();
    }

}

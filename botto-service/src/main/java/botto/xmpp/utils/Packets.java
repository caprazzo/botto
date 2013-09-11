package botto.xmpp.utils;

import botto.xmpp.botto.xmpp.connector.channel.Channel;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.xmpp.packet.*;

public class Packets {

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

    public static boolean equalBareJid(JID first, JID second) {
        Preconditions.checkNotNull(first);
        return second != null && first.toBareJID().equals(second.toBareJID());
    }

    public static boolean requireId(Packet packet) {
        return (packet instanceof Message || packet instanceof IQ);
    }

    public static boolean hasId(Packet packet) {
        return !Strings.isNullOrEmpty(packet.getID());
    }

    public static String createId() {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public static Packet preparePacketForSending(Channel channel, Packet packet) {
        boolean fixId = Packets.requireId(packet) && !Packets.hasId(packet);
        boolean fixSender = !Packets.equalBareJid(channel.getAddress(), packet.getFrom());

        if (!fixId && !fixSender) {
            return packet;
        }

        Packet copy = packet.createCopy();

        if (fixId) {
            copy.setID(Packets.createId());
        }

        if (fixSender) {
            copy.setFrom(channel.getAddress());
        }

        return copy;
    }
}

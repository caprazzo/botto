package botto.xmpp.reflection;

import botto.xmpp.annotations.BotContext;
import botto.xmpp.annotations.Context;
import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.annotations.Receive;
import com.google.common.base.Optional;

import org.junit.experimental.theories.*;
import org.junit.runner.RunWith;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Theories.class)
public class AnnotatedBotObjectTest {

    private static PotentialAssignment objectAssignment(Object obejct) {
        return PotentialAssignment.forValue(obejct.getClass().getName(), obejct);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @ParametersSuppliedBy(InvalidBotsSupplier.class)
    public @interface InvalidBots { }
    public static class InvalidBotsSupplier extends ParameterSupplier {
        @Override
        public List getValueSources(ParameterSignature parameterSignature) {
            return Arrays.asList(new Object[] {
                objectAssignment(new EmptyBot()),
                objectAssignment(new WrongAnnotationReceiveBot()),
                objectAssignment(new ForgotAnnotationReceiveBot())
            });
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @ParametersSuppliedBy(ValidBotsSupplier.class)
    public @interface ValidBots { }
    public static class ValidBotsSupplier extends ParameterSupplier {
        @Override
        public List getValueSources(ParameterSignature parameterSignature) {
            return Arrays.asList(new Object[] {
                objectAssignment(new ReceivePacketBot()),
                objectAssignment(new ReceiveMessageBot()),
                objectAssignment(new ReceivePresenceBot()),
                objectAssignment(new ReceiveIQBot()),
                objectAssignment(new PacketRespondPacketBot()),
                objectAssignment(new PacketRespondMessageBot()),
                objectAssignment(new MessageRespondPacketBot()),
                objectAssignment(new MessageRespondMessageBot()),
                objectAssignment(new PresenceRespondPresenceBot()),
                objectAssignment(new IQRespondIQBot()),
                objectAssignment(new TaggedOutputBot()),
                objectAssignment(new TaggedBotContextBot())
            });
        }
    }


    @Theory
    public void should_not_create_from_invalid_bots(@InvalidBots Object source) {
        Optional<AnnotatedBotObject> bot = AnnotatedBotObject.from(source);
        assertFalse(bot.isPresent());
    }

    @Theory
    public void should_create_from_valid_bots(@ValidBots Object source) {
        Optional<AnnotatedBotObject> bot = AnnotatedBotObject.from(source);
        assertTrue(bot.isPresent());
    }

    // just an empty object
    private static class EmptyBot { }


    // annotation ok, but wrong signature
    private static class WrongAnnotationReceiveBot {
        @Receive
        public void receive(Object object) {

        }
    }

    // ok signature, but forgot annotation
    private static class ForgotAnnotationReceiveBot {
        public void receive(Message message) {

        }
    }

    // receive a packet
    private static class ReceivePacketBot {
        @Receive
        public void onPacket(Packet message) {

        }
    }

    // receive a message
    private static class ReceiveMessageBot {
        @Receive
        public void onMessage(Message message) {

        }
    }

    // receive presence
    private static class ReceivePresenceBot {
        @Receive
        public void onPresence(Presence message) {

        }
    }

    // receive IQ
    private static class ReceiveIQBot {
        @Receive
        public void onPresence(IQ message) {

        }
    }

    // responds with a packet when receives a packet
    private static class PacketRespondPacketBot {
        @Receive
        public Packet onPacket(Packet packet) {
            return null;
        }
    }

    // responds with a packet when receives a packet
    private static class PacketRespondMessageBot {
        @Receive
        public Message onPacket(Packet packet) {
            return null;
        }
    }

    // responds with a message when receives a message
    private static class MessageRespondPacketBot {
        @Receive
        public Packet onMessage(Message packet) {
            return null;
        }
    }

    // responds with a message when receives a message
    private static class MessageRespondMessageBot {
        @Receive
        public Packet onMessage(Packet packet) {
            return null;
        }
    }

    // responds with a message when receives a message
    private static class PresenceRespondPresenceBot {
        @Receive
        public Packet onPresence(Packet packet) {
            return null;
        }
    }

    // responds with an IQ when receives an IQ
    private static class IQRespondIQBot {
        @Receive
        public IQ onIQ(IQ packet) {
            return null;
        }
    }

    // bot with a packetOutput
    private static class TaggedOutputBot {
        @Context
        private PacketOutput out;
    }

    // bot with a context
    private static class TaggedBotContextBot {
        @Context
        private BotContext context;
    }

    // bot with both context annotations
    private static class TaggedMultiBot {
        @Context
        private PacketOutput out;

        @Context
        private BotContext context;
    }

}

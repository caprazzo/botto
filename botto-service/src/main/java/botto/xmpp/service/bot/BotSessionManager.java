package botto.xmpp.service.bot;


import botto.xmpp.service.AbstractBot;
import botto.xmpp.service.BotEnvironment;

import com.sun.tools.jdi.ObsoleteMethodImpl;
import net.caprazzi.reusables.common.Managed;

import java.util.HashMap;

public class BotSessionManager implements Managed {

    private final String host;
    private final int port;

    private final HashMap<String, BotSession> sessions = new HashMap<String, BotSession>();

    private final BotSessionPacketSender sender = new BotSessionPacketSender();

    public BotSessionManager(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Deprecated
    public void createSession(final AbstractBot bot, final String node, String secret, String resource) {
        sessions.put(node, new BotSession(host, port, node, secret, resource, bot, sender));
    }

    public PacketInputOutput createSession(BotEnvironment env) {
        final BotSession session = new BotSession(host, port, env.getNode(), env.getSecret(), env.getResource(), env.getBot(), sender);
        sessions.put(env.getNode(), session);
        return session;
    }

    public void destroySession(BotEnvironment env) {
        sessions.remove(env.getNode()).stop();
    }

    public void start() {
        for(BotSession session : sessions.values()) {
            session.start();
        }

        sender.start();
    }

    public void stop() {
        sender.stop();
        for(BotSession session : sessions.values()) {
            session.stop();
        }
        sessions.clear();
    }


}

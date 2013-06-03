package botto.xmpp.service.component;

import com.google.common.collect.HashBasedTable;
import botto.xmpp.service.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

import java.util.Map;

/**
 * The ComponentBotRouter finds subdomain bots for incoming packets and executes
 * the bots using a @ComponentBotExecutor
 */
public class ComponentBotRouter {

    private final Logger Log = LoggerFactory.getLogger(ComponentBotRouter.class);

    private final ComponentBotExecutor executor;
    private final HashBasedTable<Bot, String, NodeFilter> table = HashBasedTable.create();

    public ComponentBotRouter(ComponentBotExecutor executor) {
        this.executor = executor;
    }

    public synchronized void route(String subdomain, Packet packet) {
        Log.debug("Request to route to subdomain {} for packet {}", subdomain, packet.toXML());
        for(Map.Entry<Bot, NodeFilter> entry : table.column(subdomain).entrySet()) {
            if (entry.getValue().accept(packet.getTo().getNode())) {
                Log.debug("Routing packet {} to processor {} ", packet.toXML(), entry.getKey());
                executor.execute(entry.getKey(), packet.createCopy());
            }
        }
    }

    public synchronized void addBot(Bot bot, String subdomain, NodeFilter nodeFilter) {
        table.put(bot, subdomain, nodeFilter);
    }
}

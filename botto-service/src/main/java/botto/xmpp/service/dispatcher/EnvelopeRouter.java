package botto.xmpp.service.dispatcher;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Map;

class EnvelopeRouter<TLabel, TRoute> {

    private final BiMap<PacketEnvelopeFilter<TLabel>, TRoute> table = HashBiMap.create();

    public Optional<TRoute> route(PacketEnvelope<TLabel> envelope) {
        for(Map.Entry<PacketEnvelopeFilter<TLabel>, TRoute> pair : table.entrySet()) {
            if (pair.getKey().accept(envelope)) {
                return Optional.of(pair.getValue());
            }
        }
        return Optional.absent();
    }

    public void addRoute(PacketEnvelopeFilter<TLabel> filter, TRoute route) {
        table.put(filter, route);
    }

    public void removeRoute(Bot bot) {
        table.inverse().remove(bot);
    }
}

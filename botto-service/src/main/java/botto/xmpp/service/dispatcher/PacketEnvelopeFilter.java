package botto.xmpp.service.dispatcher;

public interface PacketEnvelopeFilter<TSource> {
   public boolean accept(PacketEnvelope<TSource> envelope);
}

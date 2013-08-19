package botto.xmpp.service.bot;

import botto.xmpp.annotations.PacketOutput;
import botto.xmpp.service.dispatcher.PacketSource;

public interface PacketInputOutput {
    public PacketOutput getOutput();
    public PacketSource getSource();
}

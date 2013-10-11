import botto.xmpp.annotations.BotContext;
import botto.xmpp.annotations.Context;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
 * A bot that can say hello to an arbitrary XMPP user
 */
public class SayHelloBot {

    @Context
    private BotContext botContext;

    public boolean isConnected() {
        return botContext.isConnected();
    }

    /**
     * Sends an XMPP message that says 'hello'
     *
     * @param dest a valid user JID
     */
    public void sayHello(String dest) {
        Message message = new Message();
        message.setTo(new JID(dest));
        message.setBody("Hello, " + dest);
        message.setFrom("hello");
        botContext.send(message);
    }
}

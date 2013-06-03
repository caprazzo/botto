import botto.xmpp.annotations.Context;
import botto.xmpp.annotations.PacketOutput;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
 * A bot that can say hello to an arbitrary XMPP user
 */
public class SayHelloBot {

    @Context
    private PacketOutput output;

    /**
     * Sends an XMPP message that says 'hello'
     * @param dest a valid user JID
     */
    public void sayHello(String dest) {
        Message message = new Message();
        message.setTo(new JID(dest));
        message.setBody("Hello, " + dest);
        message.setFrom("hello");
        output.send(message);
    }
}

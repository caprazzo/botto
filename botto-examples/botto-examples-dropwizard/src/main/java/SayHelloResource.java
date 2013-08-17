import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * A simple resource that sends a greeting to
 * an XMPP user via the SayHelloBot
 */
@Path("/user")
public class SayHelloResource {

    private final SayHelloBot helloBot;

    public SayHelloResource(SayHelloBot helloBot) {
        this.helloBot = helloBot;
    }

    @GET
    @Path("/check")
    public boolean isConnected() {
        return helloBot.isConnected();
    }

    @POST
    @Path("/hello/{jid}/")
    public void sayHello(@PathParam("jid") String toJid) {
        helloBot.sayHello(toJid);
    }
}

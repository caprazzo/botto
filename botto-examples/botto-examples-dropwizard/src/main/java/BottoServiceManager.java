import botto.xmpp.service.AbstractBotService;
import botto.xmpp.service.BotServiceConfiguration;
import botto.xmpp.service.ServiceEnvironment;
import com.yammer.dropwizard.lifecycle.Managed;

/**
 * Created with IntelliJ IDEA.
 * User: caprazzo
 * Date: 03/06/2013
 * Time: 09:28
 * To change this template use File | Settings | File Templates.
 */
public class BottoServiceManager extends AbstractBotService implements Managed {

    private final BotServiceConfiguration configuration;

    public BottoServiceManager(BotServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void run(ServiceEnvironment environment) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void start() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class BottoDropwizardServiceConfiguration extends Configuration {

    @NotEmpty
    @JsonProperty
    private String xmppServer;

    @JsonProperty
    @NotNull
    private Integer xmppServerPort;

    @JsonProperty
    @NotNull
    private String xmppServerComponentSecret;

    public String getXmppServer() {
        return xmppServer;
    }

    public int getXmppServerPort() {
        return xmppServerPort;
    }

    public String getXmppServerComponentSecret() {
        return xmppServerComponentSecret;
    }

}

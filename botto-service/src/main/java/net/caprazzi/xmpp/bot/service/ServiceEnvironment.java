package net.caprazzi.xmpp.bot.service;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;

public class ServiceEnvironment {

    private final Logger Log = LoggerFactory.getLogger(ServiceEnvironment.class);

    private final HashMap<String, SubdomainEnvironment> subdomains = new HashMap<String, SubdomainEnvironment>();
    private final ServiceConfiguration configuration;

    ServiceEnvironment(ServiceConfiguration configuration) {
        Preconditions.checkNotNull(configuration, "Configuration can't be null.");
        this.configuration = configuration;
    }

    public SubdomainEnvironment getSubdomain(String subdomain) {
        SubdomainEnvironment env = subdomains.get(subdomain);
        if (env == null) {
            env = new SubdomainEnvironment(this, subdomain);
            env.setSecret(configuration.getSecret(subdomain));
            subdomains.put(subdomain, env);
        }
        return env;
    }

    Collection<SubdomainEnvironment> getSubdomains() {
        return subdomains.values();
    }

    public boolean isBotInOtherSubdomains(SubdomainEnvironment subdomain, Object bot) {
        for(SubdomainEnvironment sub : subdomains.values()) {
            if (sub == subdomain)
                continue;

            if (sub.hasBot(bot))
                return true;
        }
        return false;
    }
}

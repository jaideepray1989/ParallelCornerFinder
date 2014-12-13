package rest;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class CornerFinderService extends Service<RestServiceConfiguration> {
    public static void main(String[] args) throws Exception {
        new CornerFinderService().run(args);
    }

    @Override
    public void initialize(Bootstrap<RestServiceConfiguration> bootstrap) {
        bootstrap.setName("corner-finder-service");
    }

    @Override
    public void run(RestServiceConfiguration restServiceConfiguration, Environment environment) throws Exception {
        environment.addResource(new CornerFinderResource());
    }
}
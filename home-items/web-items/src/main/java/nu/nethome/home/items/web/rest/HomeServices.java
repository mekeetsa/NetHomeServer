package nu.nethome.home.items.web.rest;


import nu.nethome.home.items.web.rest.exceptions.ExecutionFailureMapper;
import nu.nethome.home.items.web.rest.exceptions.RestExceptionMapper;
import nu.nethome.home.system.HomeService;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class HomeServices extends Application {
    private static Set services = new HashSet();
    private static HomeService server;

    public  HomeServices() {
        services.add(new HomeItemsResource(server));
        services.add(new RestExceptionMapper());
        services.add(new ExecutionFailureMapper());
    }

    @Override
    public  Set getSingletons() {
        return services;
    }

    public  static Set getServices() {
        return services;
    }

    public static void setServer(HomeService server) {
        HomeServices.server = server;
    }
}

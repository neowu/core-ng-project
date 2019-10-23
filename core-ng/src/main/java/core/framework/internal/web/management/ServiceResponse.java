package core.framework.internal.web.management;

import core.framework.api.json.Property;

import java.util.List;

/**
 * @author neo
 */
public class ServiceResponse {
    @Property(name = "app")
    public String app;

    @Property(name = "services")
    public List<String> services;

    @Property(name = "clients")
    public List<String> clients;

    @Property(name = "producers")
    public List<String> producers;

    @Property(name = "consumers")
    public List<String> consumers;
}

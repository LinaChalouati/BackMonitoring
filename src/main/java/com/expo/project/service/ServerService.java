package com.expo.project.service;

import com.actia.monitoring.server.model.Server;
import com.expo.project.model.Project;

import java.util.Collection;

public interface ServerService {
    Server create (Server server);

    Project create(Project server);

    Server ping(String ipAddr);
    Collection<Server> list();
    Server get(Long id);
    Server update(Server server);

    Project update(Project server);

    Boolean delete(Long id);

}

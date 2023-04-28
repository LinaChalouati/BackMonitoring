package com.expo.project.service;

import com.expo.project.model.Project;
import com.expo.project.model.Status;
import com.expo.project.repo.ServerRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Collection;

import static java.lang.Boolean.TRUE;
import static org.springframework.data.domain.PageRequest.of;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class ServerServiceimpl implements ServerService {
    private final ServerRepo serverRepo;


    @Override
    public Project create(Project server) {
    log.info("Saving new Server :{} ",server.getName());
    return serverRepo.save(server);

    }
    @SneakyThrows
    @Override
    public Project ping(String ipAddr) {
        log.info("Pinging l serveur addresse IP :{}",ipAddr);
        Project project=serverRepo.findByIpAddress(ipAddr);
        InetAddress addr=   InetAddress.getByName(ipAddr);
        project.setStatus(addr.isReachable(100000) ? Status.SERVER_UP : Status.SERVER_DOWN);
        serverRepo.save(project);
        return project;

    }

    @Override
    public Collection<Project> list() {
        log.info("Fetching All SERVERS !");
        return serverRepo.findAll(of(0,30)).toList();
    }

    @Override
    public Project get(Long id) {
        log.info("Fetching l server by ID : {}!",id);
        return serverRepo.findById(id).get();


    }

    @Override
    public Project update(Project server) {
        log.info("Updating the Server :{} ",server.getName());
        return serverRepo.save(server);

    }

    @Override
    public Boolean delete(Long id) {
        log.info("Deleting the Server :{} ",id);
        serverRepo.deleteById(id);
        return TRUE;
    }
}

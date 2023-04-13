package com.grafana.expo.repo;

import com.grafana.expo.model.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
   // Optional findDashboardByTitle();
}

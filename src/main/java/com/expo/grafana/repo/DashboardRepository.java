package com.expo.grafana.repo;

import com.expo.grafana.model.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
   // Optional findDashboardByTitle();
}

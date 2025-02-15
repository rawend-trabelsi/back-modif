package com.rawend.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rawend.demo.entity.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
}

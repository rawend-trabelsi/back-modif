package com.rawend.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rawend.demo.entity.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
	   ServiceEntity findById(long id);
	 
	       @Query("SELECT s FROM ServiceEntity s WHERE s.promotion IS NOT NULL")
	       List<ServiceEntity> findAllWithPromotions();
	   

}

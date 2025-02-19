package com.rawend.demo.Repository;


import com.rawend.demo.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;



public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {
	
    Optional<PromotionEntity> findByCodePromo(String codePromo);
}

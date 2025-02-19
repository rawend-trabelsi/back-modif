package com.rawend.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.List;
@Entity
@Table(name = "promotion")
@Getter
@Setter
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codePromo; 
    private Double valeurReduction;  
    private Boolean actif; 
    private Date dateDebut;  
    private Date dateFin;  

    @Enumerated(EnumType.STRING)
    private TypeReduction typeReduction;

    @ManyToMany
    @JoinTable(
        name = "promotion_service",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<ServiceEntity> services;

    public Double getTauxReduction() {
        return (this.typeReduction == TypeReduction.POURCENTAGE) ? this.valeurReduction : 0.0;
    }
}

package com.rawend.demo.Controller;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rawend.demo.entity.PromotionEntity;
import com.rawend.demo.entity.ServiceEntity;
import com.rawend.demo.entity.TypeReduction;
import com.rawend.demo.services.PromotionService;
import com.rawend.demo.services.ServiceService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ServiceService serviceService;

    @PostMapping
    public PromotionEntity creerPromotion(@RequestBody PromotionEntity promotionEntity) {
        PromotionEntity promotionSauvegardee = promotionService.creerPromotion(promotionEntity);

        for (ServiceEntity service : promotionEntity.getServices()) {
            ServiceEntity serviceRecupere = serviceService.obtenirServiceParId(service.getId());
            if (serviceRecupere != null) {
                serviceRecupere.setPromotion(promotionSauvegardee); 
                serviceService.mettreAJourService(serviceRecupere); 
            }
        }
        return promotionSauvegardee;
    }



    @DeleteMapping("/delete/{promotionId}")
    public String supprimerPromotion(@PathVariable Long promotionId) {
        PromotionEntity promotion = promotionService.obtenirPromotionParId(promotionId);
        if (promotion == null) {
            throw new RuntimeException("La promotion n'existe pas.");
        }

     
        if (promotion.getServices() != null && !promotion.getServices().isEmpty()) {
            for (ServiceEntity service : promotion.getServices()) {
                service.setPromotion(null);  
                serviceService.mettreAJourService(service);  
            }
        }

     
        promotionService.supprimerPromotion(promotionId);
        return "Promotion supprimée avec succès, les services ne sont pas affectés";
    }


    @PostMapping("/apply")
    public ResponseEntity<?> appliquerPromo(
        @RequestParam(required = false) String codePromo, 
        @RequestParam Long serviceId
    ) {
        try {
            PromotionEntity promo = null;

            
            if (codePromo != null && !codePromo.isEmpty()) {
                promo = promotionService.trouverParCode(codePromo);
                
                if (promo == null) {
                    return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Le code promo est invalide."));
                }

                if (!promo.getActif()) {
                    return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "La promotion avec ce code n'est pas active."));
                }
            } else {
                ServiceEntity service = serviceService.obtenirServiceParId(serviceId);
                if (service == null) {
                    return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Le service avec l'ID spécifié n'existe pas."));
                }

                if (service.getPromotion() != null) {
                    promo = service.getPromotion(); 
                } else {
                    return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Aucune promotion valide pour ce service."));
                }
            }

            
            LocalDate today = LocalDate.now();
            if (convertToLocalDate(promo.getDateDebut()).isAfter(today) || 
                convertToLocalDate(promo.getDateFin()).isBefore(today)) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "La promotion n'est pas valide pour la période actuelle."));
            }

            
            ServiceEntity service = serviceService.obtenirServiceParId(serviceId);
            if (!service.getPromotions().contains(promo)) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Ce service n'est pas associé à cette promotion."));
            }

           
            double prixReduit = promotionService.calculerPrixAvecReduction(service, promo);
            return ResponseEntity.ok(Map.of(
                "serviceId", service.getId(),
                "serviceName", service.getTitre(),
                "originalPrice", service.getPrix(),
                "discountedPrice", prixReduit,
                "promotion", promo.getTauxReduction() + "% de réduction"
            ));

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Une erreur est survenue: " + e.getMessage()));
        }
    }

    @GetMapping("/servicesWithPromotions")
    public ResponseEntity<List<Map<String, Object>>> getServicesWithPromotions() {
        List<ServiceEntity> services = serviceService.obtenirTousLesServicesAvecPromotions(); // À implémenter dans ton service
        List<Map<String, Object>> responseList = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (ServiceEntity service : services) {
            PromotionEntity promo = service.getPromotion();
            if (promo != null && 
                !convertToLocalDate(promo.getDateDebut()).isAfter(today) && 
                !convertToLocalDate(promo.getDateFin()).isBefore(today)) {

                Double originalPrice = service.getPrix();
                Double discountedPrice = calculerPrixAvecReduction(service, promo);

                Map<String, Object> response = new HashMap<>();
                response.put("serviceId", service.getId());
                response.put("serviceName", service.getTitre());
                response.put("originalPrice", originalPrice);
                response.put("discountedPrice", discountedPrice);
                response.put("promotion", promo.getTauxReduction() + "% off");

                responseList.add(response);
            }
        }

        return ResponseEntity.ok(responseList);
    }


    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }



   
    private LocalDate convertToLocalDate(Timestamp timestamp) {
        return timestamp.toLocalDateTime().toLocalDate();
    }

    private double calculerPrixAvecReduction(ServiceEntity service, PromotionEntity promo) {
        double prixInitial = service.getPrix();

        switch (promo.getTypeReduction()) {
            case POURCENTAGE:
                return prixInitial * (1 - promo.getValeurReduction() / 100);
            case MONTANT_FIXE:
                return prixInitial - promo.getValeurReduction();
            default:
                return prixInitial;
        }
    }


    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

 
  

    @PutMapping("/update/{promotionId}")
    public PromotionEntity updatePromotion(@PathVariable Long promotionId, @RequestBody PromotionEntity promotionEntity) {
        PromotionEntity existingPromo = promotionService.obtenirPromotionParId(promotionId);
        if (existingPromo == null) {
            throw new RuntimeException("La promotion avec l'ID spécifié n'existe pas.");
        }

        existingPromo.setCodePromo(promotionEntity.getCodePromo());
        existingPromo.setActif(promotionEntity.getActif());
        existingPromo.setDateDebut(promotionEntity.getDateDebut());
        existingPromo.setDateFin(promotionEntity.getDateFin());
        existingPromo.setTypeReduction(promotionEntity.getTypeReduction());
        existingPromo.setValeurReduction(promotionEntity.getValeurReduction());

        if (promotionEntity.getServices() != null && !promotionEntity.getServices().isEmpty()) {
            existingPromo.setServices(promotionEntity.getServices());
            for (ServiceEntity service : promotionEntity.getServices()) {
                ServiceEntity serviceRecupere = serviceService.obtenirServiceParId(service.getId());
                if (serviceRecupere != null) {
                    serviceRecupere.getPromotions().add(existingPromo);
                    serviceService.mettreAJourService(serviceRecupere);
                }
            }
        }

        return promotionService.creerPromotion(existingPromo);
    }
}

package com.rawend.demo.services;

import com.rawend.demo.entity.PromotionEntity;
import com.rawend.demo.entity.ServiceEntity;
import com.rawend.demo.entity.TypeReduction;
import com.rawend.demo.Repository.PromotionRepository;
import com.rawend.demo.services.PromotionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Date;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ServiceService serviceService;

    public PromotionEntity creerPromotion(PromotionEntity promotionEntity) {
        return promotionRepository.save(promotionEntity);
    }

  
    public PromotionEntity mettreAJourPromotion(Long promotionId, PromotionEntity promotionEntity) {
        Optional<PromotionEntity> promotionOptional = promotionRepository.findById(promotionId);
        if (promotionOptional.isPresent()) {
            PromotionEntity promoExistante = promotionOptional.get();
            promoExistante.setCodePromo(promotionEntity.getCodePromo());
            promoExistante.setValeurReduction(promotionEntity.getValeurReduction());
            promoExistante.setActif(promotionEntity.getActif());
            promoExistante.setDateDebut(promotionEntity.getDateDebut());
            promoExistante.setDateFin(promotionEntity.getDateFin());
            promoExistante.setTypeReduction(promotionEntity.getTypeReduction());
            return promotionRepository.save(promoExistante);
        } else {
            throw new PromotionNotFoundException("Promotion non trouvée");
        }
    }


    public List<PromotionEntity> listerPromotions() {
        return promotionRepository.findAll();
    }

    // Récupérer une promotion spécifique
    public PromotionEntity obtenirPromotion(Long promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException("Promotion non trouvée"));
    }

 
    public PromotionEntity findActivePromotion() {
        LocalDate today = LocalDate.now();
        return promotionRepository.findAll().stream()
                .filter(promo -> promo.getActif() && 
                                 !convertToLocalDate(promo.getDateDebut()).isAfter(today) && 
                                 !convertToLocalDate(promo.getDateFin()).isBefore(today))
                .findFirst()
                .orElse(null); // Retourne null si aucune promotion active trouvée
    }
    public double calculerPrixAvecReduction(ServiceEntity service, PromotionEntity promo) {
        double prixInitial = service.getPrix();

        // Vérifiez si le service a une promotion spécifique
        if (service.getPromotion() != null) {
            promo = service.getPromotion();  // Utilise la promotion spécifique du service
        }

        if (promo != null) {
            if (promo.getTypeReduction() == TypeReduction.POURCENTAGE) {
                return prixInitial - (prixInitial * promo.getValeurReduction() / 100);
            }

            if (promo.getTypeReduction() == TypeReduction.MONTANT_FIXE) {
                return prixInitial - promo.getValeurReduction();
            }
        }

        // Si aucune promotion spécifique n'est trouvée, retourner le prix original
        return prixInitial;
    }

    public PromotionEntity trouverParCode(String codePromo) {
        return promotionRepository.findByCodePromo(codePromo)
                .filter(promo -> promo.getActif() &&
                        !convertToLocalDate(promo.getDateDebut()).isAfter(LocalDate.now()) &&
                        !convertToLocalDate(promo.getDateFin()).isBefore(LocalDate.now()))
                .orElse(null);
    }

    // Convertir java.util.Date en java.time.LocalDate
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Supprimer une promotion
    public void supprimerPromotion(Long promotionId) {
        promotionRepository.deleteById(promotionId);
    }
    // Récupérer une promotion par son ID
    public PromotionEntity obtenirPromotionParId(Long promotionId) {
        return promotionRepository.findById(promotionId).orElse(null);
    }
    public PromotionEntity trouverPromoActive() {
        // Supposons que vous avez une méthode pour obtenir toutes les promotions actives
        List<PromotionEntity> promotionsActives = promotionRepository.findAll().stream()
            .filter(promo -> promo.getActif() && convertToLocalDate(promo.getDateDebut()).isBefore(LocalDate.now()) 
                             && convertToLocalDate(promo.getDateFin()).isAfter(LocalDate.now()))
            .collect(Collectors.toList());

        if (promotionsActives.isEmpty()) {
            return null;  // Pas de promotion active
        }

        // Si plusieurs promotions actives, vous pouvez choisir la plus pertinente, par exemple celle avec le plus grand taux de réduction
        return promotionsActives.get(0);  
    }

}

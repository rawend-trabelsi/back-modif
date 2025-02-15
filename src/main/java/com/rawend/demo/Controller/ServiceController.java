package com.rawend.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.rawend.demo.entity.ServiceEntity;
import com.rawend.demo.services.ServiceService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    // Récupérer tous les services
    @GetMapping
    public ResponseEntity<List<ServiceEntity>> getAllServices() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    // Récupérer un service par son ID
    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntity> getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Créer un nouveau service
    @PostMapping
    public ResponseEntity<ServiceEntity> createService(@RequestParam("titre") String titre,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("prix") Double prix,
                                                       @RequestParam("duree") String duree,  // Remplacé "durée" par "duree"
                                                       @RequestParam("image") MultipartFile imageFile) throws IOException {
        
        // Convertir l'image en tableau de bytes
        byte[] imageBytes = imageFile.getBytes();
        String imageName = imageFile.getOriginalFilename();

        // Créer le service
        ServiceEntity service = new ServiceEntity();
        service.setTitre(titre);
        service.setDescription(description);
        service.setPrix(prix);
        service.setDuree(duree);  // Remplacé "durée" par "duree"
        service.setImage(imageBytes);
        service.setImageName(imageName);

        // Sauvegarder le service dans la base de données
        ServiceEntity savedService = serviceService.saveService(service);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedService);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntity> updateService(@PathVariable Long id,
                                                       @RequestParam("titre") String titre,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("prix") Double prix,
                                                       @RequestParam("duree") String duree,
                                                       @RequestParam(value = "image", required = false) MultipartFile imageFile) throws IOException {

        // Récupérer le service existant
        ServiceEntity service = serviceService.findById(id);  // Vous devez avoir cette méthode pour récupérer le service par ID
        if (service == null) {
            return ResponseEntity.notFound().build();  // Retourner une réponse 404 si le service n'est pas trouvé
        }

        // Mise à jour des champs texte
        service.setTitre(titre);
        service.setDescription(description);
        service.setPrix(prix);
        service.setDuree(duree);

        // Si une nouvelle image est envoyée, mettez à jour l'image et son nom
        if (imageFile != null && !imageFile.isEmpty()) {
            byte[] imageBytes = imageFile.getBytes();
            String imageName = imageFile.getOriginalFilename();  // Nouveau nom de l'image
            service.setImage(imageBytes);  // Mettre à jour l'image
            service.setImageName(imageName);  // Mettre à jour le nom de l'image
        }

        // Sauvegarder le service mis à jour
        ServiceEntity updatedService = serviceService.saveService(service);  // Sauvegarde dans la base de données

        // Retourner la réponse avec le service mis à jour
        return ResponseEntity.ok(updatedService);
    }

    // Supprimer un service
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}

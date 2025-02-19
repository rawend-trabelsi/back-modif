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
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

  
    @GetMapping
    public ResponseEntity<List<ServiceEntity>> getAllServices() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }

  
    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntity> getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

   
    @PostMapping
    public ResponseEntity<ServiceEntity> createService(@RequestParam("titre") String titre,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("prix") Double prix,
                                                       @RequestParam("duree") String duree,  // Remplacé "durée" par "duree"
                                                       @RequestParam("image") MultipartFile imageFile) throws IOException {
        
     
        byte[] imageBytes = imageFile.getBytes();
        String imageName = imageFile.getOriginalFilename();

        
        ServiceEntity service = new ServiceEntity();
        service.setTitre(titre);
        service.setDescription(description);
        service.setPrix(prix);
        service.setDuree(duree);  
        service.setImage(imageBytes);
        service.setImageName(imageName);

       
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

   
        ServiceEntity service = serviceService.findById(id);  
        if (service == null) {
            return ResponseEntity.notFound().build(); 
        }

        
        service.setTitre(titre);
        service.setDescription(description);
        service.setPrix(prix);
        service.setDuree(duree);

        
        if (imageFile != null && !imageFile.isEmpty()) {
            byte[] imageBytes = imageFile.getBytes();
            String imageName = imageFile.getOriginalFilename();  
            service.setImage(imageBytes);  
            service.setImageName(imageName);  
        }

     
        ServiceEntity updatedService = serviceService.saveService(service); 
      
        return ResponseEntity.ok(updatedService);
    }

    @GetMapping("/withPromotions")
    public ResponseEntity<List<Map<String, Object>>> getServicesWithPromotions() {
        return ResponseEntity.ok(serviceService.obtenirServicesAvecPromotions());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}

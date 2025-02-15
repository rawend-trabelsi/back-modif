package com.rawend.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;
import com.rawend.demo.Repository.ServiceRepository;
import com.rawend.demo.entity.ServiceEntity;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    // Récupérer tous les services
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    // Récupérer un service par son ID
    public Optional<ServiceEntity> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    // Sauvegarder un service (création ou mise à jour)
    public ServiceEntity saveService(ServiceEntity service) {
        return serviceRepository.save(service);
    }

    // Supprimer un service
    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    public ServiceEntity findById(Long id) {
        // Utilisez la méthode de JpaRepository pour trouver un service par son ID
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
    }
  
    
}

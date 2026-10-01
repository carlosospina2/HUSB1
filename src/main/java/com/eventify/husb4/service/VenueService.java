package com.eventify.husb4.service;

import com.eventify.husb4.exception.ResourceNotFoundException;
import com.eventify.husb4.model.Venue;
import com.eventify.husb4.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa de negocio para Venue.
 * Validaciones + orquestación del repositorio.
 */
@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue save(Venue venue) {
        validate(venue);
        return venueRepository.save(venue);
    }

    public Venue update(Long id, Venue data) {
        Venue existing = findById(id);
        validate(data);
        existing.setName(data.getName());
        existing.setAddress(data.getAddress());
        existing.setCity(data.getCity());
        existing.setCapacity(data.getCapacity());
        return venueRepository.save(existing);
    }

    public void delete(Long id) {
        if (!venueRepository.existsById(id))
            throw new ResourceNotFoundException("Venue", id);
        venueRepository.deleteById(id);
    }

    public Venue findById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));
    }

    public List<Venue> findAll() {
        return venueRepository.findAll();
    }

    public List<Venue> findByCity(String city) {
        return venueRepository.findByCityContainingIgnoreCase(city);
    }

    private void validate(Venue v) {
        if (v.getName() == null || v.getName().isBlank())
            throw new IllegalArgumentException("El nombre del venue es obligatorio.");
        if (v.getCity() == null || v.getCity().isBlank())
            throw new IllegalArgumentException("La ciudad del venue es obligatoria.");
        if (v.getCapacity() == null || v.getCapacity() <= 0)
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
    }
}

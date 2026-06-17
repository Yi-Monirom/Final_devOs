package net.orderzone.idcard.service;

import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.ProfileType;
import net.orderzone.idcard.repository.ProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public List<Profile> findAll() {
        return profileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Profile> findById(Long id) {
        return profileRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Profile> findByUuid(String uuid) {
        return profileRepository.findByUuid(uuid);
    }

    @Transactional(readOnly = true)
    public Optional<Profile> findByRegistrationNumber(String registrationNumber) {
        return profileRepository.findByRegistrationNumber(registrationNumber);
    }

    @Transactional(readOnly = true)
    public List<Profile> findByType(ProfileType type) {
        return profileRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Profile> search(String query) {
        return profileRepository.findByFullNameContainingIgnoreCase(query);
    }

    public Profile save(Profile profile) {
        return profileRepository.save(profile);
    }

    public void deleteById(Long id) {
        profileRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByRegistrationNumber(String registrationNumber) {
        return profileRepository.existsByRegistrationNumber(registrationNumber);
    }
}

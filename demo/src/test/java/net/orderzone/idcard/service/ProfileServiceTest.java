package net.orderzone.idcard.service;

import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.ProfileBuilder;
import net.orderzone.idcard.model.ProfileType;
import net.orderzone.idcard.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileRepository);
    }

    @Test
    void findAll_returnsAllProfiles() {
        when(profileRepository.findAll()).thenReturn(List.of());
        assertNotNull(profileService.findAll());
        verify(profileRepository).findAll();
    }

    @Test
    void findById_returnsProfile() {
        Profile p = ProfileBuilder.buildDefault(ProfileType.STUDENT);
        p.setId(1L);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(p));
        var result = profileService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void save_persistsProfile() {
        Profile p = ProfileBuilder.buildDefault(ProfileType.STUDENT);
        when(profileRepository.save(any())).thenReturn(p);
        var saved = profileService.save(p);
        assertNotNull(saved);
        verify(profileRepository).save(p);
    }

    @Test
    void deleteById_removesProfile() {
        profileService.deleteById(1L);
        verify(profileRepository).deleteById(1L);
    }
}

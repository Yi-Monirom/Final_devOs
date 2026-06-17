package net.orderzone.idcard.controller;

import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.ProfileBuilder;
import net.orderzone.idcard.model.ProfileType;
import net.orderzone.idcard.service.PhotoService;
import net.orderzone.idcard.service.ProfileService;
import net.orderzone.idcard.service.TemplateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private TemplateService templateService;

    @Mock
    private PhotoService photoService;

    @Mock
    private Model model;

    @InjectMocks
    private ProfileController controller;

    @Test
    void detail_returnsDetailView() {
        Profile p = ProfileBuilder.buildDefault(ProfileType.STUDENT);
        p.setId(1L);
        when(profileService.findById(1L)).thenReturn(Optional.of(p));
        String view = controller.detail(1L, model);
        assertEquals("profiles/detail", view);
        verify(model).addAttribute("profile", p);
    }
}

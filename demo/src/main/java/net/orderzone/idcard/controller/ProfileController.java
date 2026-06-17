package net.orderzone.idcard.controller;

import jakarta.validation.Valid;
import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.ProfileBuilder;
import net.orderzone.idcard.model.ProfileType;
import net.orderzone.idcard.service.PhotoService;
import net.orderzone.idcard.service.ProfileService;
import net.orderzone.idcard.service.TemplateService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final TemplateService templateService;
    private final PhotoService photoService;

    public ProfileController(ProfileService profileService, TemplateService templateService, PhotoService photoService) {
        this.profileService = profileService;
        this.templateService = templateService;
        this.photoService = photoService;
    }

    @GetMapping
    public String list(Model model, @PageableDefault(size = 20) Pageable pageable,
                       @RequestParam(required = false) String query,
                       @RequestParam(required = false) ProfileType type) {
        if (query != null && !query.isBlank()) {
            model.addAttribute("profiles", profileService.search(query));
        } else if (type != null) {
            model.addAttribute("profiles", profileService.findByType(type));
        } else {
            model.addAttribute("profiles", profileService.findAll(pageable));
        }
        model.addAttribute("query", query);
        model.addAttribute("type", type != null ? type.name() : null);
        return "profiles/list";
    }

    @GetMapping("/new")
    public String createForm(Model model, @RequestParam(defaultValue = "STUDENT") ProfileType type) {
        model.addAttribute("profile", ProfileBuilder.buildDefault(type));
        model.addAttribute("templates", templateService.findAll());
        model.addAttribute("profileTypes", ProfileType.values());
        return "profiles/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("profile") Profile profile,
                         BindingResult result,
                         @RequestParam("photo") MultipartFile photo,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("templates", templateService.findAll());
            model.addAttribute("profileTypes", ProfileType.values());
            return "profiles/form";
        }
        if (profile.getUuid() == null || profile.getUuid().isBlank()) {
            profile.setUuid(UUID.randomUUID().toString());
        }
        if (profile.getRegistrationNumber() == null || profile.getRegistrationNumber().isBlank()) {
            profile.setRegistrationNumber(ProfileBuilder.generateRegistrationNumber(profile.getType()));
        }
        if (!photo.isEmpty()) {
            try {
                String filename = photoService.store(photo);
                profile.setPhotoFileName(filename);
                profile.setPhotoContentType(photo.getContentType());
            } catch (IllegalArgumentException e) {
                result.rejectValue("photoFileName", "error.photo", e.getMessage());
                model.addAttribute("templates", templateService.findAll());
                model.addAttribute("profileTypes", ProfileType.values());
                return "profiles/form";
            }
        }
        profileService.save(profile);
        return "redirect:/profiles";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var profile = profileService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid profile ID: " + id));
        model.addAttribute("profile", profile);
        model.addAttribute("templates", templateService.findAll());
        model.addAttribute("profileTypes", ProfileType.values());
        return "profiles/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("profile") Profile updated,
                         BindingResult result,
                         @RequestParam("photo") MultipartFile photo,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("templates", templateService.findAll());
            model.addAttribute("profileTypes", ProfileType.values());
            return "profiles/form";
        }
        var existing = profileService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid profile ID: " + id));
        existing.setFullName(updated.getFullName());
        existing.setType(updated.getType());
        existing.setDepartment(updated.getDepartment());
        existing.setTitle(updated.getTitle());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setBloodGroup(updated.getBloodGroup());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setTemplate(updated.getTemplate());
        existing.setBarcodeType(updated.getBarcodeType());
        if (!photo.isEmpty()) {
            try {
                String filename = photoService.store(photo);
                existing.setPhotoFileName(filename);
                existing.setPhotoContentType(photo.getContentType());
            } catch (IllegalArgumentException e) {
                result.rejectValue("photoFileName", "error.photo", e.getMessage());
                model.addAttribute("templates", templateService.findAll());
                model.addAttribute("profileTypes", ProfileType.values());
                return "profiles/form";
            }
        }
        profileService.save(existing);
        return "redirect:/profiles";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        profileService.deleteById(id);
        ra.addFlashAttribute("message", "Profile deleted successfully");
        return "redirect:/profiles";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var profile = profileService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid profile ID: " + id));
        model.addAttribute("profile", profile);
        return "profiles/detail";
    }
}

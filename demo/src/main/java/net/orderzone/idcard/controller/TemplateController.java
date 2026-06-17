package net.orderzone.idcard.controller;

import jakarta.validation.Valid;
import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.service.TemplateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("templates", templateService.findAll());
        return "templates/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("template", Template.builder().build());
        return "templates/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("template") Template template, BindingResult result) {
        if (result.hasErrors()) {
            return "templates/form";
        }
        templateService.save(template);
        return "redirect:/templates";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var template = templateService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid template ID: " + id));
        model.addAttribute("template", template);
        return "templates/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("template") Template updated, BindingResult result) {
        if (result.hasErrors()) {
            return "templates/form";
        }
        var existing = templateService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid template ID: " + id));
        existing.setCode(updated.getCode());
        existing.setName(updated.getName());
        existing.setOrganizationName(updated.getOrganizationName());
        existing.setLayout(updated.getLayout());
        existing.setPrimaryColor(updated.getPrimaryColor());
        existing.setSecondaryColor(updated.getSecondaryColor());
        existing.setTextColor(updated.getTextColor());
        existing.setTagline(updated.getTagline());
        templateService.save(existing);
        return "redirect:/templates";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        templateService.deleteById(id);
        ra.addFlashAttribute("message", "Template deleted successfully");
        return "redirect:/templates";
    }
}

package net.orderzone.idcard.service;

import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.repository.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TemplateService {

    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public List<Template> findAll() {
        return templateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Template> findById(Long id) {
        return templateRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Template> findByCode(String code) {
        return templateRepository.findByCode(code);
    }

    public Template save(Template template) {
        return templateRepository.save(template);
    }

    public void deleteById(Long id) {
        templateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return templateRepository.existsByCode(code);
    }
}

package net.orderzone.idcard.controller;

import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.service.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cards")
public class CardController {

    private final ProfileService profileService;
    private final TemplateService templateService;
    private final PdfGenerationService pdfService;
    private final QrCodeService qrCodeService;
    private final BarcodeService barcodeService;
    private final PhotoService photoService;

    public CardController(ProfileService profileService, TemplateService templateService,
                          PdfGenerationService pdfService, QrCodeService qrCodeService,
                          BarcodeService barcodeService, PhotoService photoService) {
        this.profileService = profileService;
        this.templateService = templateService;
        this.pdfService = pdfService;
        this.qrCodeService = qrCodeService;
        this.barcodeService = barcodeService;
        this.photoService = photoService;
    }

    @GetMapping("/preview/{profileId}")
    public String preview(@PathVariable Long profileId, Model model) {
        var profile = profileService.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid profile ID: " + profileId));
        model.addAttribute("profile", profile);
        return "card/preview";
    }

    @GetMapping("/pdf/{profileId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long profileId) {
        var profile = profileService.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid profile ID: " + profileId));
        var template = profile.getTemplate() != null
                ? profile.getTemplate()
                : templateService.findByCode("default").orElse(null);
        byte[] pdfBytes = pdfService.generateIdCardPdf(profile, template);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=idcard_" + profile.getRegistrationNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/batch")
    public String batchForm(Model model) {
        model.addAttribute("profiles", profileService.findAll());
        model.addAttribute("templates", templateService.findAll());
        return "card/batch";
    }

    @PostMapping("/batch")
    public ResponseEntity<byte[]> batchDownload(@RequestParam List<Long> profileIds,
                                                 @RequestParam(required = false) Long templateId) {
        var template = templateId != null
                ? templateService.findById(templateId).orElse(null)
                : null;
        var baos = new java.io.ByteArrayOutputStream();
        var merged = new com.lowagie.text.Document();
        try {
            var copier = new com.lowagie.text.pdf.PdfCopy(merged, baos);
            merged.open();
            for (Long id : profileIds) {
                var profile = profileService.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid profile ID: " + id));
                var t = template != null ? template : profile.getTemplate();
                byte[] pdfBytes = pdfService.generateIdCardPdf(profile, t);
                var reader = new com.lowagie.text.pdf.PdfReader(pdfBytes);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    copier.addPage(copier.getImportedPage(reader, i));
                }
                copier.freeReader(reader);
                reader.close();
            }
            merged.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate batch PDF", e);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=batch_id_cards.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @GetMapping("/qr/{profileId}")
    public ResponseEntity<byte[]> qrCode(@PathVariable Long profileId) {
        var profile = profileService.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid profile ID: " + profileId));
        String data = "UUID:" + profile.getUuid() + "|Reg:" + profile.getRegistrationNumber();
        byte[] qr = qrCodeService.generateQrCode(data, 200, 200);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qr);
    }

    @GetMapping("/barcode/{profileId}")
    public ResponseEntity<byte[]> barcode(@PathVariable Long profileId) {
        var profile = profileService.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid profile ID: " + profileId));
        byte[] barcode = barcodeService.generateBarcode(
                profile.getRegistrationNumber(), profile.getBarcodeType(), 200, 60);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(barcode);
    }

    @GetMapping("/photos/{filename}")
    public ResponseEntity<Resource> servePhoto(@PathVariable String filename) {
        Resource file = photoService.load(filename);
        String contentType = filename.endsWith(".png") ? "image/png" : "image/jpeg";
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(file);
    }
}

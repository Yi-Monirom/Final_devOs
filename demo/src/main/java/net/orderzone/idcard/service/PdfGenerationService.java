package net.orderzone.idcard.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.Template;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
public class PdfGenerationService {

    private static final float CARD_WIDTH = 340f;
    private static final float CARD_HEIGHT = 400f;

    private static final float HEADER_HEIGHT = 50f;
    private static final float PHOTO_SIZE = 70f;
    private static final float PHOTO_Y = 255f;
    private static final float TEXT_COLUMN_X = 20f;
    private static final float TEXT_WIDTH = CARD_WIDTH - 40f;

    private final QrCodeService qrCodeService;
    private final BarcodeService barcodeService;
    private final PhotoService photoService;

    public PdfGenerationService(QrCodeService qrCodeService, BarcodeService barcodeService, PhotoService photoService) {
        this.qrCodeService = qrCodeService;
        this.barcodeService = barcodeService;
        this.photoService = photoService;
    }

    public byte[] generateIdCardPdf(Profile profile, Template template) {
        if (template == null) {
            template = Template.builder()
                .code("FALLBACK")
                .name("Fallback")
                .organizationName("Organization")
                .primaryColor("#4f46e5")
                .secondaryColor("#6366f1")
                .textColor("#111827")
                .build();
        }

        var baos = new ByteArrayOutputStream();
        var document = new Document(new Rectangle(CARD_WIDTH, CARD_HEIGHT), 0, 0, 0, 0);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        Color primary = Color.decode(template.getPrimaryColor());
        Color textColor = Color.decode(template.getTextColor());
        Color secondary = Color.decode(template.getSecondaryColor());

        PdfContentByte canvas = writer.getDirectContent();

        // White background
        canvas.setColorFill(Color.WHITE);
        canvas.roundRectangle(0, 0, CARD_WIDTH, CARD_HEIGHT, 8);
        canvas.fill();

        // Header band
        canvas.setColorFill(primary);
        canvas.roundRectangle(0, CARD_HEIGHT - HEADER_HEIGHT, CARD_WIDTH, HEADER_HEIGHT, 0);
        canvas.fill();

        // Organization name in header
        Font headerFont = new Font(Font.HELVETICA, 14, Font.BOLD, Color.WHITE);
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Paragraph(template.getOrganizationName(), headerFont),
                CARD_WIDTH / 2, CARD_HEIGHT - HEADER_HEIGHT + 15, 0);

        // Tagline if present
        if (template.getTagline() != null && !template.getTagline().isBlank()) {
            Font taglineFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.WHITE);
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Paragraph(template.getTagline(), taglineFont),
                    CARD_WIDTH / 2, CARD_HEIGHT - HEADER_HEIGHT + 3, 0);
        }

        float y = CARD_HEIGHT - HEADER_HEIGHT - 10;

        // Profile photo
        if (profile.hasPhoto() && profile.getPhotoFileName() != null) {
            try {
                byte[] photoBytes = photoService.loadBytes(profile.getPhotoFileName());
                Image photoImg = Image.getInstance(photoBytes);
                photoImg.scaleToFit(PHOTO_SIZE, PHOTO_SIZE);
                photoImg.setAbsolutePosition((CARD_WIDTH - photoImg.getScaledWidth()) / 2, PHOTO_Y);
                canvas.addImage(photoImg);

                // Circle clip — draw a circular border
                canvas.setColorStroke(primary);
                canvas.setLineWidth(2);
                float cx = CARD_WIDTH / 2;
                float cy = PHOTO_Y + photoImg.getScaledHeight() / 2;
                float r = photoImg.getScaledWidth() / 2;
                canvas.circle(cx, cy, r);
                canvas.stroke();
            } catch (Exception e) {
                // skip photo on error
            }
            y = PHOTO_Y - 10;
        }

        // Full name
        Font nameFont = new Font(Font.HELVETICA, 12, Font.BOLD, textColor);
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Paragraph(profile.getFullName(), nameFont),
                CARD_WIDTH / 2, y, 0);
        y -= 18;

        // Detail lines
        Font labelFont = new Font(Font.HELVETICA, 8, Font.BOLD, textColor);
        Font valueFont = new Font(Font.HELVETICA, 8, Font.NORMAL, textColor);

        y = addDetailLine(canvas, labelFont, valueFont, "Reg No", profile.getRegistrationNumber(), y);
        y = addDetailLine(canvas, labelFont, valueFont, "Type", profile.getType().name(), y);
        if (profile.getDepartment() != null)
            y = addDetailLine(canvas, labelFont, valueFont, "Dept", profile.getDepartment(), y);
        if (profile.getTitle() != null)
            y = addDetailLine(canvas, labelFont, valueFont, "Title", profile.getTitle(), y);

        y -= 8;

        // Barcode
        if (profile.getBarcodeType() != null && profile.getRegistrationNumber() != null) {
            try {
                byte[] barcodeBytes = barcodeService.generateBarcode(
                        profile.getRegistrationNumber(), profile.getBarcodeType(), 200, 60);
                Image barcodeImg = Image.getInstance(barcodeBytes);
                barcodeImg.scalePercent(70);
                barcodeImg.setAbsolutePosition(
                        (CARD_WIDTH - barcodeImg.getScaledWidth()) / 2,
                        y - barcodeImg.getScaledHeight());
                canvas.addImage(barcodeImg);
                y = y - barcodeImg.getScaledHeight() - 8;
            } catch (Exception e) {
                // skip barcode on error
            }
        }

        // QR Code
        try {
            String qrData = "UUID:" + profile.getUuid() + "|Reg:" + profile.getRegistrationNumber();
            byte[] qrBytes = qrCodeService.generateQrCode(qrData, 150, 150);
            Image qrImg = Image.getInstance(qrBytes);
            qrImg.scalePercent(45);
            qrImg.setAbsolutePosition(
                    CARD_WIDTH - qrImg.getScaledWidth() - 15,
                    y - qrImg.getScaledHeight());
            canvas.addImage(qrImg);
        } catch (Exception e) {
            // skip QR on error
        }

        // Footer: issue / expiry dates
        if (profile.getIssueDate() != null) {
            Font footerFont = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.GRAY);
            StringBuilder footer = new StringBuilder("Issued: " + profile.getIssueDate());
            if (profile.getExpiryDate() != null)
                footer.append(" | Expires: ").append(profile.getExpiryDate());
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Paragraph(footer.toString(), footerFont),
                    CARD_WIDTH / 2, 8, 0);
        }

        document.close();
        return baos.toByteArray();
    }

    private float addDetailLine(PdfContentByte canvas, Font labelFont, Font valueFont,
                                 String label, String value, float y) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ": ", labelFont));
        p.add(new Chunk(value, valueFont));
        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, p, TEXT_COLUMN_X, y, 0);
        return y - 14;
    }
}

package net.orderzone.idcard.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import net.orderzone.idcard.model.BarcodeType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class BarcodeService {

    public byte[] generateBarcode(String text, BarcodeType type, int width, int height) {
        try {
            if (type == BarcodeType.EAN_13 && !text.matches("\\d{12,13}")) {
                throw new IllegalArgumentException("EAN-13 requires 12-13 digits, got: " + text);
            }
            var format = type == BarcodeType.CODE_128 ? BarcodeFormat.CODE_128 : BarcodeFormat.EAN_13;
            var writer = type == BarcodeType.CODE_128 ? new Code128Writer() : new EAN13Writer();
            var bitMatrix = writer.encode(text, format, width, height);
            var baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate barcode", e);
        }
    }
}

package net.orderzone.idcard.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private final QrCodeService qrCodeService = new QrCodeService();

    @Test
    void generateQrCode_returnsNonEmptyBytes() {
        byte[] qr = qrCodeService.generateQrCode("test data", 200, 200);
        assertNotNull(qr);
        assertTrue(qr.length > 0);
    }
}

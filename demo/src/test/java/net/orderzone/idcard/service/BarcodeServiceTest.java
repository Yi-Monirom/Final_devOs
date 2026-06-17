package net.orderzone.idcard.service;

import net.orderzone.idcard.model.BarcodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BarcodeServiceTest {

    private final BarcodeService barcodeService = new BarcodeService();

    @Test
    void generateCode128_returnsNonEmptyBytes() {
        byte[] barcode = barcodeService.generateBarcode("TEST123", BarcodeType.CODE_128, 200, 60);
        assertNotNull(barcode);
        assertTrue(barcode.length > 0);
    }
}

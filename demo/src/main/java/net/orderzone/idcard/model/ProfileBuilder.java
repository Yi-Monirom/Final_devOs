package net.orderzone.idcard.model;

import java.time.LocalDate;
import java.util.UUID;

public class ProfileBuilder {

    public static Profile buildDefault(ProfileType type) {
        return Profile.builder()
                .uuid(UUID.randomUUID().toString())
                .registrationNumber(generateRegistrationNumber(type))
                .type(type)
                .barcodeType(BarcodeType.CODE_128)
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(3))
                .build();
    }

    public static String generateRegistrationNumber(ProfileType type) {
        String prefix = switch (type) {
            case STUDENT -> "STU";
            case EMPLOYEE -> "EMP";
            case USER -> "USR";
        };
        String year = String.valueOf(LocalDate.now().getYear());
        String seq = String.format("%04d", (int) (Math.random() * 9999));
        return "%s-%s-%s".formatted(year, prefix, seq);
    }
}

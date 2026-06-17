package net.orderzone.idcard.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfileBuilderTest {

    @Test
    void buildDefault_createsProfileWithCorrectType() {
        Profile p = ProfileBuilder.buildDefault(ProfileType.STUDENT);
        assertEquals(ProfileType.STUDENT, p.getType());
        assertNotNull(p.getUuid());
        assertNotNull(p.getRegistrationNumber());
        assertTrue(p.getRegistrationNumber().startsWith("2026-STU-"));
    }

    @Test
    void buildDefault_createsEmployeeProfile() {
        Profile p = ProfileBuilder.buildDefault(ProfileType.EMPLOYEE);
        assertEquals(ProfileType.EMPLOYEE, p.getType());
        assertTrue(p.getRegistrationNumber().startsWith("2026-EMP-"));
    }

    @Test
    void buildDefault_createsUserProfile() {
        Profile p = ProfileBuilder.buildDefault(ProfileType.USER);
        assertEquals(ProfileType.USER, p.getType());
        assertTrue(p.getRegistrationNumber().startsWith("2026-USR-"));
    }

    @Test
    void generateRegistrationNumber_returnsFormattedString() {
        String reg = ProfileBuilder.generateRegistrationNumber(ProfileType.STUDENT);
        assertTrue(reg.matches("\\d{4}-(STU|EMP|USR)-\\d{4}"));
    }
}

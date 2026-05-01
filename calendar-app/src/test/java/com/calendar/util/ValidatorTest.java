package com.calendar.util;

import com.calendar.model.dto.AppointmentDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTest {
    @Test
    void validateAppointmentDtoRejectsEmptyName() {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setTitle("   ");
        dto.setStartTime("2026-05-02 09:00");
        dto.setEndTime("2026-05-02 10:00");

        Validator.ValidationResult result = Validator.validateAppointmentDTO(dto);

        assertFalse(result.isValid());
    }

    @Test
    void isValidTimeRangeReturnsTrueForOrderedTimes() {
        assertTrue(Validator.isValidTimeRange(
                java.time.LocalDateTime.parse("2026-05-02T09:00:00"),
                java.time.LocalDateTime.parse("2026-05-02T10:00:00")));
    }
}

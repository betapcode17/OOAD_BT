package com.calendar.util;

import java.util.List;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.calendar.model.dto.AppointmentDTO;

public final class Validator {
    private Validator() {
    }

    public static ValidationResult validateAppointmentDTO(AppointmentDTO dto) {
        List<String> errors = new ArrayList<>();
        if (dto == null) {
            errors.add("Appointment data is required.");
            return new ValidationResult(errors);
        }
        if (isBlank(dto.getTitle())) {
            errors.add("Appointment name cannot be empty.");
        }
        if (isBlank(dto.getStartTime())) {
            errors.add("Start time is required.");
        }
        if (isBlank(dto.getEndTime())) {
            errors.add("End time is required.");
        }
        if (!isBlank(dto.getReminderMinutes())) {
            try {
                int reminder = Integer.parseInt(dto.getReminderMinutes().trim());
                if (reminder < 0) {
                    errors.add("Reminder must be zero or a positive number.");
                }
            } catch (NumberFormatException ex) {
                errors.add("Reminder must be a valid number.");
            }
        }
        return new ValidationResult(errors);
    }

    public static boolean isValidTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    public static List<Long> parseParticipantIds(String rawValue) {
        List<Long> participantIds = new ArrayList<>();
        if (isBlank(rawValue)) {
            return participantIds;
        }
        String[] parts = rawValue.split(",");
        for (String part : parts) {
            if (!part.isBlank()) {
                participantIds.add(Long.valueOf(part));
            }
        }
        return participantIds;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static final class ValidationResult {
        private final List<String> errors;

        public ValidationResult(List<String> errors) {
            this.errors = List.copyOf(errors);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}

package com.calendar.model.bo;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.calendar.model.bean.Appointment;
import com.calendar.model.bean.GroupMeeting;
import com.calendar.model.bean.Reminder;
import com.calendar.model.dao.AppointmentDAO;
import com.calendar.model.dao.ReminderDAO;
import com.calendar.model.dto.AppointmentDTO;
import com.calendar.util.Validator;

public class AppointmentBO {
    private static final Long CURRENT_USER_ID = 1L;
    private static final Long DEFAULT_CALENDAR_ID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int DEFAULT_REMINDER_MINUTES = 15;
    private static final int SEARCH_WINDOW_DAYS = 7;

    private final AppointmentDAO appointmentDAO;
    private final ReminderDAO reminderDAO;

    public AppointmentBO() {
        this(new AppointmentDAO(), new ReminderDAO());
    }

    public AppointmentBO(AppointmentDAO appointmentDAO, ReminderDAO reminderDAO) {
        this.appointmentDAO = appointmentDAO;
        this.reminderDAO = reminderDAO;
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        return appointmentDAO.findAll();
    }

    public SaveOutcome addAppointment(AppointmentDTO dto) {
        return saveAppointment(dto, false);
    }

    public SaveOutcome addAppointmentAllowConflict(AppointmentDTO dto) {
        return saveAppointment(dto, true);
    }

    private SaveOutcome saveAppointment(AppointmentDTO dto, boolean allowConflict) {
        Validator.ValidationResult validationResult = Validator.validateAppointmentDTO(dto);
        if (!validationResult.isValid()) {
            return SaveOutcome.failure(validationResult.getErrors(), List.of());
        }

        try {
            LocalDateTime startTime = parseDateTime(dto.getStartTime());
            LocalDateTime endTime = parseDateTime(dto.getEndTime());
            if (!Validator.isValidTimeRange(startTime, endTime)) {
                return SaveOutcome.failure(List.of("End time must be after start time."), List.of());
            }

            List<Long> participantIds = resolveParticipantIds(dto.getParticipantIds());
            List<Appointment> conflicts = appointmentDAO.findConflictingAppointments(startTime, endTime, participantIds);
            if (!allowConflict && !conflicts.isEmpty()) {
                List<String> suggestions = suggestGroupMeeting(dto);
                return SaveOutcome.conflict(conflicts, suggestions);
            }

            Appointment appointment = buildAppointment(dto, startTime, endTime, participantIds);
            Appointment savedAppointment = appointmentDAO.save(appointment, participantIds);
            saveReminderIfNeeded(dto, savedAppointment.getId());
            return SaveOutcome.success(savedAppointment, List.of("Appointment saved successfully."));
        } catch (DateTimeParseException | NumberFormatException exception) {
            String message = exception instanceof DateTimeParseException
                    ? "Use the format yyyy-MM-dd HH:mm for start and end time."
                    : "Participant IDs must be comma-separated numbers.";
            return SaveOutcome.failure(List.of(message), List.of());
        } catch (SQLException exception) {
            return SaveOutcome.failure(List.of("Database error: " + exception.getMessage()), List.of());
        }
    }

    public List<String> suggestGroupMeeting(AppointmentDTO dto) {
        try {
            LocalDateTime startTime = parseDateTime(dto.getStartTime());
            LocalDateTime endTime = parseDateTime(dto.getEndTime());
            if (!Validator.isValidTimeRange(startTime, endTime)) {
                return List.of("Please enter a valid time range before asking for suggestions.");
            }

            List<Long> participantIds = resolveParticipantIds(dto.getParticipantIds());
            if (participantIds.size() < 2) {
                return List.of("Add at least two participant IDs to get a group meeting suggestion.");
            }

            long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
            LocalDateTime searchStart = startTime;
            LocalDateTime searchEnd = startTime.plusDays(SEARCH_WINDOW_DAYS);
            List<String> suggestions = new ArrayList<>();

            for (LocalDateTime candidateStart = searchStart; candidateStart.isBefore(searchEnd); candidateStart = candidateStart.plusMinutes(30)) {
                LocalDateTime candidateEnd = candidateStart.plusMinutes(durationMinutes);
                List<Appointment> conflicts = appointmentDAO.findConflictingAppointments(candidateStart, candidateEnd, participantIds);
                if (conflicts.isEmpty()) {
                    suggestions.add("Suggested common slot: " + candidateStart.format(FORMATTER) + " to " + candidateEnd.format(FORMATTER));
                    break;
                }
            }

            if (suggestions.isEmpty()) {
                suggestions.add("No shared slot found in the next 7 days.");
            }
            return suggestions;
        } catch (DateTimeParseException | NumberFormatException exception) {
            String message = exception instanceof DateTimeParseException
                    ? "Use the format yyyy-MM-dd HH:mm for start and end time."
                    : "Participant IDs must be comma-separated numbers.";
            return List.of(message);
        } catch (SQLException exception) {
            return List.of("Unable to generate a suggestion: " + exception.getMessage());
        }
    }

    // New helper methods for UI-driven group meeting flows
    public List<Appointment> checkConflict(LocalDateTime startTime, LocalDateTime endTime, List<Long> participantIds) throws SQLException {
        return appointmentDAO.findConflictingAppointments(startTime, endTime, participantIds);
    }

    public Appointment findMatchingGroupMeeting(String title, long durationMinutes) throws SQLException {
        return appointmentDAO.findMatchingGroupMeeting(title, durationMinutes);
    }

    public void addParticipantToGroup(Long appointmentId, String participantName) throws SQLException {
        com.calendar.model.dao.GroupParticipantDAO gpdao = new com.calendar.model.dao.GroupParticipantDAO();
        gpdao.insert(appointmentId, participantName);
    }

    private Appointment buildAppointment(AppointmentDTO dto, LocalDateTime startTime, LocalDateTime endTime, List<Long> participantIds) {
        Appointment appointment = participantIds.size() > 1 ? new GroupMeeting() : new Appointment();
        appointment.setCalendarId(parseCalendarId(dto.getCalendarId()));
        appointment.setTitle(dto.getTitle().trim());
        appointment.setLocation(dto.getLocation() == null ? "" : dto.getLocation().trim());
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setMeetingType(participantIds.size() > 1 ? "GROUP" : "PERSONAL");
        if (appointment instanceof GroupMeeting groupMeeting) {
            groupMeeting.setParticipantIds(participantIds);
        }
        return appointment;
    }

    private void saveReminderIfNeeded(AppointmentDTO dto, Long appointmentId) throws SQLException {
        String raw = dto.getReminderMinutes();
        if (raw == null || raw.trim().isEmpty()) {
            raw = String.valueOf(DEFAULT_REMINDER_MINUTES);
        }
        String[] parts = raw.split(",");
        for (String p : parts) {
            String t = p.trim();
            if (t.isEmpty()) continue;
            try {
                int minutes = Integer.parseInt(t);
                Reminder reminder = new Reminder();
                reminder.setAppointmentId(appointmentId);
                reminder.setReminderMinutes(minutes);
                reminder.setSent(Boolean.FALSE);
                reminderDAO.save(reminder);
            } catch (NumberFormatException ex) {
                // skip invalid entry
            }
        }
    }

    private LocalDateTime parseDateTime(String rawValue) {
        return LocalDateTime.parse(rawValue.trim(), FORMATTER);
    }

    private Long parseCalendarId(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return DEFAULT_CALENDAR_ID;
        }
        return Long.valueOf(rawValue);
    }

    private List<Long> resolveParticipantIds(String rawValue) {
        List<Long> participantIds = new ArrayList<>(Validator.parseParticipantIds(rawValue));
        if (!participantIds.contains(CURRENT_USER_ID)) {
            participantIds.add(CURRENT_USER_ID);
        }
        participantIds.sort(Comparator.naturalOrder());
        return participantIds;
    }

    public static final class SaveOutcome {
        private final boolean success;
        private final Appointment appointment;
        private final List<Appointment> conflicts;
        private final List<String> messages;
        private final List<String> suggestions;

        private SaveOutcome(boolean success, Appointment appointment, List<Appointment> conflicts, List<String> messages, List<String> suggestions) {
            this.success = success;
            this.appointment = appointment;
            this.conflicts = conflicts;
            this.messages = messages;
            this.suggestions = suggestions;
        }

        public static SaveOutcome success(Appointment appointment, List<String> messages) {
            return new SaveOutcome(true, appointment, List.of(), List.copyOf(messages), List.of());
        }

        public static SaveOutcome failure(List<String> messages, List<String> suggestions) {
            return new SaveOutcome(false, null, List.of(), List.copyOf(messages), List.copyOf(suggestions));
        }

        public static SaveOutcome conflict(List<Appointment> conflicts, List<String> suggestions) {
            return new SaveOutcome(false, null, List.copyOf(conflicts), List.of("The new appointment conflicts with an existing appointment."), List.copyOf(suggestions));
        }

        public boolean isSuccess() {
            return success;
        }

        public Appointment getAppointment() {
            return appointment;
        }

        public List<Appointment> getConflicts() {
            return conflicts;
        }

        public List<String> getMessages() {
            return messages;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }
    }
}

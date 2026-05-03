package com.calendar.controller;

import javax.swing.JOptionPane;

import com.calendar.model.bo.AppointmentBO;
import com.calendar.model.dto.AppointmentDTO;
import com.calendar.view.AddAppointmentUI;
import com.calendar.view.MainUI;

public class AddAppointmentController {
    private final MainUI mainUI;
    private final AppointmentBO appointmentBO;

    public AddAppointmentController(MainUI mainUI, AppointmentBO appointmentBO) {
        this.mainUI = mainUI;
        this.appointmentBO = appointmentBO;
    }

    public void handleSave(AddAppointmentUI addAppointmentUI) {
        AppointmentDTO dto = addAppointmentUI.getAppointmentDTO();
        // Before creating a new appointment, check if a matching group meeting already exists
        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            java.time.LocalDateTime start = java.time.LocalDateTime.parse(dto.getStartTime(), fmt);
            java.time.LocalDateTime end = java.time.LocalDateTime.parse(dto.getEndTime(), fmt);
            long duration = java.time.Duration.between(start, end).toMinutes();
            var match = appointmentBO.findMatchingGroupMeeting(dto.getTitle(), duration);
            if (match != null) {
                int choice = JOptionPane.showOptionDialog(
                        addAppointmentUI,
                    "A similar group meeting already exists.\nDo you want to join it?",
                        "Group Meeting",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new Object[]{"YES", "NO"},
                        "YES");
                if (choice == JOptionPane.YES_OPTION) {
                    appointmentBO.addParticipantToGroup(match.getId(), "User");
                    JOptionPane.showMessageDialog(addAppointmentUI, "You have been added to the group meeting.", "Joined", JOptionPane.INFORMATION_MESSAGE);
                    mainUI.refreshAppointments();
                    return;
                }
                // else fallthrough to create a new appointment
            }
        } catch (java.time.format.DateTimeParseException | java.sql.SQLException ex) {
            // Parsing or DB errors — fall back to normal creation flow and let BO handle validation/errors
        }

        AppointmentBO.SaveOutcome outcome = appointmentBO.addAppointment(dto);
        if (outcome.isSuccess()) {
            JOptionPane.showMessageDialog(addAppointmentUI, "Appointment saved successfully.", "Appointment Saved", JOptionPane.INFORMATION_MESSAGE);
            addAppointmentUI.clearForm();
            mainUI.refreshAppointments();
            return;
        }

        if (!outcome.getConflicts().isEmpty()) {
            int decision = JOptionPane.showOptionDialog(
                    addAppointmentUI,
                    "⚠ You already have an appointment in this time slot!",
                    "Conflict",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new Object[]{"Choose Again", "Overwrite"},
                    "Choose Again");

            if (decision == JOptionPane.NO_OPTION) {
                AppointmentBO.SaveOutcome overwriteOutcome = appointmentBO.addAppointmentAllowConflict(dto);
                if (overwriteOutcome.isSuccess()) {
                    JOptionPane.showMessageDialog(addAppointmentUI, "Appointment overwritten and saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
                    addAppointmentUI.clearForm();
                    mainUI.refreshAppointments();
                } else {
                    JOptionPane.showMessageDialog(addAppointmentUI, buildOutcomeMessage(overwriteOutcome), "Cannot Save Appointment", JOptionPane.WARNING_MESSAGE);
                }
            }
            return;
        }

        // Show validation or failure messages inline in the add form instead of a modal dialog
        addAppointmentUI.showValidationMessages(outcome.getMessages());
    }

    private String buildOutcomeMessage(AppointmentBO.SaveOutcome outcome) {
        StringBuilder builder = new StringBuilder();
        for (String message : outcome.getMessages()) {
            builder.append(message).append(System.lineSeparator());
        }
        if (!outcome.getConflicts().isEmpty()) {
            builder.append(System.lineSeparator()).append("Conflicts:").append(System.lineSeparator());
            outcome.getConflicts().forEach(appointment -> builder.append("- ")
                    .append(appointment.getTitle())
                    .append(" [")
                    .append(appointment.getStartTime())
                    .append(" - ")
                    .append(appointment.getEndTime())
                    .append("]")
                    .append(System.lineSeparator()));
        }
        if (!outcome.getSuggestions().isEmpty()) {
            builder.append(System.lineSeparator()).append("Suggestions:").append(System.lineSeparator());
            outcome.getSuggestions().forEach(suggestion -> builder.append("- ").append(suggestion).append(System.lineSeparator()));
        }
        return builder.toString().trim();
    }
}

package com.calendar.controller;

import com.calendar.model.bo.AppointmentBO;
import com.calendar.model.dto.AppointmentDTO;
import com.calendar.view.AddAppointmentUI;
import com.calendar.view.MainUI;

import javax.swing.JOptionPane;
import java.util.List;

public class AddAppointmentController {
    private final MainUI mainUI;
    private final AppointmentBO appointmentBO;

    public AddAppointmentController(MainUI mainUI, AppointmentBO appointmentBO) {
        this.mainUI = mainUI;
        this.appointmentBO = appointmentBO;
    }

    public void handleSave(AddAppointmentUI addAppointmentUI) {
        AppointmentDTO dto = addAppointmentUI.getAppointmentDTO();
        AppointmentBO.SaveOutcome outcome = appointmentBO.addAppointment(dto);
        if (outcome.isSuccess()) {
            JOptionPane.showMessageDialog(addAppointmentUI, joinMessages(outcome.getMessages()), "Appointment Saved", JOptionPane.INFORMATION_MESSAGE);
            addAppointmentUI.clearForm();
            mainUI.refreshAppointments();
            return;
        }

        JOptionPane.showMessageDialog(addAppointmentUI, buildOutcomeMessage(outcome), "Cannot Save Appointment", JOptionPane.WARNING_MESSAGE);
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

    private String joinMessages(List<String> messages) {
        return String.join(System.lineSeparator(), messages);
    }
}

package com.calendar.model.bean;

public class Reminder {
    private Long id;
    private Long appointmentId;
    private Integer reminderMinutes;
    private Boolean sent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(Integer reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public Boolean getSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }
}

package com.calendar.model.dao;

import com.calendar.model.bean.Reminder;
import com.calendar.model.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReminderDAO {
    public Reminder save(Reminder reminder) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO reminders(appointment_id, reminder_minutes, sent) VALUES(?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, reminder.getAppointmentId());
            preparedStatement.setInt(2, reminder.getReminderMinutes());
            preparedStatement.setBoolean(3, Boolean.TRUE.equals(reminder.getSent()));
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reminder.setId(generatedKeys.getLong(1));
                }
            }
        }
        return reminder;
    }

    public List<Reminder> findByAppointmentId(Long appointmentId) throws SQLException {
        List<Reminder> reminders = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT id, appointment_id, reminder_minutes, sent FROM reminders WHERE appointment_id = ?")) {
            preparedStatement.setLong(1, appointmentId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Reminder reminder = new Reminder();
                    reminder.setId(resultSet.getLong("id"));
                    reminder.setAppointmentId(resultSet.getLong("appointment_id"));
                    reminder.setReminderMinutes(resultSet.getInt("reminder_minutes"));
                    reminder.setSent(resultSet.getBoolean("sent"));
                    reminders.add(reminder);
                }
            }
        }
        return reminders;
    }

    public List<ReminderView> getAllReminders() throws SQLException {
        List<ReminderView> list = new ArrayList<>();
        String sql = "SELECT r.id, r.appointment_id, r.reminder_minutes, r.sent, a.title, "
                + "DATE_SUB(a.start_time, INTERVAL r.reminder_minutes MINUTE) AS reminder_time "
                + "FROM reminders r JOIN appointments a ON r.appointment_id = a.id "
                + "ORDER BY reminder_time";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ReminderView rv = new ReminderView();
                rv.setId(rs.getLong("id"));
                rv.setAppointmentId(rs.getLong("appointment_id"));
                rv.setReminderMinutes(rs.getInt("reminder_minutes"));
                rv.setSent(rs.getBoolean("sent"));
                rv.setAppointmentTitle(rs.getString("title"));
                var ts = rs.getTimestamp("reminder_time");
                if (ts != null) {
                    rv.setReminderTime(ts.toLocalDateTime());
                }
                list.add(rv);
            }
        }
        return list;
    }

    // Simple view holder used by DAO
    public static class ReminderView {
        private Long id;
        private Long appointmentId;
        private Integer reminderMinutes;
        private Boolean sent;
        private String appointmentTitle;
        private LocalDateTime reminderTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
        public Integer getReminderMinutes() { return reminderMinutes; }
        public void setReminderMinutes(Integer reminderMinutes) { this.reminderMinutes = reminderMinutes; }
        public Boolean getSent() { return sent; }
        public void setSent(Boolean sent) { this.sent = sent; }
        public String getAppointmentTitle() { return appointmentTitle; }
        public void setAppointmentTitle(String appointmentTitle) { this.appointmentTitle = appointmentTitle; }
        public LocalDateTime getReminderTime() { return reminderTime; }
        public void setReminderTime(LocalDateTime reminderTime) { this.reminderTime = reminderTime; }
    }
}

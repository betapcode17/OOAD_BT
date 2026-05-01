package com.calendar.model.dao;

import com.calendar.model.bean.Reminder;
import com.calendar.model.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
}

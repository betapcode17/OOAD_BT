package com.calendar.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.calendar.model.bean.Appointment;
import com.calendar.model.db.DBConnection;

public class AppointmentDAO {
    public Appointment save(Appointment appointment, List<Long> participantIds) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long appointmentId = insertAppointment(connection, appointment);
                for (Long participantId : participantIds) {
                    insertParticipant(connection, appointmentId, participantId);
                }
                connection.commit();
                appointment.setId(appointmentId);
                return appointment;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Appointment> findAll() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT id, calendar_id, title, location, start_time, end_time, meeting_type FROM appointments ORDER BY start_time";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                appointments.add(mapAppointment(resultSet));
            }
        }
        return appointments;
    }

    public List<Appointment> findConflictingAppointments(LocalDateTime startTime, LocalDateTime endTime, List<Long> participantIds) throws SQLException {
        List<Appointment> conflicts = new ArrayList<>();
        if (participantIds == null || participantIds.isEmpty()) {
            return conflicts;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT a.id, a.calendar_id, a.title, a.location, a.start_time, a.end_time, a.meeting_type ");
        sql.append("FROM appointments a ");
        sql.append("JOIN appointment_participants ap ON a.id = ap.appointment_id ");
        sql.append("WHERE ap.user_id IN (");
        for (int index = 0; index < participantIds.size(); index++) {
            if (index > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(") AND a.start_time < ? AND a.end_time > ? ORDER BY a.start_time");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            for (Long participantId : participantIds) {
                preparedStatement.setLong(parameterIndex++, participantId);
            }
            preparedStatement.setTimestamp(parameterIndex++, java.sql.Timestamp.valueOf(endTime));
            preparedStatement.setTimestamp(parameterIndex, java.sql.Timestamp.valueOf(startTime));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    conflicts.add(mapAppointment(resultSet));
                }
            }
        }
        return conflicts;
    }

    public Appointment findMatchingGroupMeeting(String title, long durationMinutes) throws SQLException {
        String sql = "SELECT id, calendar_id, title, location, start_time, end_time, meeting_type "
                + "FROM appointments WHERE meeting_type = 'GROUP' AND title = ? "
                + "AND TIMESTAMPDIFF(MINUTE, start_time, end_time) = ? LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setLong(2, durationMinutes);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAppointment(rs);
                }
            }
        }
        return null;
    }

    private long insertAppointment(Connection connection, Appointment appointment) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO appointments(calendar_id, title, location, start_time, end_time, meeting_type) VALUES(?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            if (appointment.getCalendarId() == null) {
                preparedStatement.setNull(1, java.sql.Types.BIGINT);
            } else {
                preparedStatement.setLong(1, appointment.getCalendarId());
            }
            preparedStatement.setString(2, appointment.getTitle());
            preparedStatement.setString(3, appointment.getLocation());
            preparedStatement.setTimestamp(4, java.sql.Timestamp.valueOf(appointment.getStartTime()));
            preparedStatement.setTimestamp(5, java.sql.Timestamp.valueOf(appointment.getEndTime()));
            preparedStatement.setString(6, appointment.getMeetingType());
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }
        throw new SQLException("Unable to save appointment.");
    }

    private void insertParticipant(Connection connection, long appointmentId, long userId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO appointment_participants(appointment_id, user_id) VALUES(?, ?)")) {
            preparedStatement.setLong(1, appointmentId);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
    }

    private Appointment mapAppointment(ResultSet resultSet) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(resultSet.getLong("id"));
        long calendarId = resultSet.getLong("calendar_id");
        if (!resultSet.wasNull()) {
            appointment.setCalendarId(calendarId);
        }
        appointment.setTitle(resultSet.getString("title"));
        appointment.setLocation(resultSet.getString("location"));
        appointment.setStartTime(resultSet.getTimestamp("start_time").toLocalDateTime());
        appointment.setEndTime(resultSet.getTimestamp("end_time").toLocalDateTime());
        appointment.setMeetingType(resultSet.getString("meeting_type"));
        return appointment;
    }
}

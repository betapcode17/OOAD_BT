package com.calendar.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.calendar.model.db.DBConnection;

public class GroupParticipantDAO {

    public void insert(Long appointmentId, String participantName) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO group_participants(appointment_id, participant_name) VALUES(?, ?)")) {
            ps.setLong(1, appointmentId);
            ps.setString(2, participantName);
            ps.executeUpdate();
        }
    }

    public int countParticipants(Long appointmentId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM ("
                + "SELECT u.name AS participant_name "
                + "FROM appointment_participants ap "
                + "JOIN users u ON ap.user_id = u.id "
                + "WHERE ap.appointment_id = ? "
                + "UNION "
                + "SELECT gp.participant_name "
                + "FROM group_participants gp "
                + "WHERE gp.appointment_id = ?"
                + ") t";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, appointmentId);
            ps.setLong(2, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    public List<String> findParticipantNames(Long appointmentId) throws SQLException {
        List<String> names = new ArrayList<>();
        String sql = "SELECT participant_name FROM ("
                + "SELECT u.name AS participant_name "
                + "FROM appointment_participants ap "
                + "JOIN users u ON ap.user_id = u.id "
                + "WHERE ap.appointment_id = ? "
                + "UNION "
                + "SELECT gp.participant_name "
                + "FROM group_participants gp "
                + "WHERE gp.appointment_id = ?"
                + ") t ORDER BY participant_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, appointmentId);
            ps.setLong(2, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("participant_name"));
                }
            }
        }
        return names;
    }
}

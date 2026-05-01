package com.calendar.model.db;

import com.calendar.model.bean.Appointment;
import com.calendar.model.bean.Reminder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DBConnection {
    /*
     * Basic SQL structure:
     * users(id, name, email)
     * calendars(id, user_id, name)
     * appointments(id, calendar_id, title, location, start_time, end_time, meeting_type)
     * appointment_participants(appointment_id, user_id)
     * reminders(id, appointment_id, reminder_minutes, sent)
     */
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream inputStream = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("db.properties was not found on the classpath.");
            }
            PROPERTIES.load(inputStream);
            Class.forName(PROPERTIES.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(
                PROPERTIES.getProperty("db.url"),
                PROPERTIES.getProperty("db.username"),
                PROPERTIES.getProperty("db.password")
        );
        initializeSchema(connection);
        return connection;
    }

    private static void initializeSchema(Connection connection) throws SQLException {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            // Only reset schema when explicitly requested via db.reset=true in db.properties
            String reset = PROPERTIES.getProperty("db.reset", "false");
            if ("true".equalsIgnoreCase(reset)) {
                resetSchema(statement);
            }

            statement.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(120) NOT NULL, "
                    + "email VARCHAR(180) NOT NULL UNIQUE"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS calendars ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id BIGINT NOT NULL, "
                    + "name VARCHAR(120) NOT NULL, "
                    + "CONSTRAINT fk_calendar_user FOREIGN KEY (user_id) REFERENCES users(id)"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS appointments ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "calendar_id BIGINT, "
                    + "title VARCHAR(180) NOT NULL, "
                    + "location VARCHAR(180), "
                    + "start_time TIMESTAMP NOT NULL, "
                    + "end_time TIMESTAMP NOT NULL, "
                    + "meeting_type VARCHAR(20) NOT NULL, "
                    + "CONSTRAINT fk_appointment_calendar FOREIGN KEY (calendar_id) REFERENCES calendars(id)"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS appointment_participants ("
                    + "appointment_id BIGINT NOT NULL, "
                    + "user_id BIGINT NOT NULL, "
                    + "PRIMARY KEY (appointment_id, user_id), "
                    + "CONSTRAINT fk_participant_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE, "
                    + "CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users(id)"
                    + ")");
            statement.execute("CREATE TABLE IF NOT EXISTS reminders ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "appointment_id BIGINT NOT NULL, "
                    + "reminder_minutes INT NOT NULL, "
                    + "sent BOOLEAN DEFAULT FALSE, "
                    + "CONSTRAINT fk_reminder_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE"
                    + ")");
        }

        seedUsers(connection);
        seedCalendars(connection);
        seedSampleAppointment(connection);
    }

    private static void resetSchema(Statement statement) throws SQLException {
        statement.execute("SET FOREIGN_KEY_CHECKS=0");
        statement.execute("DROP TABLE IF EXISTS reminders");
        statement.execute("DROP TABLE IF EXISTS appointment_participants");
        statement.execute("DROP TABLE IF EXISTS appointments");
        statement.execute("DROP TABLE IF EXISTS calendars");
        statement.execute("DROP TABLE IF EXISTS users");
        statement.execute("SET FOREIGN_KEY_CHECKS=1");
    }

    private static void seedUsers(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users")) {
            if (resultSet.next() && resultSet.getInt(1) == 0) {
                insertUser(connection, "Alice Johnson", "alice@example.com");
                insertUser(connection, "Bob Smith", "bob@example.com");
                insertUser(connection, "Charlie Lee", "charlie@example.com");
            }
        }
    }

    private static void seedCalendars(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM calendars")) {
            if (resultSet.next() && resultSet.getInt(1) == 0) {
                insertCalendar(connection, 1L, "Alice Calendar");
                insertCalendar(connection, 2L, "Bob Calendar");
                insertCalendar(connection, 3L, "Charlie Calendar");
            }
        }
    }

    private static void seedSampleAppointment(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM appointments")) {
            if (resultSet.next() && resultSet.getInt(1) == 0) {
                Appointment appointment = new Appointment();
                appointment.setCalendarId(1L);
                appointment.setTitle("Team Standup");
                appointment.setLocation("Meeting Room A");
                appointment.setStartTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0));
                appointment.setEndTime(appointment.getStartTime().plusMinutes(30));
                appointment.setMeetingType("PERSONAL");

                long appointmentId = insertAppointment(connection, appointment);
                insertParticipant(connection, appointmentId, 1L);

                Reminder reminder = new Reminder();
                reminder.setAppointmentId(appointmentId);
                reminder.setReminderMinutes(15);
                reminder.setSent(Boolean.FALSE);
                insertReminder(connection, reminder);
            }
        }
    }

    private static void insertUser(Connection connection, String name, String email) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO users(name, email) VALUES(?, ?)")) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();
        }
    }

    private static void insertCalendar(Connection connection, Long userId, String name) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO calendars(user_id, name) VALUES(?, ?)")) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, name);
            preparedStatement.executeUpdate();
        }
    }

    private static long insertAppointment(Connection connection, Appointment appointment) throws SQLException {
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
        throw new SQLException("Failed to create the appointment record.");
    }

    private static void insertParticipant(Connection connection, long appointmentId, long userId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO appointment_participants(appointment_id, user_id) VALUES(?, ?)")) {
            preparedStatement.setLong(1, appointmentId);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        }
    }

    private static void insertReminder(Connection connection, Reminder reminder) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO reminders(appointment_id, reminder_minutes, sent) VALUES(?, ?, ?)")) {
            preparedStatement.setLong(1, reminder.getAppointmentId());
            preparedStatement.setInt(2, reminder.getReminderMinutes());
            preparedStatement.setBoolean(3, Boolean.TRUE.equals(reminder.getSent()));
            preparedStatement.executeUpdate();
        }
    }
}

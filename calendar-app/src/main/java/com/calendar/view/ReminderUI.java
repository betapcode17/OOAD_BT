package com.calendar.view;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.calendar.model.dao.ReminderDAO;
import com.calendar.model.dao.ReminderDAO.ReminderView;

public class ReminderUI extends JFrame {
    private final JTable table;
    private final DefaultTableModel model;
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReminderUI() {
        super("Reminders");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"Appointment", "Reminder Time", "Minutes Before"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadReminders();
    }

    private void loadReminders() {
        try {
            ReminderDAO dao = new ReminderDAO();
            List<ReminderView> list = dao.getAllReminders();
            model.setRowCount(0);
            for (ReminderView rv : list) {
                model.addRow(new Object[]{rv.getAppointmentTitle(), rv.getReminderTime().format(TF), rv.getReminderMinutes()});
            }
        } catch (SQLException ex) {
            model.setRowCount(0);
            model.addRow(new Object[]{"Error", ex.getMessage(), ""});
        }
    }

    public static void showDialog() {
        SwingUtilities.invokeLater(() -> {
            ReminderUI ui = new ReminderUI();
            ui.setVisible(true);
        });
    }
}

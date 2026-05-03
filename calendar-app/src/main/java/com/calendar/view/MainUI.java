package com.calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.calendar.controller.AddAppointmentController;
import com.calendar.model.bean.Appointment;
import com.calendar.model.bo.AppointmentBO;
import com.calendar.model.dao.GroupParticipantDAO;
import com.calendar.model.dao.ReminderDAO;
import com.calendar.model.dao.ReminderDAO.ReminderView;
import com.calendar.view.component.AppointmentCardRenderer;
import com.calendar.view.component.MiniCalendar;

public class MainUI extends JFrame {
    private final AppointmentBO appointmentBO;
    private final AddAppointmentController addAppointmentController;
    private final DefaultListModel<Appointment> appointmentListModel;
    private final JList<Appointment> appointmentList;
    private final MiniCalendar miniCalendar;
    private java.time.LocalDate selectedDate = null;
    private JLabel statusLabel;
    private final DefaultTableModel reminderTableModel;
    private final JTable reminderTable;
    private final DefaultTableModel groupMeetingTableModel;
    private final JTable groupMeetingTable;
    private final List<Appointment> groupMeetingRows;
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MainUI() {
        this.appointmentBO = new AppointmentBO();
        this.addAppointmentController = new AddAppointmentController(this, appointmentBO);
        this.appointmentListModel = new DefaultListModel<>();
        this.appointmentList = new JList<>(appointmentListModel);
        this.miniCalendar = new MiniCalendar();
        this.reminderTableModel = new DefaultTableModel(new Object[]{"Appointment", "Reminder Time", "Reminder Message"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.reminderTable = new JTable(reminderTableModel);
        this.groupMeetingTableModel = new DefaultTableModel(new Object[]{"Meeting Title", "Start", "End", "Location"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.groupMeetingTable = new JTable(groupMeetingTableModel);
        this.groupMeetingRows = new ArrayList<>();

        // Listen for date clicks on mini calendar — only select date, do NOT open dialog
        miniCalendar.setDateClickListener(date -> onDateSelected(date));

        initializeUI();
        loadAllData();
    }

    private void initializeUI() {
        setTitle("Calendar App - Modern UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ====================== TOP BAR ======================
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        // ====================== MAIN CONTENT ======================
        JPanel mainContent = new JPanel(new BorderLayout(16, 0));
        mainContent.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        mainContent.setBackground(new Color(248, 249, 250));

        // ===== SIDEBAR (Mini Calendar + Dark Mode) =====
        JPanel sidebar = createSidebar();
        mainContent.add(sidebar, BorderLayout.WEST);

        // ===== CENTER (Tabbed Views) =====
        JPanel centerPanel = createCenterPanel();
        mainContent.add(centerPanel, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        // ====================== BOTTOM BAR ======================
        JPanel bottomBar = createBottomBar();
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(41, 128, 185));
        topBar.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("📅 Calendar App");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Manage your schedule with ease");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(220, 230, 245));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        topBar.add(titlePanel, BorderLayout.CENTER);

        return topBar;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(8, 16));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(new Color(240, 243, 248));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 210, 220)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        // Mini Calendar
        JPanel calendarBox = new JPanel(new BorderLayout());
        calendarBox.setBackground(Color.WHITE);
        calendarBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        calendarBox.add(miniCalendar, BorderLayout.CENTER);

        sidebar.add(calendarBox, BorderLayout.CENTER);

        return sidebar;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        // Header
        JLabel header = new JLabel("Schedule");
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setForeground(new Color(40, 50, 60));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(header, BorderLayout.WEST);

        centerPanel.add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabbedPane.addTab("View Appointments", createAppointmentsTab());
        tabbedPane.addTab("View Reminders", createRemindersTab());
        tabbedPane.addTab("View Group Meetings", createGroupMeetingsTab());

        centerPanel.add(tabbedPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel createAppointmentsTab() {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setOpaque(false);

        // Appointment List with Custom Renderer
        appointmentList.setCellRenderer(new AppointmentCardRenderer());
        appointmentList.setFixedCellHeight(100);
        appointmentList.setBackground(new Color(248, 249, 250));
        appointmentList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Hover effect on mouse over
        appointmentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = appointmentList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    appointmentList.setSelectedIndex(index);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(appointmentList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.setBackground(new Color(248, 249, 250));

        tab.add(scrollPane, BorderLayout.CENTER);

        return tab;
        }

        private JPanel createRemindersTab() {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setOpaque(false);

        reminderTable.setRowHeight(32);
        JScrollPane scrollPane = new JScrollPane(reminderTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 235), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        tab.add(scrollPane, BorderLayout.CENTER);
        return tab;
        }

        private JPanel createGroupMeetingsTab() {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setOpaque(false);

        groupMeetingTable.setRowHeight(32);
            groupMeetingTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = groupMeetingTable.rowAtPoint(e.getPoint());
                        if (row >= 0) {
                            showGroupMeetingDetail(row);
                        }
                    }
                }
            });
        JScrollPane scrollPane = new JScrollPane(groupMeetingTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 235), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        tab.add(scrollPane, BorderLayout.CENTER);
        return tab;
    }

        private void showGroupMeetingDetail(int row) {
            if (row < 0 || row >= groupMeetingRows.size()) {
                return;
            }

            Appointment meeting = groupMeetingRows.get(row);
            try {
                GroupParticipantDAO groupParticipantDAO = new GroupParticipantDAO();
                int participantCount = groupParticipantDAO.countParticipants(meeting.getId());
                List<String> participantNames = groupParticipantDAO.findParticipantNames(meeting.getId());
                String names = participantNames.isEmpty() ? "No participants" : String.join(", ", participantNames);

                String message = "Meeting Title: " + meeting.getTitle() + System.lineSeparator()
                        + "Location: " + (meeting.getLocation() == null ? "" : meeting.getLocation()) + System.lineSeparator()
                        + "Start: " + (meeting.getStartTime() == null ? "" : meeting.getStartTime().format(DT_FORMATTER)) + System.lineSeparator()
                        + "End: " + (meeting.getEndTime() == null ? "" : meeting.getEndTime().format(DT_FORMATTER)) + System.lineSeparator()
                        + "Number of participants: " + participantCount + System.lineSeparator()
                        + "Participants: " + names;

                JOptionPane.showMessageDialog(this, message, "Group Meeting Details", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(this,
                        "Unable to load group meeting details: " + exception.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    private JPanel createBottomBar() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(245, 247, 250));
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 220)));
        bottomBar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Button Panel
        JButton addBtn = new JButton("➕ Add Appointment");
        JButton refreshBtn = new JButton("🔄 Refresh");

        styleButton(addBtn, new Color(46, 204, 113));
        styleButton(refreshBtn, new Color(52, 152, 219));

        addBtn.addActionListener(e -> openAddAppointmentDialog());
        refreshBtn.addActionListener(e -> refreshAppointments());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);

        bottomBar.add(buttonPanel, BorderLayout.EAST);

        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 120, 140));
        bottomBar.add(statusLabel, BorderLayout.WEST);

        return bottomBar;
    }

    private void styleButton(JButton button, Color color) {
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
    }

    private void openAddAppointmentDialog() {
        AddAppointmentUI addAppointmentUI = new AddAppointmentUI(this, addAppointmentController);
        if (selectedDate != null) {
            java.time.LocalDate date = selectedDate;
            java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(date, java.time.LocalTime.of(9, 0));
            addAppointmentUI.setStartDateTime(startDateTime);
            addAppointmentUI.lockDateField();
            // keep the calendar selection visible instead of clearing it
            statusLabel.setText("Selected: " + date.toString());
        }
        addAppointmentUI.setVisible(true);
    }

    private void onDateSelected(LocalDate date) {
        this.selectedDate = date;
        miniCalendar.setSelectedDate(date);
        statusLabel.setText("Selected: " + date.toString());
    }

    public void refreshAppointments() {
        loadAllData();
    }

    private void loadAllData() {
        loadAppointments();
        loadReminders();
        loadGroupMeetings();
    }

    private void loadAppointments() {
        try {
            List<Appointment> appointments = appointmentBO.getAllAppointments();
            appointmentListModel.clear();
            for (Appointment appointment : appointments) {
                appointmentListModel.addElement(appointment);
            }
            if (appointments.isEmpty()) {
                // Show empty state message
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load appointments: " + exception.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadReminders() {
        try {
            ReminderDAO reminderDAO = new ReminderDAO();
            List<ReminderView> reminders = reminderDAO.getAllReminders();
            reminderTableModel.setRowCount(0);
            for (ReminderView reminder : reminders) {
                String reminderTime = reminder.getReminderTime() == null ? "" : reminder.getReminderTime().format(DT_FORMATTER);
                String message = "Remind " + reminder.getReminderMinutes() + " minutes before";
                reminderTableModel.addRow(new Object[]{
                        reminder.getAppointmentTitle(),
                        reminderTime,
                        message
                });
            }
        } catch (SQLException exception) {
            reminderTableModel.setRowCount(0);
            reminderTableModel.addRow(new Object[]{"Error", "", exception.getMessage()});
        }
    }

    private void loadGroupMeetings() {
        try {
            List<Appointment> appointments = appointmentBO.getAllAppointments();
            groupMeetingTableModel.setRowCount(0);
            groupMeetingRows.clear();
            for (Appointment appointment : appointments) {
                if (!"GROUP".equalsIgnoreCase(appointment.getMeetingType())) {
                    continue;
                }
                groupMeetingRows.add(appointment);
                groupMeetingTableModel.addRow(new Object[]{
                        appointment.getTitle(),
                        appointment.getStartTime() == null ? "" : appointment.getStartTime().format(DT_FORMATTER),
                        appointment.getEndTime() == null ? "" : appointment.getEndTime().format(DT_FORMATTER),
                        appointment.getLocation()
                });
            }
        } catch (SQLException exception) {
            groupMeetingTableModel.setRowCount(0);
            groupMeetingRows.clear();
            groupMeetingTableModel.addRow(new Object[]{"Error", "", "", exception.getMessage()});
        }
    }
}
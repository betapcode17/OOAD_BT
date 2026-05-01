package com.calendar.view;

import com.calendar.controller.AddAppointmentController;
import com.calendar.model.dto.AppointmentDTO;
import com.calendar.view.component.DateTimePickerField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;

public class AddAppointmentUI extends JDialog {
    private final AddAppointmentController controller;
    private final JTextField titleField;
    private final JTextField locationField;
    private final DateTimePickerField startTimeField;
    private final DateTimePickerField endTimeField;
    private final JSpinner reminderSpinner;
    private final JTextField participantIdsField;
    private final JButton saveButton;
    private boolean isFormValid = false;
    private boolean titleTouched = false;
    private boolean startTouched = false;
    private boolean endTouched = false;

    public AddAppointmentUI(java.awt.Frame owner, AddAppointmentController controller) {
        super(owner, "Create Appointment", true);
        this.controller = controller;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusMinutes(30);

        this.titleField = new JTextField();
        this.locationField = new JTextField();
        this.startTimeField = new DateTimePickerField(now);
        this.endTimeField = new DateTimePickerField(later);
        this.reminderSpinner = new JSpinner(new SpinnerNumberModel(15, 0, 1440, 5));
        this.participantIdsField = new JTextField("1");
        this.saveButton = new JButton("💾 Save");

        initializeUi();
        setupValidation();
    }

    private void initializeUi() {
        setLayout(new BorderLayout());
        setSize(600, 650);
        setLocationRelativeTo(getOwner());

        // ====================== HEADER ======================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = new JLabel("Create Appointment");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // ====================== FORM CARD ======================
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 249, 250));

        JPanel formCard = createFormCard();
        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(null);
        formScroll.setBackground(new Color(248, 249, 250));
        mainPanel.add(formScroll, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // ====================== BUTTON BAR ======================
        JPanel buttonBar = createButtonBar();
        add(buttonBar, BorderLayout.SOUTH);
    }

    private JPanel createFormCard() {
        JPanel card = new JPanel();
        card.setLayout(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 235)),
                new EmptyBorder(24, 24, 24, 24)
        ));

        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(12, 0, 6, 0);
        labelGbc.gridx = 0;

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1.0;
        fieldGbc.insets = new Insets(0, 0, 12, 0);
        fieldGbc.gridx = 0;

        int row = 0;

        // Title
        row = addFormRow(card, labelGbc, fieldGbc, row, "Title *", createStyledField(titleField, 300, 40));

        // Location
        row = addFormRow(card, labelGbc, fieldGbc, row, "Location", createStyledField(locationField, 300, 40));

        // Start Time
        row = addFormRow(card, labelGbc, fieldGbc, row, "Start DateTime *", startTimeField);

        // End Time
        row = addFormRow(card, labelGbc, fieldGbc, row, "End DateTime *", endTimeField);

        // Reminder
        JPanel reminderPanel = new JPanel();
        reminderPanel.setOpaque(false);
        reminderPanel.setLayout(new BorderLayout(8, 0));
        reminderSpinner.setPreferredSize(new Dimension(80, 36));
        reminderPanel.add(reminderSpinner, BorderLayout.WEST);
        reminderPanel.add(new JLabel("minutes before"), BorderLayout.CENTER);
        row = addFormRow(card, labelGbc, fieldGbc, row, "Reminder", reminderPanel);

        // Participant IDs
        row = addFormRow(card, labelGbc, fieldGbc, row, "Participant IDs", createStyledField(participantIdsField, 300, 40));

        // Fill remaining space
        GridBagConstraints spacer = new GridBagConstraints();
        spacer.weighty = 1.0;
        spacer.gridy = row;
        card.add(new JPanel(), spacer);

        return card;
    }

    private int addFormRow(JPanel panel, GridBagConstraints labelGbc, GridBagConstraints fieldGbc, int row, String labelText, javax.swing.JComponent field) {
        GridBagConstraints lbc = (GridBagConstraints) labelGbc.clone();
        lbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(new Color(40, 50, 60));
        panel.add(label, lbc);

        GridBagConstraints fbc = (GridBagConstraints) fieldGbc.clone();
        fbc.gridy = row + 1;
        panel.add(field, fbc);

        return row + 2;
    }

    private JTextField createStyledField(JTextField field, int width, int height) {
        field.setPreferredSize(new Dimension(width, height));
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(new LineBorder(new Color(220, 225, 235), 1, false));
        field.setMargin(new Insets(6, 12, 6, 12));
        return field;
    }

    private JPanel createButtonBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(245, 247, 250));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 220)));
        bar.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel rightButtons = new JPanel();
        rightButtons.setOpaque(false);

        styleButton(saveButton, new Color(46, 204, 113)); // Green
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveButton.addActionListener(e -> {
            try {
                controller.handleSave(this);
                // Do NOT close the form automatically. Keep it open and cleared on success.
            } catch (Exception ex) {
                showError("Error saving appointment: " + ex.getMessage());
            }
        });
        saveButton.setEnabled(false);

        JButton closeButton = new JButton("✕ Close");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        styleButton(closeButton, new Color(189, 195, 199)); // Gray
        closeButton.addActionListener(e -> dispose());

        rightButtons.add(saveButton);
        rightButtons.add(closeButton);

        bar.add(rightButtons, BorderLayout.EAST);

        return bar;
    }

    private void styleButton(JButton button, Color color) {
        button.setFocusPainted(false);
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

    private void setupValidation() {
        DocumentListener validationListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateForm(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateForm(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateForm(); }
        };

        titleField.getDocument().addDocumentListener(validationListener);
        locationField.getDocument().addDocumentListener(validationListener);
        participantIdsField.getDocument().addDocumentListener(validationListener);

        // Mark touched on focus lost (so fields don't show red just because user clicked into them)
        titleField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                titleTouched = true;
                validateForm();
            }
        });

        // participant field is optional; do not mark as touched on focus to avoid premature red highlight

        // Listen for date/time changes from the picker to mark as touched
        startTimeField.addPropertyChangeListener("dateTime", evt -> {
            startTouched = true;
            validateForm();
        });
        endTimeField.addPropertyChangeListener("dateTime", evt -> {
            endTouched = true;
            validateForm();
        });

        reminderSpinner.addChangeListener(e -> validateForm());

        // Validate on initial load (no visual errors until fields are touched)
        SwingUtilities.invokeLater(this::validateForm);
    }

    private void validateForm() {
        boolean titleValid = !titleField.getText().trim().isEmpty();
        boolean startValid = startTimeField.getDateTime() != null;
        boolean endValid = endTimeField.getDateTime() != null;
        boolean timeOrderValid = false;

        if (startValid && endValid) {
            timeOrderValid = startTimeField.getDateTime().isBefore(endTimeField.getDateTime());
        }

        int reminder = (Integer) reminderSpinner.getValue();
        boolean reminderValid = reminder >= 0;

        // Visual feedback only for touched fields
        titleField.setBackground((titleTouched && !titleValid) ? new Color(255, 200, 200) : Color.WHITE);
        startTimeField.setInvalid(startTouched && !startValid);
        endTimeField.setInvalid(endTouched && (!endValid || !timeOrderValid));

        isFormValid = titleValid && startValid && endValid && timeOrderValid && reminderValid;
        saveButton.setEnabled(isFormValid);

        if (!timeOrderValid && endValid) {
            endTimeField.setInvalid(true);
        }
    }

    public AppointmentDTO getAppointmentDTO() {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setTitle(titleField.getText());
        dto.setLocation(locationField.getText());
        dto.setStartTime(startTimeField.getFormattedDateTime());
        dto.setEndTime(endTimeField.getFormattedDateTime());
        dto.setReminderMinutes(String.valueOf(reminderSpinner.getValue()));
        dto.setParticipantIds(participantIdsField.getText());
        return dto;
    }

    public void clearForm() {
        titleField.setText("");
        locationField.setText("");
        LocalDateTime now = LocalDateTime.now();
        startTimeField.setDateTime(now);
        endTimeField.setDateTime(now.plusMinutes(30));
        reminderSpinner.setValue(15);
        participantIdsField.setText("1");
        // Reset touched state so validation highlights do not show immediately
        titleTouched = false;
        startTouched = false;
        endTouched = false;
        validateForm();
    }

    public void showError(String text) {
        JOptionPane.showMessageDialog(this, text, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}

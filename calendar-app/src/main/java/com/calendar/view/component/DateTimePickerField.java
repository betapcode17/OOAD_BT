package com.calendar.view.component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class DateTimePickerField extends JPanel {
    private final JLabel displayLabel;
    private LocalDateTime selectedDateTime;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DateTimePickerField(LocalDateTime initialDateTime) {
        this.selectedDateTime = initialDateTime;
        setLayout(new BorderLayout(8, 0));
        setPreferredSize(new Dimension(300, 40));

        // Display label
        displayLabel = new JLabel();
        displayLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        displayLabel.setOpaque(true);
        displayLabel.setBackground(Color.WHITE);
        displayLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        updateDisplay();

        // Picker button
        JButton pickerButton = new JButton("📅");
        pickerButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        pickerButton.setPreferredSize(new Dimension(40, 40));
        pickerButton.setFocusPainted(false);
        pickerButton.setBackground(new Color(52, 152, 219));
        pickerButton.setForeground(Color.WHITE);
        pickerButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pickerButton.addActionListener(e -> openDateTimePicker());

        add(displayLabel, BorderLayout.CENTER);
        add(pickerButton, BorderLayout.EAST);

        setBorder(new LineBorder(new Color(220, 225, 235), 1, false));
    }

    private void openDateTimePicker() {
        JDialog pickerDialog = new JDialog((java.awt.Frame) null, "Select Date & Time", true);
        pickerDialog.setLayout(new BorderLayout(12, 12));
        pickerDialog.setSize(500, 380);
        pickerDialog.setLocationRelativeTo(null);

        // Calendar panel
        JPanel calendarPanel = new JPanel(new GridLayout(7, 7, 6, 6));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        calendarPanel.setBackground(Color.WHITE);

        Calendar cal = Calendar.getInstance();
        cal.setTime(java.sql.Timestamp.valueOf(selectedDateTime));

        // Populate calendar
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;

        // Headers
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayName : dayNames) {
            JLabel dayLabel = new JLabel(dayName);
            dayLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            dayLabel.setHorizontalAlignment(JLabel.CENTER);
            dayLabel.setForeground(new Color(100, 120, 140));
            calendarPanel.add(dayLabel);
        }

        // Empty cells
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarPanel.add(new JLabel());
        }

        // Days
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int selectedDay = selectedDateTime.getDayOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
            dayButton.setFocusPainted(false);
            dayButton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            dayButton.setPreferredSize(new Dimension(60, 50));

            if (day == selectedDay && month == cal.get(Calendar.MONTH) && year == cal.get(Calendar.YEAR)) {
                dayButton.setBackground(new Color(52, 152, 219));
                dayButton.setForeground(Color.WHITE);
                dayButton.setOpaque(true);
            } else {
                dayButton.setBackground(Color.WHITE);
                dayButton.setForeground(new Color(40, 50, 60));
            }

            final int currentDay = day;
            dayButton.addActionListener(e -> {
                selectedDateTime = LocalDateTime.of(year, month + 1, currentDay, selectedDateTime.getHour(), selectedDateTime.getMinute());
                // Update highlights for all day buttons
                for (java.awt.Component comp : calendarPanel.getComponents()) {
                    if (comp instanceof javax.swing.JButton b) {
                        try {
                            int d = Integer.parseInt(b.getText());
                            if (d == currentDay) {
                                b.setBackground(new Color(52, 152, 219));
                                b.setForeground(Color.WHITE);
                                b.setOpaque(true);
                            } else {
                                b.setBackground(Color.WHITE);
                                b.setForeground(new Color(40, 50, 60));
                                b.setOpaque(true);
                            }
                        } catch (NumberFormatException ex) {
                            // not a day button (header/empty), ignore
                        }
                    }
                }
                updateDisplay();
            });

            calendarPanel.add(dayButton);
        }

        // Time panel
        JPanel timePanel = new JPanel();
            timePanel.setBackground(new Color(245, 247, 250));
        timePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel timeLabel = new JLabel("Time:");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        timePanel.add(timeLabel);

        // Hour spinner
        SpinnerNumberModel hourModel = new SpinnerNumberModel(selectedDateTime.getHour(), 0, 23, 1);
        JSpinner hourSpinner = new JSpinner(hourModel);
        hourSpinner.setPreferredSize(new Dimension(50, 30));
    hourSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Minute spinner
        SpinnerNumberModel minuteModel = new SpinnerNumberModel(selectedDateTime.getMinute(), 0, 59, 5);
        JSpinner minuteSpinner = new JSpinner(minuteModel);
        minuteSpinner.setPreferredSize(new Dimension(50, 30));
    minuteSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));

        timePanel.add(hourSpinner);
        timePanel.add(new JLabel("  :  "));
        timePanel.add(minuteSpinner);

        // Buttons
        JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(new Color(245, 247, 250));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JButton okButton = new JButton("OK");
        okButton.setBackground(new Color(46, 204, 113));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
            okButton.setFont(new Font("SansSerif", Font.BOLD, 12));
            okButton.setPreferredSize(new Dimension(100, 36));
        okButton.addActionListener(e -> {
            int hour = (Integer) hourSpinner.getValue();
            int minute = (Integer) minuteSpinner.getValue();
            selectedDateTime = LocalDateTime.of(
                    selectedDateTime.getYear(),
                    selectedDateTime.getMonth(),
                    selectedDateTime.getDayOfMonth(),
                    hour,
                    minute
            );
            updateDisplay();
            pickerDialog.dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusPainted(false);
            cancelButton.setFont(new Font("SansSerif", Font.BOLD, 12));
            cancelButton.setPreferredSize(new Dimension(100, 36));
        cancelButton.addActionListener(e -> pickerDialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        pickerDialog.add(calendarPanel, BorderLayout.CENTER);
        pickerDialog.add(timePanel, BorderLayout.NORTH);
            pickerDialog.add(buttonPanel, BorderLayout.SOUTH);
        pickerDialog.add(buttonPanel, BorderLayout.SOUTH);
        pickerDialog.setVisible(true);
    }

    private void updateDisplay() {
        displayLabel.setText(selectedDateTime.format(formatter));
        // Notify listeners that the date/time changed
        try {
            firePropertyChange("dateTime", null, selectedDateTime);
        } catch (Exception ignored) {
        }
    }

    public LocalDateTime getDateTime() {
        return selectedDateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.selectedDateTime = dateTime;
        updateDisplay();
    }

    public String getFormattedDateTime() {
        return selectedDateTime.format(formatter);
    }

    public void setInvalid(boolean invalid) {
        if (invalid) {
            setBorder(new LineBorder(new Color(244, 67, 54), 2, false));
        } else {
            setBorder(new LineBorder(new Color(220, 225, 235), 1, false));
        }
    }
}

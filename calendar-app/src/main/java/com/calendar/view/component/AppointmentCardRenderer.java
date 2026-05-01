package com.calendar.view.component;

import com.calendar.model.bean.Appointment;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;

public class AppointmentCardRenderer extends JPanel implements ListCellRenderer<Appointment> {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final JLabel titleLabel;
    private final JLabel timeLabel;
    private final JLabel locationLabel;

    public AppointmentCardRenderer() {
        setOpaque(true);
        setLayout(new BorderLayout(12, 6));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Title
        titleLabel = new JLabel();
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

        // Time & Location
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        timeLabel.setForeground(new Color(100, 120, 140));

        locationLabel = new JLabel();
        locationLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        locationLabel.setForeground(new Color(140, 150, 160));

        infoPanel.add(timeLabel, BorderLayout.WEST);
        infoPanel.add(locationLabel, BorderLayout.EAST);

        add(titleLabel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Appointment> list,
            Appointment value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (value != null) {
            titleLabel.setText("📌 " + value.getTitle());

            String startTime = value.getStartTime().format(TIME_FORMATTER);
            String endTime = value.getEndTime().format(TIME_FORMATTER);
            String date = value.getStartTime().format(DATE_FORMATTER);
            timeLabel.setText("🕒 " + startTime + " → " + endTime + " | " + date);

            String location = value.getLocation();
            if (location == null || location.trim().isEmpty()) {
                locationLabel.setText("📍 No location");
            } else {
                locationLabel.setText("📍 " + location);
            }
        }

        if (isSelected) {
            setBackground(new Color(52, 152, 219));
            titleLabel.setForeground(Color.WHITE);
            timeLabel.setForeground(new Color(200, 220, 240));
            locationLabel.setForeground(new Color(200, 220, 240));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(30, 120, 200), 2),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        } else {
            setBackground(new Color(250, 252, 255));
            titleLabel.setForeground(new Color(40, 50, 60));
            timeLabel.setForeground(new Color(100, 120, 140));
            locationLabel.setForeground(new Color(140, 150, 160));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 225, 235), 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        }

        return this;
    }
}

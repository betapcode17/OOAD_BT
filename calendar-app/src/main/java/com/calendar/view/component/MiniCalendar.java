package com.calendar.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MiniCalendar extends JPanel {
    public interface DateClickListener {
        void onDateClicked(LocalDate date);
    }

    private YearMonth currentMonth;
    private final JLabel monthLabel;
    private DateClickListener dateClickListener;
    private LocalDate selectedDate = null;
    private final JPanel calendarPanel;

    public MiniCalendar() {
        this.currentMonth = YearMonth.now();
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(new Color(245, 247, 250));

        // Header with month/year
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JButton prevBtn = new JButton("◀");
        prevBtn.setFocusPainted(false);
        prevBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        prevBtn.addActionListener(e -> previousMonth());

        JButton nextBtn = new JButton("▶");
        nextBtn.setFocusPainted(false);
        nextBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        nextBtn.addActionListener(e -> nextMonth());

        monthLabel = new JLabel();
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        monthLabel.setHorizontalAlignment(JLabel.CENTER);

        headerPanel.add(prevBtn, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(nextBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Single calendar grid with 7 columns x 7 rows (first row = headers), ensures stable 7-day header
        this.calendarPanel = new JPanel(new GridLayout(7, 7, 4, 4));
        this.calendarPanel.setOpaque(false);
        rebuildCalendarPanel();
        add(this.calendarPanel, BorderLayout.CENTER);

        updateMonthLabel();
    }

    private void rebuildCalendarPanel() {
        calendarPanel.removeAll();

        // Day headers (first row)
        String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayHeader : dayHeaders) {
            JLabel dayLabel = new JLabel(dayHeader);
            dayLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            dayLabel.setHorizontalAlignment(JLabel.CENTER);
            dayLabel.setForeground(new Color(100, 120, 140));
            calendarPanel.add(dayLabel);
        }

        // Date cells: ensure exactly 42 cells (6 weeks x 7 days)
        LocalDate firstDay = currentMonth.atDay(1);
        int daysInMonth = currentMonth.lengthOfMonth();
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // 0 = Sunday

        // previous-month fillers
        LocalDate previousMonthDate = firstDay.minusDays(startDayOfWeek);
        for (int i = 0; i < startDayOfWeek; i++) {
            JButton dayBtn = new JButton(String.valueOf(previousMonthDate.getDayOfMonth()));
            dayBtn.setFocusPainted(false);
            dayBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
            dayBtn.setForeground(new Color(180, 180, 180));
            dayBtn.setOpaque(true);
            dayBtn.setBackground(new Color(250, 250, 250));
            dayBtn.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            calendarPanel.add(dayBtn);
            previousMonthDate = previousMonthDate.plusDays(1);
        }

        // current month days
        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDay = day;
            LocalDate currentDate = currentMonth.atDay(day);
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFocusPainted(false);
            dayBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            dayBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

            if (selectedDate != null && currentDate.equals(selectedDate)) {
                dayBtn.setBackground(new Color(52, 152, 219));
                dayBtn.setForeground(Color.WHITE);
                dayBtn.setOpaque(true);
            } else if (selectedDate == null) {
                LocalDate today = LocalDate.now();
                YearMonth todayMonth = YearMonth.now();
                if (day == today.getDayOfMonth() && currentMonth.equals(todayMonth)) {
                    dayBtn.setBackground(new Color(52, 152, 219));
                    dayBtn.setForeground(Color.WHITE);
                    dayBtn.setOpaque(true);
                } else {
                    dayBtn.setBackground(Color.WHITE);
                    dayBtn.setForeground(new Color(40, 50, 60));
                    dayBtn.setOpaque(true);
                }
            } else {
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setForeground(new Color(40, 50, 60));
                dayBtn.setOpaque(true);
            }

            dayBtn.addActionListener(e -> {
                LocalDate clickedDate = currentMonth.atDay(currentDay);
                setSelectedDate(clickedDate);
                if (dateClickListener != null) {
                    dateClickListener.onDateClicked(clickedDate);
                }
            });

            calendarPanel.add(dayBtn);
        }

        // next-month fillers to reach 42 cells total
        int filled = startDayOfWeek + daysInMonth;
        int remaining = 42 - filled;
        for (int i = 1; i <= remaining; i++) {
            JButton dayBtn = new JButton(String.valueOf(i));
            dayBtn.setFocusPainted(false);
            dayBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
            dayBtn.setForeground(new Color(180, 180, 180));
            dayBtn.setOpaque(true);
            dayBtn.setBackground(new Color(250, 250, 250));
            dayBtn.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            calendarPanel.add(dayBtn);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private void previousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        updateMonthLabel();
        rebuildCalendarPanel();
    }

    private void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        updateMonthLabel();
        rebuildCalendarPanel();
    }

    private void updateMonthLabel() {
        String month = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthLabel.setText(month + " " + currentMonth.getYear());
    }

    public void setDateClickListener(DateClickListener listener) {
        this.dateClickListener = listener;
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        rebuildCalendarPanel();
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }
}

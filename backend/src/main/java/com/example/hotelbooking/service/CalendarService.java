package com.example.hotelbooking.service;

import com.example.hotelbooking.entity.Booking;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Surprise feature: builds an .ics calendar file so guests can add their stay to any calendar app. */
@Service
public class CalendarService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public String toIcs(Booking b) {
        // All-day event: DTEND is exclusive in iCalendar, which matches the check-out day exactly.
        String[] lines = {
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "PRODID:-//Hotel Booking Portal//EN",
                "CALSCALE:GREGORIAN",
                "BEGIN:VEVENT",
                "UID:" + b.getReference() + "@hotel-booking-portal",
                "DTSTAMP:" + STAMP.format(LocalDateTime.now()),
                "DTSTART;VALUE=DATE:" + DATE.format(b.getCheckIn()),
                "DTEND;VALUE=DATE:" + DATE.format(b.getCheckOut()),
                "SUMMARY:" + escape("Stay at " + b.getHotel().getName()),
                "LOCATION:" + escape(b.getHotel().getName() + ", " + b.getHotel().getLocation()),
                "DESCRIPTION:" + escape("Booking reference " + b.getReference() + ". " + b.getNights() + " night(s)."),
                "END:VEVENT",
                "END:VCALENDAR"
        };
        return String.join("\r\n", lines) + "\r\n";
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n");
    }
}

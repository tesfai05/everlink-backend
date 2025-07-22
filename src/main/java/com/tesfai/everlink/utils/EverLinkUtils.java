package com.tesfai.everlink.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EverLinkUtils {

    private static final String DEFAULT_PATTERN = "MM/dd/yyyy";
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    // Convert LocalDate to String
    public static String toString(LocalDate date) {
        return date != null ? date.format(DEFAULT_FORMATTER) : null;
    }

    // Convert String to LocalDate
    public static LocalDate fromString(String dateString) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DEFAULT_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected: " + DEFAULT_PATTERN, e);
        }
    }

    //Total months Since JoinDate
    public static int monthsBetweenDates(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        Period period = Period.between(date1, date2);
        return period.getYears() * 12 + period.getMonths();
    }

    //Total months Since JoinDate
    public static boolean isAfter(LocalDate joinDate, LocalDate leaveDate) {
        if (joinDate == null ) {
            throw new IllegalArgumentException("Join date must not be null");
        }
        if (leaveDate == null ) {
            throw new IllegalArgumentException("Leave date must not be null");
        }
        return joinDate.isAfter(leaveDate);
    }

    public  static boolean isAlphanumeric(String str) {
        return str != null && str.matches("[a-zA-Z0-9]+");
    }
}

package com.example.mini.global.util.datetime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class DateTimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static List<LocalDateTime> parseDateTimes(String checkIn, String checkOut) {
        LocalDateTime convertedCheckIn;
        LocalDateTime convertedCheckOut;
        if (checkIn == null || checkIn.isEmpty()) {
            convertedCheckIn = LocalDateTime.now();
            convertedCheckOut = convertedCheckIn.minusDays(1);
        } else {
            convertedCheckIn = LocalDateTime.parse(checkIn, FORMATTER);
            convertedCheckOut = LocalDateTime.parse(checkOut, FORMATTER);
        }
        return Arrays.asList(convertedCheckIn, convertedCheckOut);
    }
}

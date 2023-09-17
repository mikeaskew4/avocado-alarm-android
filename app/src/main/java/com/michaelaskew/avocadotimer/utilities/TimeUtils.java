package com.michaelaskew.avocadotimer.utilities;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeUtils {

    public static String getRelativeTimeText(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(localDateTime, now);

        if (duration.isNegative()) {
            return "in the future"; // Handle dates in the future if needed
        }

        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        if (days > 0) {
            if (days == 1) return "a day ago";
            return days + " days ago";
        }
        if (hours > 0 || minutes > 50) {
            if (hours == 1) return "around an hour ago";
            return hours + " hours ago";
        }

        if (minutes > 0) {
            if (minutes == 1) return "just a minute ago";
            return minutes + " minutes ago";
        }

        return "just now";
    }

    public static Object[] getTimeRemaining(String dateTime, int timer, int squishiness) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
        LocalDateTime targetTime = localDateTime.plusMinutes(timer - (squishiness * 60));
        LocalDateTime now = LocalDateTime.now();

        Duration timeRemaining = Duration.between(now, targetTime);

        long days = timeRemaining.toDays();
        long hours = timeRemaining.toHours();
        long minutes = timeRemaining.minusHours(hours).toMinutes();
        long seconds = timeRemaining.minusHours(hours).minusMinutes(minutes).toSeconds();

        String formattedTime;
        if (timeRemaining.isNegative() || timeRemaining.isZero()) {
            formattedTime = "Avocado is ready!";
        } else {
            formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }

        double fractionElapsed = ((double) timeRemaining.toMinutes() / timer);

        return new Object[]{formattedTime, fractionElapsed};
    }
}

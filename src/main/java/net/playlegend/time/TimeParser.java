package net.playlegend.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.jetbrains.annotations.NotNull;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class TimeParser {

    private final String string;
    private LocalDateTime localDateTime;
    private LocalDateTime base;

    public TimeParser(@NotNull String string, @NotNull LocalDateTime base) {
        this.string = string;
        this.base = base;
    }

    public static LocalDateTime localDateTimeFromEpochSeconds(long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds, 0), ZoneId.systemDefault());
    }

    public static String epochSecondsToInline(long epochSecs) {
        return localDateTimeFromEpochSeconds(epochSecs).format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public long parseToEpochSeconds() {
        if (localDateTime == null)
            this.localDateTime = base.plus(getInSeconds(), ChronoUnit.SECONDS);

        return localDateTime.atZone(ZoneId.systemDefault())
                .toEpochSecond();
    }

    public long getInSeconds() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays().appendSuffix("d")
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .appendSeconds().appendSuffix("s")
                .toFormatter();

        return formatter.parsePeriod(string).toStandardSeconds().getSeconds();
    }

}

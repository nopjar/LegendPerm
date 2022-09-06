package net.playlegend.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TimeParserTest {

    @Test
    void parseToEpochSeconds() {
        LocalDateTime ldt = LocalDateTime.of(2022, 9, 6, 2, 51);
        long epochSeconds = new TimeParser(ldt).parseToEpochSeconds();

        assertEquals(1662425460, epochSeconds);
    }

    @Test
    void getInSeconds() {
        TimeParser timeParser = new TimeParser("1d23m10s", LocalDateTime.now());

        assertEquals(87790, timeParser.getInSeconds());
    }

}
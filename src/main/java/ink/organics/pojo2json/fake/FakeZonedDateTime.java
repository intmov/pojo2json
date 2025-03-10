package ink.organics.pojo2json.fake;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FakeZonedDateTime extends FakeTemporal implements JsonFakeValuesService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public Object random() {
        return ZonedDateTime
                .ofInstant(Instant.ofEpochMilli((long) super.random()), ZoneId.systemDefault())
                .format(formatter);
    }

    @Override
    public Object def() {
        return ZonedDateTime
                .ofInstant(Instant.ofEpochMilli((long) super.def()), ZoneId.systemDefault())
                .format(formatter);
    }
}

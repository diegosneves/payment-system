package br.com.neves.paymentsystem.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.util.Objects.isNull;

public enum DataConverter {

    BRAZIL(ZoneId.of("America/Sao_Paulo"));


    private ZoneId zoneId;

    DataConverter(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public Instant toInstant(final LocalDate localDate) {
        return isNull(localDate) ? null : localDate.atStartOfDay(this.zoneId).toInstant();
    }

    public Instant toInstant(final LocalDateTime localDateTime) {
        return isNull(localDateTime) ? null : localDateTime.atZone(this.zoneId).toInstant();
    }

    public LocalDate toLocalDate(final Instant instant) {
        return isNull(instant) ? null : LocalDate.ofInstant(instant, this.zoneId);
    }

    public LocalDateTime toLocalDateTime(final Instant instant) {
        return isNull(instant) ? null : LocalDateTime.ofInstant(instant, this.zoneId);
    }

    public Instant toInstantNow() {
        return Instant.now();
    }

    public LocalDateTime toLocalDateTimeNow() {
        return LocalDateTime.now(this.zoneId);
    }

    public LocalDate toLocalDateNow() {
        return LocalDate.now(this.zoneId);
    }

}

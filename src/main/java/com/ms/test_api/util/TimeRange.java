package com.ms.test_api.util;

import java.time.Duration;
import java.time.LocalTime;

public record TimeRange(LocalTime start, LocalTime end) {

    public TimeRange {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end time cannot be null");
        }
        if (end.isBefore(start) || end.equals(start)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }

    /** true nếu hai khoảng có phần giao nhau. */
    public boolean overlaps(TimeRange other) {
        if (other == null) {
            throw new IllegalArgumentException("Other time range cannot be null");
        }
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }

    /** true nếu khoảng `other` nằm gọn bên trong khoảng này. */
    public boolean contains(TimeRange other) {
        if (other == null) {
            throw new IllegalArgumentException("Other time range cannot be null");
        }
        return !this.start.isAfter(other.start) && !this.end.isBefore(other.end);
    }

    public long durationMinutes() {
        return Duration.between(start, end).toMinutes();
    }
}
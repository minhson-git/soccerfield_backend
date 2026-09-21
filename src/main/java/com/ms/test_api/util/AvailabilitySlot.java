package com.ms.test_api.util;

import java.time.LocalTime;

public record AvailabilitySlot(LocalTime startTime, LocalTime endTime, boolean available) {
}
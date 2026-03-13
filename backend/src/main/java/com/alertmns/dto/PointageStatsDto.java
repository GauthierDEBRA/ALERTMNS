package com.alertmns.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointageStatsDto {

    private Long dailyMinutes;
    private Long weeklyMinutes;
    private Long monthlyMinutes;

    private String dailyFormatted;
    private String weeklyFormatted;
    private String monthlyFormatted;

    private Boolean isCurrentlyPresent;
    private String arrivedAt;

    public static String formatMinutes(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        return String.format("%dh%02d", hours, mins);
    }
}

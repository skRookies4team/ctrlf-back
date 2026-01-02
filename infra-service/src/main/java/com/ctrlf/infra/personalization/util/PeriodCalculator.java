package com.ctrlf.infra.personalization.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Period 유형에 따른 기간 계산 유틸리티.
 */
public final class PeriodCalculator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private PeriodCalculator() {}

    /**
     * Period 유형에 따른 기간을 계산합니다.
     * 
     * @param period Period 유형 (this-week, this-month, 3m, this-year)
     * @return PeriodDates (시작일, 종료일)
     */
    public static PeriodDates calculatePeriod(String period) {
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end = today;

        if (period == null || period.isBlank()) {
            // 기본값: this-year
            period = "this-year";
        }

        switch (period) {
            case "this-week":
                // 이번 주 (월~일)
                start = today.minusDays(today.getDayOfWeek().getValue() - 1);
                break;
            case "this-month":
                // 이번 달
                start = today.withDayOfMonth(1);
                break;
            case "3m":
                // 최근 3개월 (90일)
                start = today.minusDays(90);
                break;
            case "this-year":
                // 올해
                start = today.withDayOfYear(1);
                break;
            default:
                // 기본값: this-year
                start = today.withDayOfYear(1);
        }

        return new PeriodDates(
            start.format(DATE_FORMATTER),
            end.format(DATE_FORMATTER)
        );
    }

    /**
     * Period 시작일과 종료일을 담는 클래스.
     */
    public static class PeriodDates {
        private final String start;
        private final String end;

        public PeriodDates(String start, String end) {
            this.start = start;
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }
    }
}


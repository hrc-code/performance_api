package com.example.workflow.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class DateTimeUtils {
    /** 返回本月的最开始的时间和结束时间
     * [0]  为月初时间  [1] 为月末时间*/
    public static  LocalDateTime[]  getTheStartAndEndTimeOfMonth() {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 获取本月的第一天（即月初）
        LocalDate firstDayOfMonth = currentDate.with(TemporalAdjusters.firstDayOfMonth());
        // 将月初日期与凌晨0点（即一天的开始）组合成LocalDateTime
        LocalDateTime startOfMonth = LocalDateTime.of(firstDayOfMonth, LocalTime.MIDNIGHT);
        // 获取本月的最后一天（即月末）
        LocalDate lastDayOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth());
        // 将月末日期与一天的最后一刻（即23:59:59.999...）组合成LocalDateTime
        LocalDateTime endOfMonth = LocalDateTime.of(lastDayOfMonth, LocalTime.MAX);
        return new LocalDateTime[]{startOfMonth, endOfMonth};
    }

    /* 返回现在的时间 */
    public static String now(String format) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }
}

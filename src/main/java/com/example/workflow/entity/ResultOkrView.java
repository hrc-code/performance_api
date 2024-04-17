package com.example.workflow.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ResultOkrView {
        @JsonSerialize(using= ToStringSerializer.class)
        private Long okrId;
        private String target;
        @JsonSerialize(using= ToStringSerializer.class)
        private Long assessorId;
        private Double totalScore;
        @JsonSerialize(using= ToStringSerializer.class)
        private Long keyId;
        private String keyResult;
        private Double keyWeight;
        @JsonSerialize(using= ToStringSerializer.class)
        private Long positionId;
        @JsonSerialize(using= ToStringSerializer.class)
        private Long liaEmpId;
        @JsonSerialize(using= ToStringSerializer.class)
        private String name;
        @JsonSerialize(using= ToStringSerializer.class)
        private Long empOkrId;
        private BigDecimal score;
        private Short examine;
        private BigDecimal result;
        private Short state;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
}

package com.example.workflow.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ResultEmpPieceExcel {
    private Integer workOrder;
    private BigDecimal quality;
    private String name;
    private BigDecimal targetNum;
    private String empName;
}

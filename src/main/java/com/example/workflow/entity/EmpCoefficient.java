package com.example.workflow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmpCoefficient {
    @TableId
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    @JsonSerialize(using= ToStringSerializer.class)
    private BigDecimal positionCoefficient;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long regionCoefficientId;
    private BigDecimal wage;
    private BigDecimal performanceWage;
    private Short state;

    @TableField(fill= FieldFill.INSERT)//插入时填充字段
    private LocalDateTime createTime;

    @TableField(fill=FieldFill.INSERT_UPDATE)//插入和更新时填充字段
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private  Long updateUser;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        EmpCoefficient other = (EmpCoefficient) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getEmpId() == null ? other.getEmpId() == null : this.getEmpId().equals(other.getEmpId()))
                && (this.getPositionCoefficient() == null ? other.getPositionCoefficient() == null : this.getPositionCoefficient().equals(other.getPositionCoefficient()))
                && (this.getRegionCoefficientId() == null ? other.getRegionCoefficientId() == null : this.getRegionCoefficientId().equals(other.getRegionCoefficientId()))
                && (this.getWage() == null ? other.getWage() == null : this.getWage().equals(other.getWage()))
                && (this.getPerformanceWage() == null ? other.getPerformanceWage() == null : this.getPerformanceWage().equals(other.getPerformanceWage()))
                && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateUser() == null ? other.getUpdateUser() == null : this.getUpdateUser().equals(other.getUpdateUser()))
                && (this.getCreateUser() == null ? other.getCreateUser() == null : this.getCreateUser().equals(other.getCreateUser()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getEmpId() == null) ? 0 : getEmpId().hashCode());
        result = prime * result + ((getPositionCoefficient() == null) ? 0 : getPositionCoefficient().hashCode());
        result = prime * result + ((getRegionCoefficientId() == null) ? 0 : getRegionCoefficientId().hashCode());
        result = prime * result + ((getWage() == null) ? 0 : getWage().hashCode());
        result = prime * result + ((getPerformanceWage() == null) ? 0 : getPerformanceWage().hashCode());
        result = prime * result + ((getState() == null) ? 0 : getState().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateUser() == null) ? 0 : getUpdateUser().hashCode());
        result = prime * result + ((getCreateUser() == null) ? 0 : getCreateUser().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", empId=").append(empId);
        sb.append(", positionCoefficient=").append(positionCoefficient);
        sb.append(", regionCoefficientId=").append(regionCoefficientId);
        sb.append(", wage=").append(wage);
        sb.append(", performanceWage=").append(performanceWage);
        sb.append(", state=").append(state);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateUser=").append(updateUser);
        sb.append(", createUser=").append(createUser);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}

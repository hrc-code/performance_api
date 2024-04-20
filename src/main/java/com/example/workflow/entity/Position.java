package com.example.workflow.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.workflow.common.StateChange;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
public class Position implements Serializable {
    @JsonSerialize(using= ToStringSerializer.class)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String position;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long deptId;
    private Short type;
    private String typeName;
    private Short kind;
    private String kindName;
    private Short auditStatus;
    @JsonDeserialize(using = StateChange.class)
    private Short state;
    private String ins;

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
        Position other = (Position) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getDeptId() == null ? other.getDeptId() == null : this.getDeptId().equals(other.getDeptId()))
                && (this.getPosition() == null ? other.getPosition() == null : this.getPosition().equals(other.getPosition()))
                && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
                && (this.getTypeName() == null ? other.getTypeName() == null : this.getTypeName().equals(other.getTypeName()))
                && (this.getAuditStatus() == null ? other.getAuditStatus() == null : this.getAuditStatus().equals(other.getAuditStatus()))
                && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()))
                && (this.getIns() == null ? other.getIns() == null : this.getIns().equals(other.getIns()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getCreateUser() == null ? other.getCreateUser() == null : this.getCreateUser().equals(other.getCreateUser()))
                && (this.getUpdateUser() == null ? other.getUpdateUser() == null : this.getUpdateUser().equals(other.getUpdateUser()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getDeptId() == null) ? 0 : getDeptId().hashCode());
        result = prime * result + ((getPosition() == null) ? 0 : getPosition().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getTypeName() == null) ? 0 : getTypeName().hashCode());
        result = prime * result + ((getAuditStatus() == null) ? 0 : getAuditStatus().hashCode());
        result = prime * result + ((getState() == null) ? 0 : getState().hashCode());
        result = prime * result + ((getIns() == null) ? 0 : getIns().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getCreateUser() == null) ? 0 : getCreateUser().hashCode());
        result = prime * result + ((getUpdateUser() == null) ? 0 : getUpdateUser().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", deptId=").append(deptId);
        sb.append(", position=").append(position);
        sb.append(", type=").append(type);
        sb.append(", typeName=").append(typeName);
        sb.append(", auditStatus=").append(auditStatus);
        sb.append(", state=").append(state);
        sb.append(", ins=").append(ins);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", createUser=").append(createUser);
        sb.append(", updateUser=").append(updateUser);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
    
}

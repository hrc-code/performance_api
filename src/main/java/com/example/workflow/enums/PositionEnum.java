package com.example.workflow.enums;

import lombok.Getter;


@Getter
public enum PositionEnum {

    //一类岗位
    ONE("1","Process_1gzouwy"),
    FOUR("4", "Process_1whe0gq"),
    THREE("3", "Process_01p7ac7")
    ;
    /** 岗位类型*/
    final Short type ;
    /** 流程key */
    final String processKey;

    PositionEnum(String type, String processKey) {
        this.type = Short.parseShort(type);
        this.processKey = processKey;
    }
}

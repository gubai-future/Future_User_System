package com.mysystem.futuresystemhd.constant;

public enum ExamineConstant {
    EXAMINE_ABSENCE(0,"待审核"),
    EXAMINE_AGREE(1,"同意"),
    EXAMINE_DISAGREE(2,"不同意"),
    EXAMINE_OBSOLETE(3,"过时");


    private Integer code;

    private String msg;

    ExamineConstant(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static String getByMessage(Integer code){
        ExamineConstant[] values = ExamineConstant.values();

        for (ExamineConstant value : values) {
            if(value.getCode().equals(code)){
                return value.getMsg();
            }
        }

        return null;
    }


    public static ExamineConstant getByStatus(Integer code){
        ExamineConstant[] values = ExamineConstant.values();

        for (ExamineConstant value : values) {
            if(value.getCode().equals(code)){
                return value;
            }
        }

        return null;
    }
}

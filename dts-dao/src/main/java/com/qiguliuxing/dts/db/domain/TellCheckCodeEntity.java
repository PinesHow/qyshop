package com.qiguliuxing.dts.db.domain;


import java.util.Date;

/**
 * @ClassName: TellCheckCodeEntity
 * @Description:
 * @author: mh
 * @date: 2020/6/30
 * @Version: 1.0
 */
public class TellCheckCodeEntity {

    private String tell;
    private Date createDate;
    private String code;

    public String getTell() {
        return tell;
    }

    public void setTell(String tell) {
        this.tell = tell;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

package me.hyperperform.reporting.request;

import java.sql.Timestamp;

/**
 * Request object for GetScore method found in {@see me.hyperperform.reporting.IReport}.
 *
 * @author : CodusMaximus
 * @version : 1.0
 * @since : 2016/08/19
 */
public class GetScoreRequest {

    private String name;
    private Timestamp startDate;
    private Timestamp endDate;

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = Timestamp.valueOf(endDate);
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = Timestamp.valueOf(startDate);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

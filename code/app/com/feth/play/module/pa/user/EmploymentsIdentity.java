package com.feth.play.module.pa.user;

import java.io.Serializable;
import java.util.Collection;

public interface EmploymentsIdentity {
  public static class EmploymentInfo implements Serializable {
    protected String id;
    protected String title;
    protected String summary;
    protected int startDateMonth;
    protected int startDateYear;
    protected int endDateMonth;
    protected int endDateYear;
    protected boolean isCurrent;
    protected String companyName;
    
    public EmploymentInfo(String id, 
        String title, String summary, 
        int startDateMonth, int startDateYear, 
        int endDateMonth, int endDateYear, 
        boolean isCurrent, String companyName) {
      this.id = id;
      this.title = title;
      this.summary = summary;
      this.startDateMonth = startDateMonth;
      this.startDateYear = startDateYear;
      this.endDateMonth = endDateMonth;
      this.endDateYear = endDateYear;
      this.isCurrent = isCurrent;
      this.companyName = companyName;
    }
    
    public String getId() {
      return id;
    }
    
    public String getTitle() {
      return title;
    }
    
    public String getSummary() {
      return summary;
    }
    
    public int getStartDateMonth() {
      return startDateMonth;
    }
    
    public int getStartDateYear() {
      return startDateYear;
    }
    
    public int getEndDateMonth() {
      return endDateMonth;
    }
    
    public int getEndDateYear() {
      return endDateYear;
    }
    
    public boolean isCurrent() {
      return isCurrent;
    }
    
    public String getCompanyName() {
      return companyName;
    }
    
  }
  
  public Collection<EmploymentInfo> getEmployments();
  
}

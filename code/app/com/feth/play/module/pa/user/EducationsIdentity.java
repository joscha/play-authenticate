package com.feth.play.module.pa.user;

import java.io.Serializable;
import java.util.Collection;

public interface EducationsIdentity {
  
  class EducationInfo implements Serializable {
    protected String id;
    protected String schoolName;
    protected String degree;
    protected int startDateYear;
    protected int endDateYear;
    
    public EducationInfo(String id, String schoolName, String degree, int startDateYear, int endDateYear) {
      this.id = id;
      this.schoolName = schoolName;
      this.degree = degree;
      this.startDateYear = startDateYear;
      this.endDateYear = endDateYear;
    }
    
    public String getId() {
      return id;
    }
    
    public String getSchoolName() {
      return schoolName;
    }
    
    public String getDegree() {
      return degree;
    }
    
    public int getStartDateYear() {
      return startDateYear;
    }
    
    public int getEndDateYear() {
      return endDateYear;
    }
    
  }
  
  public Collection<EducationInfo> getEducations();
}

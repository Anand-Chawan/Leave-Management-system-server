package com.achawan.employee;

import java.io.Serializable;
import java.time.LocalDate;

public class ApplyLeaves implements Serializable {

	private static final long serialVersionUID = 1L;

	private String empName;

	private LocalDate fromDate;

	private LocalDate tillDate;

	private String leaveType;

	public LocalDate getFromDate() {
		return fromDate;
	}

	public void setFromDate(LocalDate fromDate) {
		this.fromDate = fromDate;
	}

	public LocalDate getTillDate() {
		return tillDate;
	}

	public void setTillDate(LocalDate tillDate) {
		this.tillDate = tillDate;
	}

	public String getLeaveType() {
		return leaveType;
	}

	public void setLeaveType(String leaveType) {
		this.leaveType = leaveType;
	}

	public String getEmpName() {
		return empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	@Override
	public String toString() {
		return "ApplyLeaves [empName=" + empName + ", fromDate=" + fromDate + ", tillDate=" + tillDate + ", leaveType="
				+ leaveType + "]";
	}

}

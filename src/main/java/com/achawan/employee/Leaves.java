package com.achawan.employee;

import java.time.LocalDate;
import java.util.HashMap;

public class Leaves {

	private int PTO;

	private int SL;

	private int OH;

	
	// key -> date,   value -> leaveType
	private HashMap<LocalDate, String> pendingLeaves = new HashMap<>();
	private HashMap<LocalDate, String> approvedLeaves = new HashMap<>(); 
	private HashMap<LocalDate, String> rejectedLeaves = new HashMap<>(); 
	
	public Leaves() {
		this.PTO = 10;
		this.SL = 5;
		this.OH = 3;
	}

	public int getPTO() {
		return PTO;
	}

	public void setPTO(int pTO) {
		PTO = pTO;
	}

	public int getSL() {
		return SL;
	}

	public void setSL(int sL) {
		SL = sL;
	}

	public int getOH() {
		return OH;
	}

	public void setOH(int oH) {
		OH = oH;
	}

	public HashMap<LocalDate, String> getPendingLeaves() {
		return pendingLeaves;
	}

	public void setPendingLeaves(LocalDate date, String leaveType) {
		pendingLeaves.put(date, leaveType);
	}

	public HashMap<LocalDate, String> getApprovedLeaves() {
		return approvedLeaves;
	}

	public void setApprovedLeaves(LocalDate date, String leaveType) {
		approvedLeaves.put(date, leaveType);
	}
	
	public HashMap<LocalDate, String> getRejectedLeaves() {
		return rejectedLeaves;
	}

	public void setRejectedLeaves(LocalDate date, String leaveType) {
		rejectedLeaves.put(date, leaveType);
	}
	
	@Override
	public String toString() {
		return "PTO=" + PTO + ", SL=" + SL + ", OH=" + OH;
	}

}

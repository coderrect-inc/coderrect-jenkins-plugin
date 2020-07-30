package io.jenkins.plugins.coderrect;

import java.util.ArrayList;

public class CoderrectStats {
    // does CoderrectParser parse the race json correctly? Otherwise
    // all metrics are meaningless.
    private boolean parsedOk;

    // when parsedOk==false, errmsg gives us more details about the failure
    private String errmsg;

    private String projectName;
    private String reportGeneratedAt;
    private String coderrectVer;

    // # of data races
    private int dataRaces;
    // # of deadlocks
    private int deadlocks;
    // # of mismatched APIs
    private int mismatchedAPIs;
    // # of atomicity violations
    private int atomicityViolations;
    // # of order violations
    private int orderViolations;
    // # of total races
    private int total;

    // list of data races
    private ArrayList<DataRace> dataRaceList;

    public CoderrectStats() {
        this.parsedOk = true;
        this.errmsg = "OK";
        this.dataRaceList = new ArrayList<DataRace>();
    }

    public ArrayList<DataRace> getDataRaceList() {
        return this.dataRaceList;
    }

    public boolean getParsedOk() {
        return parsedOk;
    }
    public void setParsedOk(boolean parsedOk) {
        this.parsedOk = parsedOk;
    }

    public String getErrmsg() {
        return errmsg;
    }
    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getReportGeneratedAt() {
        return reportGeneratedAt;
    }
    public void setReportGeneratedAt(String reportGeneratedAt) {
        this.reportGeneratedAt = reportGeneratedAt;
    }

    public String getCoderrectVer() {
        return coderrectVer;
    }
    public void setCoderrectVer(String coderrectVer) {
        this.coderrectVer = coderrectVer;
    }

    public int getDataRaces() {
        return this.dataRaces;
    }
    public void setDataRaces(int dataRaces) {
        this.dataRaces = dataRaces;
        updateTotal();
    }

    public int getDeadlocks() {
        return deadlocks;
    }
    public void setDeadlocks(int deadlocks) {
        this.deadlocks = deadlocks;
        updateTotal();
    }

    public int getMismatchedAPIs() {
        return mismatchedAPIs;
    }
    public void setMismatchedAPIs(int mismatchedAPIs) {
        this.mismatchedAPIs = mismatchedAPIs;
        updateTotal();
    }

    public int getAtomicityViolations() {
        return atomicityViolations;
    }
    public void setAtomicityViolations(int atomicityViolations) {
        this.atomicityViolations = atomicityViolations;
        updateTotal();
    }

    public int getOrderViolations() {
        return orderViolations;
    }
    public void setOrderViolations(int orderViolations) {
        this.orderViolations = orderViolations;
        updateTotal();
    }

    public int getTotal() {
        return this.total;
    }
    private void updateTotal() {
        this.total = dataRaces + deadlocks + mismatchedAPIs + atomicityViolations + orderViolations;
    }
}

package com.DeSCAM.Check;

public class MessageObj {

    public MessageObj(String file, String msg, String severityLevel, String violationType, int rowStartNum, int rowEndNum, int columnStartNum, int columnEndNum) {
        setFileDir(file);
        setMessage(msg);
        setSeverityLevel(severityLevel);
        setViolationType(violationType);
        setRowStartNumber(rowStartNum);
        setRowEndNumber(rowEndNum);
        setColumnStartNumber(columnStartNum);
        setColumnEndNumber(columnEndNum);
    }

    public void setFileDir(String dir) {
        fileDir = dir;
    }

    public void setMessage(String msg) {
        message = msg;
    }

    public void setSeverityLevel(String level) {
        severity = level;
    }

    public void setRowStartNumber(int rowStartNumber) {
        rowStart = rowStartNumber;
    }

    public void setRowEndNumber(int rowEndNumber) {
        rowEnd = rowEndNumber;
    }

    public void setColumnStartNumber(int ColumnStartNumber) {
        columnStart = ColumnStartNumber;
    }

    public void setColumnEndNumber(int ColumnEndNumber) {
        columnEnd = ColumnEndNumber;
    }

    public void setViolationType(String violationType) {
        violation = violationType;
    }

    public String getFileDir() {
        return fileDir;
    }

    public String getMessage() {
        return message;
    }

    public String getSeverityLevel() {
        return severity;
    }

    public String getViolationType() {
        return violation;
    }

    public int getRowStartNum() {
        return rowStart;
    }

    public int getRowEndNum() {
        return rowEnd;
    }

    public int getColumnStartNum() {
        return columnStart;
    }

    public int getColumnEndNum() {
        return columnEnd;
    }


    private String fileDir = "", message = "", severity = "", violation = "";
    private int rowStart = 0, rowEnd = 0, columnStart = 0, columnEnd = 0;
}

package com.example.android.musicapp;

public class Admin {

    private String competitionName;
    private String competitionDate;
    private String competitionJoinDate;
    private String status;
    private String compId;
    private boolean complete;



    public Admin(){

    }

    public Admin( String compId, String competitionName, String competitionDate,String competitionJoinDate, String status, boolean complete) {
        this.compId = compId;
        this.competitionName = competitionName;
        this.competitionDate = competitionDate;
        this.competitionJoinDate = competitionJoinDate;
        this.status = status;
        this.complete = complete;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionDate() {
        return competitionDate;
    }

    public String getCompId(){
        return compId;
    }

    public void setCompId(String compId){
        this.compId = compId;
    }

    public void setCompetitionDate(String competitionDate) {
        this.competitionDate = competitionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompetitionJoinDate() {
        return competitionJoinDate;
    }

    public void setCompetitionJoinDate(String competitionJoinDate) {
        this.competitionJoinDate = competitionJoinDate;
    }

    public boolean getComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}

package com.example.android.musicapp;

public class Competition {
    private String Username;
    private String Profile_url;
    private String Video;
    private String SongTitle;
    private String CompetitionStatus;
    private long votes;
    private long comments;

    public Competition(){

    }

    public Competition(String username, String profile_url, String video, String songTitle,String competitionStatus, long votes, long comments ) {
        Username = username;
        Profile_url = profile_url;
        Video = video;
        SongTitle = songTitle;
        CompetitionStatus = competitionStatus;
        this.votes = votes;
        this.comments = comments;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getProfile_url() {
        return Profile_url;
    }

    public void setProfile_url(String profile_url) {
        Profile_url = profile_url;
    }

    public String getVideo() {
        return Video;
    }

    public void setVideo(String video) {
        Video = video;
    }

    public String getSongTitle() {
        return SongTitle;
    }

    public void setSongTitle(String songTitle) {
        SongTitle = songTitle;
    }

    public String getCompetitionStatus() {
        return CompetitionStatus;
    }

    public void setCompetitionStatus(String competitionStatus) {
        CompetitionStatus = competitionStatus;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }

    public long getComments() {
        return comments;
    }

    public void setComments(long comments) {
        this.comments = comments;
    }
}

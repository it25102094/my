package lk.sportix.model;

/**
 * Simplified FollowTeam Model for Member 1 (IT25102095).
 * Represents a user following a sports team in the 'follow_teams' table.
 */
public class FollowTeam {

    private int followId;
    private int userId;
    private int teamId;
    private boolean notifyOnUpload;

    // Simple display fields joined from 'teams' and 'sports' tables
    private String teamName;
    private String country;
    private String sportName;
    private boolean followedByCurrentUser;

    public FollowTeam() {
    }

    public FollowTeam(int userId, int teamId, boolean notifyOnUpload) {
        this.userId = userId;
        this.teamId = teamId;
        this.notifyOnUpload = notifyOnUpload;
    }

    public int getFollowId() { return followId; }
    public void setFollowId(int followId) { this.followId = followId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public boolean isNotifyOnUpload() { return notifyOnUpload; }
    public void setNotifyOnUpload(boolean notifyOnUpload) { this.notifyOnUpload = notifyOnUpload; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getSportName() { return sportName; }
    public void setSportName(String sportName) { this.sportName = sportName; }

    public boolean isFollowedByCurrentUser() { return followedByCurrentUser; }
    public void setFollowedByCurrentUser(boolean followedByCurrentUser) { this.followedByCurrentUser = followedByCurrentUser; }
}
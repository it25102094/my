package lk.sportix.model;

/**
 * Sport Model — maps to the 'sports' table.
 * Used to build the Sport drop-downs from the database instead of hard-coding IDs in the JSPs
 * (the old upload form offered "Athletics" = id 6, which does not exist and broke the insert).
 */
public class Sport {
    private int sportId;
    private String sportName;

    public Sport() {}

    public Sport(int sportId, String sportName) {
        this.sportId = sportId;
        this.sportName = sportName;
    }

    public int getSportId() { return sportId; }
    public void setSportId(int sportId) { this.sportId = sportId; }

    public String getSportName() { return sportName; }
    public void setSportName(String sportName) { this.sportName = sportName; }
}

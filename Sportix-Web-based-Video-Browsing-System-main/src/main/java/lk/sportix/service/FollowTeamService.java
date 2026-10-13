package lk.sportix.service;

import lk.sportix.dao.FollowTeamDAO;
import lk.sportix.model.FollowTeam;

import java.util.ArrayList;
import java.util.List;

public class FollowTeamService {

    private final FollowTeamDAO followTeamDAO = new FollowTeamDAO();

    public boolean followTeam(int userId, int teamId, boolean notifyOnUpload) {
        if (userId <= 0 || teamId <= 0) return false;
        return followTeamDAO.followTeam(userId, teamId, notifyOnUpload);
    }

    public List<FollowTeam> getFollowedTeams(int userId) {
        return followTeamDAO.getFollowedTeamsByUser(userId);
    }

    public List<FollowTeam> getAllTeamsWithStatus(int userId) {
        return followTeamDAO.getAllTeamsWithFollowStatus(userId);
    }

    public boolean updateNotificationPreference(int userId, int teamId, boolean notifyOnUpload) {
        if (userId <= 0 || teamId <= 0) return false;
        return followTeamDAO.updateNotificationPreference(userId, teamId, notifyOnUpload);
    }

    public List<Integer> getFollowedTeamIds(int userId) {
        List<Integer> teamIds = new ArrayList<>();
        if (userId <= 0) return teamIds;
        for (FollowTeam ft : followTeamDAO.getFollowedTeamsByUser(userId)) {
            teamIds.add(ft.getTeamId());
        }
        return teamIds;
    }

    public boolean unfollowTeam(int userId, int teamId) {
        if (userId <= 0 || teamId <= 0) return false;
        return followTeamDAO.unfollowTeam(userId, teamId);
    }
}
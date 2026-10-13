package lk.sportix.service;

import lk.sportix.dao.WatchLaterDAO;
import lk.sportix.model.WatchLater;

import java.util.List;

/**
 * Simplified Service layer for Watch Later folders and saved videos.
 * Owned by Member 2 (IT25102096).
 */
public class WatchLaterService {

    private final WatchLaterDAO watchLaterDAO = new WatchLaterDAO();

    // CREATE Folder
    public boolean createFolder(int userId, String folderName) {
        if (userId <= 0 || folderName == null || folderName.trim().isEmpty()) {
            return false;
        }
        WatchLater folder = new WatchLater(userId, folderName.trim());
        return watchLaterDAO.createFolder(folder);
    }

    // READ Folders
    public List<WatchLater> getUserFolders(int userId) {
        return watchLaterDAO.getFoldersByUser(userId);
    }

    // UPDATE Folder Name
    public boolean renameFolder(int folderId, int userId, String newName) {
        if (folderId <= 0 || userId <= 0 || newName == null || newName.trim().isEmpty()) {
            return false;
        }
        return watchLaterDAO.renameFolder(folderId, userId, newName.trim());
    }

    // DELETE Folder
    public boolean deleteFolder(int folderId, int userId) {
        if (folderId <= 0 || userId <= 0) {
            return false;
        }
        return watchLaterDAO.deleteFolder(folderId, userId);
    }

    // CREATE Saved Video Item
    public boolean addVideoToWatchLater(int userId, int videoId, Integer folderId, String notes) {
        if (userId <= 0 || videoId <= 0) {
            return false;
        }
        String cleanNotes = (notes != null && !notes.trim().isEmpty()) ? notes.trim() : null;
        WatchLater item = new WatchLater(userId, videoId, folderId, cleanNotes);
        return watchLaterDAO.addWatchLaterItem(item);
    }

    // READ Saved Video Items
    public List<WatchLater> getWatchLaterItems(int userId, Integer folderIdFilter) {
        return watchLaterDAO.getItemsByUser(userId, folderIdFilter);
    }

    // UPDATE Saved Video Item (Move Folder or Update Note)
    public boolean updateWatchLaterItem(int itemId, int userId, Integer folderId, String notes) {
        if (itemId <= 0 || userId <= 0) {
            return false;
        }
        String cleanNotes = (notes != null && !notes.trim().isEmpty()) ? notes.trim() : null;
        return watchLaterDAO.updateWatchLaterItem(itemId, userId, folderId, cleanNotes);
    }
    public boolean isVideoSaved(int userId, int videoId) {
        if (userId <= 0 || videoId <= 0) return false;
        for (WatchLater item : watchLaterDAO.getItemsByUser(userId, null)) {
            if (item.getVideoId() == videoId) return true;
        }
        return false;
    }

    public boolean removeByUserAndVideo(int userId, int videoId) {
        if (userId <= 0 || videoId <= 0) return false;
        return watchLaterDAO.removeByUserAndVideo(userId, videoId);
    }
    // DELETE Saved Video Item
    public boolean removeWatchLaterItem(int itemId, int userId) {
        if (itemId <= 0 || userId <= 0) {
            return false;
        }
        return watchLaterDAO.removeWatchLaterItem(itemId, userId);
    }
}
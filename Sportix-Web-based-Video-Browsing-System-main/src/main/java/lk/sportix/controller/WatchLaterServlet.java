package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.sportix.model.WatchLater;
import lk.sportix.service.WatchLaterService;
import lk.sportix.util.WebUtil;

import java.io.IOException;
import java.util.List;

/**
 * WatchLaterServlet — Member 2: Watch Later list and folders.
 *   GET  /user/watchlater[?folder=ID]                       READ (optionally filtered by folder)
 *   POST action=add | remove                                saved videos  (create / delete)
 *   POST action=updateItem (folderId, notes)                saved videos  (update: move / note)
 *   POST action=createFolder | renameFolder | deleteFolder  folders       (create / update / delete)
 * FIX: the folder and item-update methods existed in the service/DAO but no servlet action or
 * UI ever called them, so Watch Later folders could not be used at all.
 */
@WebServlet(urlPatterns = {"/user/watchlater", "/watch-later"})
public class WatchLaterServlet extends HttpServlet {

    private final WatchLaterService watchLaterService = new WatchLaterService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int folderFilter = WebUtil.parseInt(request.getParameter("folder"), 0);

        // MVC: Controller calls Service to read data
        List<WatchLater> items = watchLaterService.getWatchLaterItems(userId, folderFilter > 0 ? folderFilter : null);
        request.setAttribute("watchLaterVideos", items);
        request.setAttribute("folders", watchLaterService.getUserFolders(userId));
        request.setAttribute("activeFolder", folderFilter);

        request.getRequestDispatcher("/WEB-INF/views/watch-later.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        int videoId = WebUtil.parseInt(request.getParameter("videoId"), 0);
        int folderId = WebUtil.parseInt(request.getParameter("folderId"), 0);
        int itemId = WebUtil.parseInt(request.getParameter("itemId"), 0);
        String folderName = request.getParameter("folderName");

        if ("remove".equals(action) && videoId > 0) {
            watchLaterService.removeByUserAndVideo(userId, videoId);
            WebUtil.flash(request, "Removed from Watch Later.");
        } else if (("add".equals(action) || "addItem".equals(action) || action == null) && videoId > 0) {
            watchLaterService.addVideoToWatchLater(userId, videoId, folderId > 0 ? folderId : null, null);
            WebUtil.flash(request, "\u2713 Saved to Watch Later!");
        } else if ("updateItem".equals(action) && itemId > 0) {
            String notes = request.getParameter("notes");
            if (notes != null && notes.trim().length() > 255) {
                WebUtil.flash(request, "Notes must be 255 characters or less.");
            } else {
                boolean ok = watchLaterService.updateWatchLaterItem(itemId, userId, folderId > 0 ? folderId : null, notes);
                WebUtil.flash(request, ok ? "Saved video updated." : "Could not update the saved video.");
            }
        } else if ("createFolder".equals(action)) {
            if (folderName != null && folderName.trim().length() > 100) {
                WebUtil.flash(request, "Folder name must be 100 characters or less.");
            } else {
                WebUtil.flash(request, watchLaterService.createFolder(userId, folderName)
                        ? "Folder created." : "Please enter a folder name.");
            }
        } else if ("renameFolder".equals(action) && folderId > 0) {
            if (folderName != null && folderName.trim().length() > 100) {
                WebUtil.flash(request, "Folder name must be 100 characters or less.");
            } else {
                WebUtil.flash(request, watchLaterService.renameFolder(folderId, userId, folderName)
                        ? "Folder renamed." : "Could not rename the folder.");
            }
        } else if ("deleteFolder".equals(action) && folderId > 0) {
            // videos inside the folder are kept (folder_id -> NULL, ON DELETE SET NULL)
            WebUtil.flash(request, watchLaterService.deleteFolder(folderId, userId)
                    ? "Folder deleted (its videos were kept in your list)." : "Could not delete the folder.");
        }

        // SMART REDIRECT: Return to the referring page if clicked from video details
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/video")) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(request.getContextPath() + "/user/watchlater"
                    + (WebUtil.parseInt(request.getParameter("returnFolder"), 0) > 0 ? "?folder=" + request.getParameter("returnFolder") : ""));
        }
    }
}

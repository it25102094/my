<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${video.title}"/> — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .video-embed-wrapper {
            position: relative;
            width: 100%;
            padding-top: 56.25%;
            border-radius: 12px;
            overflow: hidden;
            background-color: var(--color-surface-alt);
        }
        .video-embed-wrapper iframe, .spoiler-overlay {
            position: absolute;
            top: 0; left: 0; width: 100%; height: 100%; border: none;
        }
        .video-details-meta { margin-top: 20px; }
        .video-details-meta h1 { font-size: 1.5rem; margin-bottom: 8px; }
        .video-actions { margin-top: 20px; display: flex; gap: 10px; flex-wrap: wrap; }
        .video-actions form { display: inline-flex; gap: 8px; }
    </style>
</head>
<body>

<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <c:choose>
        <c:when test="${empty video}"><p>Video not found.</p></c:when>
        <c:otherwise>
            <%-- LIVE videos are exempt from Spoiler-Free Mode --%>
            <c:set var="locked" value="${sessionScope.spoilerFree == true && param.reveal != '1' && !isLive}"/>

            <div class="video-embed-wrapper">
                <c:choose>
                    <c:when test="${locked}">
                        <div class="spoiler-overlay">
                            <a class="reveal-btn" href="${pageContext.request.contextPath}/video?id=${video.videoId}&reveal=1">Reveal &amp; Watch</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <iframe src="${video.youTubeURL}"
                                title="Video player"
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                allowfullscreen></iframe>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="video-details-meta">
                <h1>
                    <c:choose>
                        <c:when test="${locked}">Result hidden to prevent spoilers</c:when>
                        <c:otherwise><c:out value="${video.title}"/></c:otherwise>
                    </c:choose>
                </h1>
                <div class="video-meta">${video.viewCount} views</div>
            </div>

            <!-- ========================================== -->
            <!-- MEMBER 4: LIKE, DISLIKE, & REPORT          -->
            <!-- ========================================== -->
            <div class="reactions-container" style="display: flex; gap: 10px; margin-top: 15px; margin-bottom: 20px; align-items: center;">
                <!-- Like Form -->
                <form action="${pageContext.request.contextPath}/reaction" method="post">
                    <input type="hidden" name="action" value="LIKE">
                    <input type="hidden" name="videoId" value="${video.videoId}">
                    <button type="submit"
                            style="padding: 8px 16px; border-radius: 20px; border: 1px solid var(--color-border); cursor: pointer; display: flex; align-items: center; gap: 8px; font-weight: bold;
                            ${userReaction == 'LIKE' ? 'background: var(--color-accent); color: #000;' : 'background: var(--color-surface); color: var(--color-title);'}">
                        👍 Like (${likesCount})
                    </button>
                </form>

                <!-- Dislike Form -->
                <form action="${pageContext.request.contextPath}/reaction" method="post">
                    <input type="hidden" name="action" value="DISLIKE">
                    <input type="hidden" name="videoId" value="${video.videoId}">
                    <button type="submit"
                            style="padding: 8px 16px; border-radius: 20px; border: 1px solid var(--color-border); cursor: pointer; display: flex; align-items: center; gap: 8px; font-weight: bold;
                            ${userReaction == 'DISLIKE' ? 'background: #EF4444; color: #FFF;' : 'background: var(--color-surface); color: var(--color-title);'}">
                        👎 Dislike (${dislikesCount})
                    </button>
                </form>

                <!-- Report Form -->
                <c:if test="${not empty sessionScope.userId}">
                    <form action="${pageContext.request.contextPath}/report" method="post" style="margin-left: auto;">
                        <input type="hidden" name="videoId" value="${video.videoId}">
                        <div style="display: flex; gap: 8px;">
                            <select name="reason" required style="padding: 8px; border-radius: 6px; border: 1px solid var(--color-border); background: var(--color-surface); color: var(--color-title);">
                                <option value="" disabled selected>Select issue...</option>
                                <option value="Inappropriate Content">Inappropriate Content</option>
                                <option value="Broken Video / Won't Play">Broken Video</option>
                                <option value="Spam or Misleading">Spam / Misleading</option>
                                <option value="Wrong Sport Category">Wrong Sport Category</option>
                            </select>
                            <button type="submit" class="btn-outline" style="border-color: #EF4444; color: #EF4444; padding: 8px 16px; border-radius: 6px;" onsubmit="return confirm('Report this video to admins?');">
                                🚩 Report
                            </button>
                        </div>
                    </form>
                </c:if>
            </div>

            <%-- Watch Later + Follow (logged-in users only) --%>
            <c:if test="${not empty sessionScope.userId}">
                <div class="video-actions">
                        <%-- Watch Later Form (Dynamic Active State) --%>
                    <form method="post" action="${pageContext.request.contextPath}/user/watchlater">
                        <input type="hidden" name="videoId" value="${video.videoId}">
                        <input type="hidden" name="action" value="${isSaved ? 'remove' : 'add'}">
                        <button class="${isSaved ? 'btn-outline' : 'btn-accent'}" type="submit">
                                ${isSaved ? '✓ Saved to Watch Later' : 'Save to Watch Later'}
                        </button>
                    </form>

                        <%-- Dynamic Teams Loop with Follow/Unfollow toggle --%>
                    <c:forEach var="team" items="${videoTeams}">
                        <c:set var="isFollowing" value="${followedTeamIds.contains(team.teamId)}" />
                        <form method="post" action="${pageContext.request.contextPath}/user/follow">
                            <input type="hidden" name="teamId" value="${team.teamId}">
                            <input type="hidden" name="videoId" value="${video.videoId}">
                            <input type="hidden" name="teamName" value="${team.teamName}">
                            <button type="submit" class="${isFollowing ? 'btn-accent' : 'btn-outline'}">
                                    ${isFollowing ? '✓ Following (Unfollow)' : 'Follow '.concat(team.teamName)}
                            </button>
                        </form>
                    </c:forEach>
                </div>
            </c:if>

            <%-- Notification Flash Banner --%>
            <c:if test="${not empty sessionScope.flashMessage}">
                <div class="form-success" style="margin-top: 15px; margin-bottom: 5px;">
                    <c:out value="${sessionScope.flashMessage}"/>
                </div>
                <% session.removeAttribute("flashMessage"); %>
            </c:if>

            <!-- ========================================== -->
            <!-- MEMBER 4: COMMENTS SECTION                 -->
            <!-- ========================================== -->
            <div class="comments-section" style="margin-top: 40px; padding-top: 20px; border-top: 1px solid var(--color-border);">
                <h3 style="margin-bottom: 20px;">Comments (${comments.size()})</h3>

                <c:if test="${not empty sessionScope.flashMessage}">
                    <div style="color: #EF4444; background: rgba(239,68,68,0.1); padding: 10px; border-radius: 6px; margin-bottom: 15px; font-size: 0.9rem;">
                        <c:out value="${sessionScope.flashMessage}"/>
                    </div>
                    <c:remove var="flashMessage" scope="session"/>
                </c:if>

                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <form action="${pageContext.request.contextPath}/comment" method="post" style="margin-bottom: 30px; display: flex; flex-direction: column; gap: 10px;">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="videoId" value="${video.videoId}">
                            <textarea name="commentText" rows="3" placeholder="Add a public comment..." required style="width: 100%; padding: 12px; border-radius: 8px; border: 1px solid var(--color-border); background: var(--color-surface); color: var(--color-title); font-family: inherit; resize: vertical;"></textarea>
                            <button type="submit" class="btn-accent" style="align-self: flex-end; padding: 8px 20px; border-radius: 6px;">Post Comment</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <p style="color: var(--color-text-muted); margin-bottom: 30px; background: var(--color-surface-alt); padding: 15px; border-radius: 8px;">
                            <a href="${pageContext.request.contextPath}/login" style="color: var(--color-accent); font-weight: bold; text-decoration: none;">Log in</a> to post a comment.
                        </p>
                    </c:otherwise>
                </c:choose>

                <div class="comments-list" style="display: flex; flex-direction: column; gap: 15px;">
                    <c:forEach var="c" items="${comments}">
                        <div class="comment-card" style="padding: 16px; background: var(--color-surface); border: 1px solid var(--color-border); border-radius: 8px;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                                <strong style="color: var(--color-title); font-size: 0.95rem;"><c:out value="${c.username}"/></strong>
                                <span style="font-size: 0.8rem; color: var(--color-text-muted);">${c.createdAt}</span>
                            </div>
                            <p style="margin-bottom: 12px; font-size: 0.95rem; color: var(--color-text); white-space: pre-wrap;"><c:out value="${c.commentText}"/></p>
                            <c:if test="${sessionScope.userId == c.userId}">
                                <form action="${pageContext.request.contextPath}/comment" method="post" onsubmit="return confirm('Are you sure you want to delete your comment?');">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="commentId" value="${c.commentId}">
                                    <input type="hidden" name="videoId" value="${video.videoId}">
                                    <button type="submit" style="background: none; border: none; color: #EF4444; font-size: 0.85rem; cursor: pointer; font-weight: 600; padding: 0;">Delete</button>
                                </form>
                            </c:if>
                        </div>
                    </c:forEach>
                    <c:if test="${empty comments}">
                        <p style="color: var(--color-text-muted); text-align: center; padding: 20px 0;">No comments yet. Be the first to share your thoughts!</p>
                    </c:if>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
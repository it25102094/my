<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
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
        .video-embed-wrapper iframe, .video-embed-wrapper .spoiler-overlay {
            position: absolute;
            top: 0; left: 0; width: 100%; height: 100%; border: none;
            display: flex; align-items: center; justify-content: center;
        }
        .video-details-meta { margin-top: 20px; }
        .video-details-meta h1 { font-size: 1.5rem; margin-bottom: 8px; }
        .video-actions { margin-top: 20px; display: flex; gap: 10px; flex-wrap: wrap; }
        .video-actions form { display: inline-flex; gap: 8px; }
        .reaction-btn {
            padding: 8px 16px; border-radius: 20px; border: 1px solid var(--color-border); cursor: pointer;
            display: flex; align-items: center; gap: 8px; font-weight: bold;
            background: var(--color-surface); color: var(--color-title);
        }
        .reaction-btn.on-like { background: var(--color-accent); color: #111827; }
        .reaction-btn.on-dislike { background: #EF4444; color: #FFFFFF; }
        .comment-card { padding: 16px; background: var(--color-surface); border: 1px solid var(--color-border); border-radius: 8px; }
        .comment-edit { display: none; margin-top: 10px; }
        .comment-edit.show { display: flex; flex-direction: column; gap: 8px; }
        textarea.comment-box { width: 100%; padding: 12px; border-radius: 8px; border: 1px solid var(--color-border); background: var(--color-surface); color: var(--color-title); font-family: inherit; resize: vertical; }
    </style>
</head>
<body>

<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <c:choose>
        <c:when test="${empty video}"><p>Video not found.</p></c:when>
        <c:otherwise>
            <%-- LIVE videos are exempt from Spoiler-Free Mode (FIX: used ${isLive}, which never existed) --%>
            <c:set var="locked" value="${sessionScope.spoilerFree == true && param.reveal != '1' && !video.isLive}"/>

            <c:if test="${previewMode}">
                <div class="preview-banner">&#128065; Preview only &mdash; this video is <strong>${video.status}</strong> and is not visible to the public yet.</div>
            </c:if>

            <%-- Notification Flash Banner (FIX: it was printed twice and removed by a scriptlet) --%>
            <%@ include file="includes/flash.jsp" %>

            <div class="watch-layout">
                <div>
                    <div class="video-embed-wrapper">
                        <c:choose>
                            <c:when test="${locked}">
                                <div class="spoiler-overlay">
                                    <a class="reveal-btn" href="${pageContext.request.contextPath}/video?id=${video.videoId}&reveal=1">Reveal &amp; Watch</a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <iframe src="<c:out value='${video.youTubeURL}'/>"
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
                        <div class="video-meta">
                            ${video.viewCount} views &bull; <c:out value="${video.sportName}"/> &bull; <c:out value="${video.categoryName}"/>
                            <c:if test="${not empty video.uploaderName}"> &bull; by <c:out value="${video.uploaderName}"/></c:if>
                            <c:if test="${not locked and not empty video.matchScoreText}"> &bull; <strong><c:out value="${video.matchScoreText}"/></strong></c:if>
                        </div>
                        <c:if test="${not locked and not empty video.description}">
                            <p style="margin-top:10px; white-space: pre-wrap;"><c:out value="${video.description}"/></p>
                        </c:if>
                    </div>

                    <!-- ========================================== -->
                    <!-- MEMBER 4: LIKE, DISLIKE, RATING & REPORT   -->
                    <!-- ========================================== -->
                    <div class="reactions-container" style="display: flex; gap: 10px; margin-top: 15px; margin-bottom: 10px; align-items: center; flex-wrap: wrap;">
                        <form action="${pageContext.request.contextPath}/reaction" method="post">
                            <input type="hidden" name="action" value="LIKE">
                            <input type="hidden" name="videoId" value="${video.videoId}">
                            <button type="submit" class="reaction-btn ${userReaction == 'LIKE' ? 'on-like' : ''}">&#128077; Like (${likesCount})</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/reaction" method="post">
                            <input type="hidden" name="action" value="DISLIKE">
                            <input type="hidden" name="videoId" value="${video.videoId}">
                            <button type="submit" class="reaction-btn ${userReaction == 'DISLIKE' ? 'on-dislike' : ''}">&#128078; Dislike (${dislikesCount})</button>
                        </form>

                        <!-- Rating (ratings table: create / update / delete) -->
                        <div style="display:flex; align-items:center; gap:8px; margin-left:6px;">
                            <span class="muted">&#11088; <fmt:formatNumber value="${avgRating}" minFractionDigits="1" maxFractionDigits="1"/> (${ratingCount})</span>
                            <c:if test="${not empty sessionScope.userId}">
                                <form action="${pageContext.request.contextPath}/reaction" method="post" class="rating-stars" title="Rate this video">
                                    <input type="hidden" name="action" value="RATE">
                                    <input type="hidden" name="videoId" value="${video.videoId}">
                                    <c:forEach var="star" begin="1" end="5">
                                        <button type="submit" name="rating" value="${star}" class="${star <= userRating ? 'on' : ''}" aria-label="${star} star">&#9733;</button>
                                    </c:forEach>
                                </form>
                                <c:if test="${userRating > 0}">
                                    <form action="${pageContext.request.contextPath}/reaction" method="post">
                                        <input type="hidden" name="action" value="UNRATE">
                                        <input type="hidden" name="videoId" value="${video.videoId}">
                                        <button type="submit" class="link-danger" title="Remove my rating">&times; remove</button>
                                    </form>
                                </c:if>
                            </c:if>
                        </div>

                        <!-- Report Form -->
                        <c:if test="${not empty sessionScope.userId}">
                            <form action="${pageContext.request.contextPath}/report" method="post" style="margin-left: auto;"
                                  onsubmit="return confirm('Report this video to admins?');">
                                <input type="hidden" name="videoId" value="${video.videoId}">
                                <div style="display: flex; gap: 8px;">
                                    <select name="reason" required style="padding: 8px; border-radius: 6px; border: 1px solid var(--color-border); background: var(--color-surface); color: var(--color-title);">
                                        <option value="" disabled selected>Select issue...</option>
                                        <option value="Inappropriate Content">Inappropriate Content</option>
                                        <option value="Broken Video / Won't Play">Broken Video</option>
                                        <option value="Spam or Misleading">Spam / Misleading</option>
                                        <option value="Wrong Sport Category">Wrong Sport Category</option>
                                    </select>
                                    <button type="submit" class="btn-outline" style="border-color: #EF4444; color: #EF4444; padding: 8px 16px; border-radius: 6px;">&#128681; Report</button>
                                </div>
                            </form>
                        </c:if>
                    </div>

                    <%-- Watch Later + Follow (logged-in users only) --%>
                    <c:choose>
                        <c:when test="${not empty sessionScope.userId}">
                            <div class="video-actions">
                                <%-- Watch Later Form (Dynamic Active State) --%>
                                <form method="post" action="${pageContext.request.contextPath}/user/watchlater">
                                    <input type="hidden" name="videoId" value="${video.videoId}">
                                    <input type="hidden" name="action" value="${isSaved ? 'remove' : 'add'}">
                                    <button class="${isSaved ? 'btn-outline' : 'btn-accent'}" type="submit">
                                            ${isSaved ? '&#10003; Saved to Watch Later (remove)' : '+ Save to Watch Later'}
                                    </button>
                                </form>

                                <%-- Teams of this video with Follow/Unfollow toggle (Member 1) -> Observer pattern (Member 5) --%>
                                <c:forEach var="team" items="${videoTeams}">
                                    <c:set var="isFollowing" value="${followedTeamIds.contains(team.teamId)}" />
                                    <form method="post" action="${pageContext.request.contextPath}/user/follow">
                                        <input type="hidden" name="teamId" value="${team.teamId}">
                                        <input type="hidden" name="videoId" value="${video.videoId}">
                                        <input type="hidden" name="teamName" value="<c:out value='${team.teamName}'/>">
                                        <input type="hidden" name="action" value="${isFollowing ? 'unfollow' : 'follow'}">
                                        <button type="submit" class="${isFollowing ? 'btn-accent' : 'btn-outline'}">
                                            <c:choose>
                                                <c:when test="${isFollowing}">&#10003; Following <c:out value="${team.teamName}"/> (Unfollow)</c:when>
                                                <c:otherwise>&#128276; Follow <c:out value="${team.teamName}"/></c:otherwise>
                                            </c:choose>
                                        </button>
                                    </form>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${not empty videoTeams}">
                                <p class="muted" style="margin-top:14px;"><a href="${pageContext.request.contextPath}/login" style="color: var(--color-accent-readable); font-weight: bold;">Log in</a>
                                    to follow
                                    <c:forEach var="team" items="${videoTeams}" varStatus="st"><c:out value="${team.teamName}"/>${st.last ? '' : ' / '}</c:forEach>
                                    and get notified about new videos.</p>
                            </c:if>
                        </c:otherwise>
                    </c:choose>

                    <!-- ========================================== -->
                    <!-- MEMBER 4: COMMENTS SECTION (CRUD)          -->
                    <!-- ========================================== -->
                    <div class="comments-section" id="comments" style="margin-top: 40px; padding-top: 20px; border-top: 1px solid var(--color-border);">
                        <h3 style="margin-bottom: 20px;">Comments (${comments.size()})</h3>

                        <c:choose>
                            <c:when test="${not empty sessionScope.userId}">
                                <form action="${pageContext.request.contextPath}/comment" method="post" style="margin-bottom: 30px; display: flex; flex-direction: column; gap: 10px;">
                                    <input type="hidden" name="action" value="add">
                                    <input type="hidden" name="videoId" value="${video.videoId}">
                                    <textarea name="commentText" rows="3" maxlength="500" class="comment-box" placeholder="Add a public comment..." required></textarea>
                                    <button type="submit" class="btn-accent" style="align-self: flex-end; padding: 8px 20px; border-radius: 6px;">Post Comment</button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <p style="color: var(--color-text-muted); margin-bottom: 30px; background: var(--color-surface-alt); padding: 15px; border-radius: 8px;">
                                    <a href="${pageContext.request.contextPath}/login" style="color: var(--color-accent-readable); font-weight: bold; text-decoration: none;">Log in</a> to post a comment.
                                </p>
                            </c:otherwise>
                        </c:choose>

                        <div class="comments-list" style="display: flex; flex-direction: column; gap: 15px;">
                            <c:forEach var="c" items="${comments}">
                                <div class="comment-card">
                                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                                        <strong style="color: var(--color-title); font-size: 0.95rem;"><c:out value="${c.username}"/></strong>
                                        <span style="font-size: 0.8rem; color: var(--color-text-muted);">
                                            <fmt:formatDate value="${c.createdAt}" pattern="dd MMM yyyy, hh:mm a"/><c:if test="${c.edited}"> (edited)</c:if>
                                        </span>
                                    </div>
                                    <p style="margin-bottom: 12px; font-size: 0.95rem; color: var(--color-text); white-space: pre-wrap;"><c:out value="${c.commentText}"/></p>
                                    <c:if test="${sessionScope.userId == c.userId}">
                                        <div style="display:flex; gap:14px;">
                                            <button type="button" class="link-plain" onclick="document.getElementById('edit-${c.commentId}').classList.toggle('show')">Edit</button>
                                            <form action="${pageContext.request.contextPath}/comment" method="post" onsubmit="return confirm('Are you sure you want to delete your comment?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="commentId" value="${c.commentId}">
                                                <input type="hidden" name="videoId" value="${video.videoId}">
                                                <button type="submit" class="link-danger">Delete</button>
                                            </form>
                                        </div>
                                        <form id="edit-${c.commentId}" class="comment-edit" action="${pageContext.request.contextPath}/comment" method="post">
                                            <input type="hidden" name="action" value="update">
                                            <input type="hidden" name="commentId" value="${c.commentId}">
                                            <input type="hidden" name="videoId" value="${video.videoId}">
                                            <textarea name="commentText" rows="3" maxlength="500" class="comment-box" required><c:out value="${c.commentText}"/></textarea>
                                            <button type="submit" class="btn-accent btn-sm" style="align-self:flex-end;">Save changes</button>
                                        </form>
                                    </c:if>
                                </div>
                            </c:forEach>
                            <c:if test="${empty comments}">
                                <p style="color: var(--color-text-muted); text-align: center; padding: 20px 0;">No comments yet. Be the first to share your thoughts!</p>
                            </c:if>
                        </div>
                    </div>
                </div>

                <!-- ========================================== -->
                <!-- MEMBER 5: SPONSORED SIDEBAR (ads)          -->
                <!-- ========================================== -->
                <aside class="watch-sidebar">
                    <c:if test="${not empty sidebarAds}">
                        <div class="sidebar-title">Sponsored</div>
                        <c:forEach var="ad" items="${sidebarAds}">
                            <%@ include file="includes/ad-card.jsp" %>
                        </c:forEach>
                    </c:if>
                </aside>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="includes/footer.jsp" %>

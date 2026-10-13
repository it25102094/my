<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <div class="auth-card" style="max-width: 520px;">
        <h2>My Profile</h2>

        <c:if test="${param.updated == 'true'}">
            <p style="color: var(--color-accent); font-size: 0.9rem;">Profile updated successfully!</p>
        </c:if>
        <c:if test="${param.pwdChanged == 'true'}">
            <p style="color: var(--color-accent); font-size: 0.9rem;">Password changed successfully!</p>
        </c:if>

        <p class="video-meta"><strong>Username:</strong> <c:out value="${user.username}"/></p>
        <p class="video-meta"><strong>First Name:</strong> <c:out value="${user.firstName}"/></p>
        <p class="video-meta"><strong>Last Name:</strong> <c:out value="${user.lastName}"/></p>
        <p class="video-meta"><strong>Email:</strong> <c:out value="${user.email}"/></p>
        <p class="video-meta"><strong>Contact Number:</strong> <c:out value="${not empty user.contactNumber ? user.contactNumber : 'Not set'}"/></p>
        <p class="video-meta"><strong>Favourite Sports:</strong> <c:out value="${not empty user.favouriteSports ? user.favouriteSports : 'Not set'}"/></p>
        <p class="video-meta"><strong>Hide Scores:</strong> ${user.hideScores ? 'Yes (Scores Hidden)' : 'No (Scores Visible)'}</p>
        <p class="video-meta"><strong>Role:</strong> <c:out value="${user.role}"/></p>

        <hr style="border: none; border-top: 1px solid var(--color-border); margin: 8px 0;">

        <h3>Followed Teams</h3>
        <c:choose>
            <c:when test="${not empty followedTeams}">
                <c:forEach var="ft" items="${followedTeams}">
                    <div style="display:flex; justify-content:space-between; align-items:center; padding: 6px 0;">
                        <span class="video-meta"><c:out value="${ft.teamName}"/> (<c:out value="${ft.sportName}"/>)</span>
                        <form method="post" action="${pageContext.request.contextPath}/user/follow" style="display:inline;">
                            <input type="hidden" name="action" value="unfollow">
                            <input type="hidden" name="teamId" value="${ft.teamId}">
                            <button type="submit" class="btn-outline" style="padding: 4px 10px; font-size: 0.8rem;">Unfollow</button>
                        </form>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <p class="video-meta">No teams followed yet. Open any match video to follow a team.</p>
            </c:otherwise>
        </c:choose>

        <div class="video-actions">
            <a href="${pageContext.request.contextPath}/user/edit-profile" class="btn-accent">Edit Profile</a>
            <a href="${pageContext.request.contextPath}/user/watchlater" class="btn-outline">Watch Later</a>
        </div>
    </div>
</section>

<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
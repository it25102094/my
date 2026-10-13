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
    <div class="auth-card" style="max-width: 640px;">
        <h2>My Profile</h2>

        <%@ include file="includes/flash.jsp" %>
        <c:if test="${param.updated == 'true'}">
            <p class="flash flash-success">Profile updated successfully!</p>
        </c:if>
        <c:if test="${param.pwdChanged == 'true'}">
            <p class="flash flash-success">Password changed successfully!</p>
        </c:if>
        <c:if test="${param.error == 'deleteFailed'}">
            <p class="flash flash-error">Your account could not be deleted. Please try again.</p>
        </c:if>

        <p class="video-meta"><strong>Username:</strong> <c:out value="${user.username}"/></p>
        <p class="video-meta"><strong>First Name:</strong> <c:out value="${user.firstName}"/></p>
        <p class="video-meta"><strong>Last Name:</strong> <c:out value="${user.lastName}"/></p>
        <p class="video-meta"><strong>Email:</strong> <c:out value="${user.email}"/></p>
        <p class="video-meta"><strong>Contact Number:</strong> <c:out value="${not empty user.contactNumber ? user.contactNumber : 'Not set (needed for password reset)'}"/></p>
        <p class="video-meta"><strong>Favourite Sports:</strong> <c:out value="${not empty user.favouriteSports ? user.favouriteSports : 'Not set'}"/></p>
        <p class="video-meta"><strong>Hide Scores:</strong> ${user.hideScores ? 'Yes (Scores Hidden)' : 'No (Scores Visible)'}</p>
        <p class="video-meta"><strong>Role:</strong> <c:out value="${user.role}"/></p>
        <c:if test="${user.role == 'ADVERTISER'}">
            <p class="video-meta"><strong>Company:</strong> <c:out value="${user.companyName}"/></p>
        </c:if>

        <div class="video-actions">
            <a href="${pageContext.request.contextPath}/user/edit-profile" class="btn-accent">Edit Profile</a>
            <a href="${pageContext.request.contextPath}/user/watchlater" class="btn-outline">Watch Later</a>
            <a href="${pageContext.request.contextPath}/notifications" class="btn-outline">&#128276; Notifications</a>
            <c:if test="${user.role == 'ADVERTISER' || user.role == 'ADMIN'}">
                <a href="${pageContext.request.contextPath}/advertiser/dashboard" class="btn-outline">&#128226; Advertiser Dashboard</a>
            </c:if>
        </div>

        <hr style="border: none; border-top: 1px solid var(--color-border); margin: 8px 0;">

        <!-- Member 1: follow_teams CRUD. Followers with alerts ON are notified by Member 5's Observer pattern -->
        <h3 id="teams">Followed Teams</h3>
        <c:choose>
            <c:when test="${not empty followedTeams}">
                <c:forEach var="ft" items="${followedTeams}">
                    <div class="team-row">
                        <span class="video-meta"><strong><c:out value="${ft.teamName}"/></strong> (<c:out value="${ft.sportName}"/>)
                            <span class="badge ${ft.notifyOnUpload ? 'badge-green' : 'badge-gray'}">${ft.notifyOnUpload ? 'alerts on' : 'alerts off'}</span>
                        </span>
                        <span style="display:flex; gap:8px;">
                            <form method="post" action="${pageContext.request.contextPath}/user/follow" style="display:inline;">
                                <input type="hidden" name="action" value="${ft.notifyOnUpload ? 'notifyOff' : 'notifyOn'}">
                                <input type="hidden" name="teamId" value="${ft.teamId}">
                                <input type="hidden" name="teamName" value="<c:out value='${ft.teamName}'/>">
                                <input type="hidden" name="from" value="profile">
                                <button type="submit" class="btn-outline btn-sm">${ft.notifyOnUpload ? '&#128277; Mute alerts' : '&#128276; Alerts on'}</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/user/follow" style="display:inline;">
                                <input type="hidden" name="action" value="unfollow">
                                <input type="hidden" name="teamId" value="${ft.teamId}">
                                <input type="hidden" name="teamName" value="<c:out value='${ft.teamName}'/>">
                                <input type="hidden" name="from" value="profile">
                                <button type="submit" class="btn-outline btn-sm">Unfollow</button>
                            </form>
                        </span>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <p class="video-meta">No teams followed yet. Follow a team below (or from any match video).</p>
            </c:otherwise>
        </c:choose>

        <h3>Discover Teams</h3>
        <c:forEach var="t" items="${allTeams}">
            <c:if test="${not t.followedByCurrentUser}">
                <div class="team-row">
                    <span class="video-meta"><strong><c:out value="${t.teamName}"/></strong> (<c:out value="${t.sportName}"/><c:if test="${not empty t.country}">, <c:out value="${t.country}"/></c:if>)</span>
                    <form method="post" action="${pageContext.request.contextPath}/user/follow" style="display:inline;">
                        <input type="hidden" name="action" value="follow">
                        <input type="hidden" name="teamId" value="${t.teamId}">
                        <input type="hidden" name="teamName" value="<c:out value='${t.teamName}'/>">
                        <input type="hidden" name="from" value="profile">
                        <button type="submit" class="btn-accent btn-sm">+ Follow</button>
                    </form>
                </div>
            </c:if>
        </c:forEach>
    </div>
</section>

<%@ include file="includes/footer.jsp" %>

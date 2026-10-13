<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- =====================================================================================
     Member 5 (IT25102094) - Notification Management : user inbox
     (This view was missing - NotificationServlet forwarded to it and crashed with HTTP 500.)
       READ   : list (All / Unread tabs) + unread count
       UPDATE : open (marks read), mark read / unread, mark all as read
       DELETE : delete one, delete all read
     Notifications are CREATED automatically (Observer pattern when a followed team posts a
     video, Factory pattern for moderation results) or by an admin broadcast.
     ===================================================================================== --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notifications — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="includes/navbar.jsp" %>

<section class="page-section" style="max-width: 900px;">
    <div class="dash-header">
        <div>
            <h1>&#128276; Notifications</h1>
            <div class="sub">You have <strong>${unreadTotal}</strong> unread notification(s).</div>
        </div>
        <div class="actions" style="display:flex; gap:8px; flex-wrap:wrap;">
            <form method="post" action="${pageContext.request.contextPath}/notifications">
                <input type="hidden" name="action" value="markAllRead">
                <button type="submit" class="btn-outline btn-sm" ${unreadTotal == 0 ? 'disabled' : ''}>&#10003; Mark all as read</button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/notifications"
                  onsubmit="return confirm('Delete all notifications you have already read?');">
                <input type="hidden" name="action" value="clearRead">
                <button type="submit" class="btn-outline btn-sm">&#128465; Delete read</button>
            </form>
        </div>
    </div>

    <%@ include file="includes/flash.jsp" %>

    <div class="tabs" style="margin-bottom:16px;">
        <a href="${pageContext.request.contextPath}/notifications" class="${unreadOnly ? '' : 'active'}">All</a>
        <a href="${pageContext.request.contextPath}/notifications?filter=unread" class="${unreadOnly ? 'active' : ''}">Unread (${unreadTotal})</a>
    </div>

    <c:choose>
        <c:when test="${empty notifications}">
            <div class="empty-state">
                <div class="big">&#128237;</div>
                <p>${unreadOnly ? 'No unread notifications.' : 'No notifications yet.'}</p>
                <p style="margin-top:6px;">Follow your favourite teams on your <a href="${pageContext.request.contextPath}/profile#teams" style="color:var(--color-accent-readable)">profile</a>
                    and you will be notified when they post a new video.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="notif-list">
                <c:forEach var="n" items="${notifications}">
                    <div class="notif-item ${n.read ? '' : 'unread'}">
                        <div class="notif-icon">${n.icon}</div>
                        <div>
                            <h4><c:out value="${n.title}"/></h4>
                            <p><c:out value="${n.message}"/></p>
                            <div class="notif-meta">
                                <span><fmt:formatDate value="${n.createdAt}" pattern="dd MMM yyyy, hh:mm a"/></span>
                                <span class="badge badge-gray">${n.notificationType}</span>
                                <c:if test="${not n.read}"><span class="badge badge-green">NEW</span></c:if>
                            </div>
                        </div>
                        <div class="notif-actions">
                            <c:if test="${not empty n.targetLink}">
                                <form method="post" action="${pageContext.request.contextPath}/notifications">
                                    <input type="hidden" name="action" value="open">
                                    <input type="hidden" name="notificationId" value="${n.notificationId}">
                                    <input type="hidden" name="link" value="<c:out value='${n.targetLink}'/>">
                                    <button type="submit" class="btn-accent btn-sm">Open</button>
                                </form>
                            </c:if>
                            <form method="post" action="${pageContext.request.contextPath}/notifications">
                                <input type="hidden" name="action" value="${n.read ? 'markUnread' : 'markRead'}">
                                <input type="hidden" name="notificationId" value="${n.notificationId}">
                                <c:if test="${unreadOnly}"><input type="hidden" name="filter" value="unread"></c:if>
                                <button type="submit" class="link-plain">${n.read ? 'Mark unread' : 'Mark read'}</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/notifications"
                                  onsubmit="return confirm('Delete this notification?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="notificationId" value="${n.notificationId}">
                                <c:if test="${unreadOnly}"><input type="hidden" name="filter" value="unread"></c:if>
                                <button type="submit" class="link-danger">Delete</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="includes/footer.jsp" %>

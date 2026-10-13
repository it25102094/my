<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- =====================================================================================
     Member 6 - Reporting, Analytics & Admin Management : Admin Dashboard
     FIXES: all user supplied values are now escaped with c:out (the old page printed titles,
     reasons and flash messages raw = stored XSS), the light-only colours were replaced by the
     theme variables, and the missing admin features were added:
       moderation (videos & reports), user management, advertising oversight (Member 5),
       announcements (Member 5), saved analytics reports (CRUD), activity log, CSV export.
     ===================================================================================== --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .admin-nav { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }
        .admin-nav a { font-size: 0.82rem; padding: 6px 12px; border-radius: 999px; border: 1px solid var(--color-border); background: var(--color-surface); color: var(--color-title); font-weight: 600; }
        .admin-nav a:hover { border-color: var(--color-accent); }
        .count-pill { background: var(--color-live); color: #fff; border-radius: 999px; padding: 0 7px; font-size: 0.72rem; margin-left: 4px; }
    </style>
</head>
<body>
<%@ include file="includes/navbar.jsp" %>

<section class="page-section">

    <div class="dash-header">
        <div>
            <h1>&#128737; Admin Dashboard</h1>
            <div class="sub">Moderate content, manage users and advertising, broadcast announcements and save analytics reports.</div>
        </div>
        <a href="${pageContext.request.contextPath}/admin/export-csv" class="btn-accent btn-sm">&#11015; Export Analytics CSV</a>
    </div>

    <div class="admin-nav">
        <a href="#moderation">Pending videos<c:if test="${not empty pendingVideos}"><span class="count-pill">${pendingVideos.size()}</span></c:if></a>
        <a href="#reports">Reports<c:if test="${not empty pendingReports}"><span class="count-pill">${pendingReports.size()}</span></c:if></a>
        <a href="#users">Users</a>
        <a href="#advertising">Advertising</a>
        <a href="#notifications">Announcements</a>
        <a href="#saved-reports">Saved reports</a>
        <a href="#logs">Activity log</a>
    </div>

    <!-- Displays success/error messages after actions -->
    <%@ include file="includes/flash.jsp" %>

    <!-- ===================== SYSTEM ANALYTICS ===================== -->
    <h2 class="section-title" style="margin-top: 10px;">System Analytics</h2>
    <div class="stats-grid">
        <div class="stat-card"><div class="stat-label">Total users</div><div class="stat-value">${systemStats.totalUsers}</div></div>
        <div class="stat-card"><div class="stat-label">Approved videos</div><div class="stat-value">${systemStats.totalVideos}</div></div>
        <div class="stat-card"><div class="stat-label">Pending videos</div><div class="stat-value">${systemStats.pendingVideos}</div></div>
        <div class="stat-card"><div class="stat-label">Pending reports</div><div class="stat-value">${systemStats.pendingReports}</div></div>
        <div class="stat-card"><div class="stat-label">Total views</div><div class="stat-value">${systemStats.totalViews}</div></div>
        <div class="stat-card"><div class="stat-label">Comments</div><div class="stat-value">${systemStats.totalComments}</div></div>
        <div class="stat-card"><div class="stat-label">Running campaigns</div><div class="stat-value">${systemStats.activeCampaigns}</div><div class="stat-hint">${systemStats.totalAds} ads in total</div></div>
        <div class="stat-card"><div class="stat-label">Ad clicks</div><div class="stat-value">${systemStats.adClicks}</div><div class="stat-hint">${systemStats.adImpressions} impressions</div></div>
    </div>

    <!-- ===================== PENDING VIDEOS ===================== -->
    <h2 class="section-title" id="moderation">Pending Video Submissions <small>approving notifies the uploader and the team followers</small></h2>
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>Title</th><th>Uploader</th><th>Sport / Type</th><th>Submitted</th><th>Actions</th></tr></thead>
            <tbody>
            <c:forEach var="v" items="${pendingVideos}">
                <tr>
                    <td><a href="${pageContext.request.contextPath}/video?id=${v.videoId}" target="_blank"><c:out value="${v.title}"/></a>
                        <div class="muted"><a href="<c:out value='${v.youtubeUrl}'/>" target="_blank" rel="noopener">YouTube link</a></div></td>
                    <td><c:out value="${v.uploader}"/></td>
                    <td><c:out value="${v.sportName}"/> / <c:out value="${v.videoType}"/></td>
                    <td class="muted"><fmt:formatDate value="${v.createdAt}" pattern="dd MMM yyyy HH:mm"/></td>
                    <td>
                        <div class="actions">
                            <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form">
                                <input type="hidden" name="action" value="updateVideoStatus">
                                <input type="hidden" name="videoId" value="${v.videoId}">
                                <input type="hidden" name="status" value="APPROVED">
                                <button type="submit" class="btn-success btn-sm">Approve</button>
                            </form>
                            <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form">
                                <input type="hidden" name="action" value="updateVideoStatus">
                                <input type="hidden" name="videoId" value="${v.videoId}">
                                <input type="hidden" name="status" value="REJECTED">
                                <button type="submit" class="btn-danger btn-sm">Reject</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty pendingVideos}">
                <tr><td colspan="5" class="empty">No pending video submissions.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <!-- ===================== PENDING REPORTS ===================== -->
    <h2 class="section-title" id="reports">Pending Content Reports <small>the reporter is notified of the decision</small></h2>
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>Reported content</th><th>Reason</th><th>Reporter</th><th>Date</th><th>Decision</th></tr></thead>
            <tbody>
            <c:forEach var="r" items="${pendingReports}">
                <tr>
                    <td><c:choose>
                        <c:when test="${r.videoId > 0}"><a href="${pageContext.request.contextPath}/video?id=${r.videoId}" target="_blank"><c:out value="${r.videoTitle}"/></a></c:when>
                        <c:otherwise><c:out value="${r.videoTitle}"/></c:otherwise>
                    </c:choose></td>
                    <td><c:out value="${r.reason}"/></td>
                    <td><c:out value="${r.reporter}"/></td>
                    <td class="muted"><fmt:formatDate value="${r.createdAt}" pattern="dd MMM yyyy"/></td>
                    <td>
                        <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form" style="flex-wrap:wrap;">
                            <input type="hidden" name="action" value="updateReportStatus">
                            <input type="hidden" name="reportId" value="${r.reportId}">
                            <input type="text" name="notes" placeholder="Resolution notes (optional)" maxlength="500">
                            <button type="submit" name="status" value="RESOLVED" class="btn-success btn-sm">Resolve</button>
                            <button type="submit" name="status" value="DISMISSED" class="btn-muted btn-sm">Dismiss</button>
                        </form>
                        <c:if test="${r.videoId > 0}">
                            <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form"
                                  onsubmit="return confirm('Permanently remove this video?');">
                                <input type="hidden" name="action" value="removeReportedVideo">
                                <input type="hidden" name="reportId" value="${r.reportId}">
                                <button type="submit" class="link-danger">Remove video</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty pendingReports}">
                <tr><td colspan="5" class="empty">No pending content reports.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <!-- ===================== USER MANAGEMENT ===================== -->
    <h2 class="section-title" id="users">User Management</h2>
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>#</th><th>User</th><th>Email</th><th>Role</th><th>Status</th><th>Joined</th><th>Actions</th></tr></thead>
            <tbody>
            <c:forEach var="u" items="${users}">
                <c:set var="isMe" value="${u.userId == sessionScope.userId}"/>
                <tr>
                    <td class="muted">${u.userId}</td>
                    <td><strong><c:out value="${u.username}"/></strong><div class="muted"><c:out value="${u.fullName}"/><c:if test="${not empty u.companyName}"> &middot; <c:out value="${u.companyName}"/></c:if></div></td>
                    <td><c:out value="${u.email}"/></td>
                    <td>
                        <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form">
                            <input type="hidden" name="action" value="changeRole">
                            <input type="hidden" name="userId" value="${u.userId}">
                            <select name="role" ${isMe ? 'disabled' : ''}>
                                <option value="USER" ${u.role == 'USER' ? 'selected' : ''}>USER</option>
                                <option value="CONTENT_MANAGER" ${u.role == 'CONTENT_MANAGER' ? 'selected' : ''}>CONTENT_MANAGER</option>
                                <option value="ADVERTISER" ${u.role == 'ADVERTISER' ? 'selected' : ''}>ADVERTISER</option>
                                <option value="ADMIN" ${u.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                            </select>
                            <c:if test="${not isMe}"><button type="submit" class="btn-outline btn-sm">Save</button></c:if>
                        </form>
                    </td>
                    <td><span class="badge ${u.status == 'ACTIVE' ? 'badge-green' : 'badge-red'}">${u.status}</span></td>
                    <td class="muted"><fmt:formatDate value="${u.createdAt}" pattern="dd MMM yyyy"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${isMe}"><span class="muted">(you)</span></c:when>
                            <c:otherwise>
                                <div class="actions">
                                    <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form">
                                        <input type="hidden" name="action" value="changeStatus">
                                        <input type="hidden" name="userId" value="${u.userId}">
                                        <input type="hidden" name="status" value="${u.status == 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE'}">
                                        <button type="submit" class="${u.status == 'ACTIVE' ? 'btn-warning' : 'btn-success'} btn-sm">${u.status == 'ACTIVE' ? 'Suspend' : 'Activate'}</button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form"
                                          onsubmit="return confirm('Delete this user and ALL of their data?');">
                                        <input type="hidden" name="action" value="deleteUser">
                                        <input type="hidden" name="userId" value="${u.userId}">
                                        <button type="submit" class="btn-danger btn-sm">Delete</button>
                                    </form>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <!-- ===================== ADVERTISING OVERSIGHT (Member 5) ===================== -->
    <h2 class="section-title" id="advertising">
        <span>Advertising Campaigns <small>all advertisers</small></span>
        <a href="${pageContext.request.contextPath}/advertiser/dashboard" class="btn-outline btn-sm">Open full ad manager &rarr;</a>
    </h2>
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>Campaign</th><th>Advertiser</th><th>Dates</th><th>State</th><th>Ads</th><th>Impr. / Clicks</th><th>Actions</th></tr></thead>
            <tbody>
            <c:forEach var="cmp" items="${campaigns}">
                <tr>
                    <td><strong><c:out value="${cmp.campaignName}"/></strong><div class="muted">${cmp.priorityLabel} priority &middot; LKR <fmt:formatNumber value="${cmp.budget}" maxFractionDigits="0"/></div></td>
                    <td><c:out value="${cmp.companyName != null ? cmp.companyName : cmp.advertiserName}"/></td>
                    <td class="muted">${cmp.startDate} &rarr; ${cmp.endDate}</td>
                    <td><c:set var="rs" value="${cmp.runState}"/><span class="badge ${rs == 'RUNNING' ? 'badge-green' : (rs == 'PAUSED' || rs == 'SCHEDULED' ? 'badge-yellow' : 'badge-gray')}">${rs}</span></td>
                    <td>${cmp.adCount}</td>
                    <td class="muted">${cmp.totalImpressions} / ${cmp.totalClicks}</td>
                    <td>
                        <div class="actions">
                            <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form">
                                <input type="hidden" name="action" value="campaignStatus">
                                <input type="hidden" name="campaignId" value="${cmp.campaignId}">
                                <input type="hidden" name="status" value="${cmp.status == 'ACTIVE' ? 'PAUSED' : 'ACTIVE'}">
                                <button type="submit" class="${cmp.status == 'ACTIVE' ? 'btn-warning' : 'btn-success'} btn-sm">${cmp.status == 'ACTIVE' ? 'Pause' : 'Resume'}</button>
                            </form>
                            <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form"
                                  onsubmit="return confirm('Delete this campaign and all of its ads?');">
                                <input type="hidden" name="action" value="deleteCampaign">
                                <input type="hidden" name="campaignId" value="${cmp.campaignId}">
                                <button type="submit" class="btn-danger btn-sm">Delete</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty campaigns}">
                <tr><td colspan="7" class="empty">No advertising campaigns yet.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <!-- ===================== ANNOUNCEMENTS (Member 5 notifications) ===================== -->
    <h2 class="section-title" id="notifications">Send a Notification <small>appears in every selected user's notification inbox</small></h2>
    <div class="card">
        <form action="${pageContext.request.contextPath}/admin/notifications" method="post">
            <input type="hidden" name="action" value="broadcast">
            <div class="form-grid">
                <div class="form-field">
                    <label for="ntype">Type</label>
                    <select id="ntype" name="type">
                        <option value="ANNOUNCEMENT">Announcement</option>
                        <option value="PROMOTION">Promotion</option>
                    </select>
                </div>
                <div class="form-field">
                    <label for="audience">Send to</label>
                    <select id="audience" name="audience">
                        <option value="ALL">All active users</option>
                        <option value="USER">Viewers only</option>
                        <option value="ADVERTISER">Advertisers only</option>
                        <option value="CONTENT_MANAGER">Content managers only</option>
                        <option value="ADMIN">Admins only</option>
                    </select>
                </div>
                <div class="form-field">
                    <label for="nlink">Link (optional)</label>
                    <input type="text" id="nlink" name="link" maxlength="255" placeholder="/live or /video?id=1">
                </div>
                <div class="form-field full">
                    <label for="ntitle">Title <span class="required">*</span></label>
                    <input type="text" id="ntitle" name="title" required maxlength="100" placeholder="e.g. Asia Cup final is LIVE tonight!">
                </div>
                <div class="form-field full">
                    <label for="nmessage">Message <span class="required">*</span></label>
                    <textarea id="nmessage" name="message" required maxlength="300" placeholder="Short message (max 300 characters)"></textarea>
                </div>
            </div>
            <div class="form-actions"><button type="submit" class="btn-accent">Send notification</button></div>
        </form>
    </div>
    <div class="table-wrap" style="margin-top:14px;">
        <table class="data-table">
            <thead><tr><th>Recent announcements</th><th>Sent</th><th>Recipients</th><th>Read</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="a" items="${announcements}">
                <tr>
                    <td><strong><c:out value="${a.title}"/></strong><div class="muted"><c:out value="${a.message}"/></div></td>
                    <td class="muted"><fmt:formatDate value="${a.sentAt}" pattern="dd MMM yyyy HH:mm"/></td>
                    <td>${a.recipients}</td>
                    <td>${a.readCount}</td>
                    <td>
                        <form action="${pageContext.request.contextPath}/admin/notifications" method="post" class="inline-form"
                              onsubmit="return confirm('Remove this announcement from every inbox?');">
                            <input type="hidden" name="action" value="recall">
                            <input type="hidden" name="title" value="<c:out value='${a.title}'/>">
                            <input type="hidden" name="message" value="<c:out value='${a.message}'/>">
                            <button type="submit" class="link-danger">Recall</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty announcements}">
                <tr><td colspan="5" class="empty">No announcements sent yet.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <!-- ===================== SAVED REPORTS (CRUD) ===================== -->
    <h2 class="section-title" id="saved-reports">Saved Analytics Reports</h2>
    <div class="card">
        <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form" style="width:100%;">
            <input type="hidden" name="action" value="saveReport">
            <input type="text" name="reportTitle" maxlength="150" placeholder="Report title (optional), e.g. October week 2 summary" style="flex:1;">
            <button type="submit" class="btn-accent btn-sm">&#128190; Save current analytics snapshot</button>
        </form>
    </div>
    <div class="table-wrap" style="margin-top:14px;">
        <table class="data-table">
            <thead><tr><th>Title</th><th>Summary</th><th>Saved by</th><th>Date</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="sr" items="${savedReports}">
                <tr>
                    <td>
                        <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form">
                            <input type="hidden" name="action" value="renameReport">
                            <input type="hidden" name="reportId" value="${sr.savedReportId}">
                            <input type="text" name="reportTitle" value="<c:out value='${sr.reportTitle}'/>" maxlength="150" required>
                            <button type="submit" class="btn-outline btn-sm">Rename</button>
                        </form>
                        <div class="muted">${sr.reportType}</div>
                    </td>
                    <td class="muted" style="max-width:420px;"><c:out value="${sr.summaryContent}"/></td>
                    <td><c:out value="${sr.adminName}"/></td>
                    <td class="muted"><fmt:formatDate value="${sr.createdAt}" pattern="dd MMM yyyy HH:mm"/></td>
                    <td>
                        <form action="${pageContext.request.contextPath}/admin" method="post" class="inline-form"
                              onsubmit="return confirm('Delete this saved report?');">
                            <input type="hidden" name="action" value="deleteReport">
                            <input type="hidden" name="reportId" value="${sr.savedReportId}">
                            <button type="submit" class="link-danger">Delete</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty savedReports}">
                <tr><td colspan="5" class="empty">No saved reports yet.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <!-- ===================== ACTIVITY LOG ===================== -->
    <h2 class="section-title" id="logs">Admin Activity Log <small>last 15 actions</small></h2>
    <div class="table-wrap">
        <table class="data-table">
            <thead><tr><th>When</th><th>Admin</th><th>Action</th><th>Target</th><th>Description</th></tr></thead>
            <tbody>
            <c:forEach var="l" items="${adminLogs}">
                <tr>
                    <td class="muted"><fmt:formatDate value="${l.createdAt}" pattern="dd MMM HH:mm"/></td>
                    <td><c:out value="${l.admin}"/></td>
                    <td><span class="badge badge-gray"><c:out value="${l.actionType}"/></span></td>
                    <td class="muted"><c:out value="${l.targetTable}"/><c:if test="${not empty l.targetId}"> #${l.targetId}</c:if></td>
                    <td><c:out value="${l.description}"/></td>
                </tr>
            </c:forEach>
            <c:if test="${empty adminLogs}">
                <tr><td colspan="5" class="empty">No admin actions recorded yet.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</section>

<%@ include file="includes/footer.jsp" %>

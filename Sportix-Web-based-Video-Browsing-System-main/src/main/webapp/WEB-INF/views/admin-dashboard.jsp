<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | Sportix</title>
    <!-- Include your main stylesheet if you have one -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

    <style>
        /* Modern UI Dashboard Styles */
        body {
            background-color: #f8fafc; /* Light background to contrast the dark cards */
            margin: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        }

        .admin-container {
            max-width: 1100px;
            margin: 40px auto;
            padding: 20px;
        }

        .dashboard-header {
            color: #1e293b;
            font-size: 2rem;
            margin-bottom: 24px;
            font-weight: 700;
        }

        /* Analytics Cards */
        .analytics-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 24px;
            margin-bottom: 40px;
        }

        .stat-card {
            background: #1e293b;
            border: 1px solid #334155;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.2);
            transition: transform 0.2s ease, border-color 0.2s ease;
        }

        .stat-card:hover {
            transform: translateY(-4px);
            border-color: #caff33;
        }

        .stat-card h4 {
            color: #94a3b8;
            font-size: 0.875rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin: 0 0 12px 0;
            font-weight: 600;
        }

        .stat-card h2 {
            color: #f8fafc;
            font-size: 2.75rem;
            margin: 0;
            font-weight: 700;
            line-height: 1;
        }

        /* Modern Table Styles */
        .dashboard-section-title {
            color: #1e293b;
            font-size: 1.5rem;
            margin: 40px 0 16px 0;
            font-weight: 600;
            border-bottom: 2px solid #e2e8f0;
            padding-bottom: 8px;
        }

        .modern-table {
            width: 100%;
            border-collapse: collapse;
            background: #1e293b;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            color: #f8fafc;
        }

        .modern-table th {
            background: #0f172a;
            color: #94a3b8;
            text-align: left;
            padding: 16px;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.85rem;
            letter-spacing: 0.05em;
        }

        .modern-table td {
            padding: 16px;
            border-bottom: 1px solid #334155;
        }

        .modern-table tr:last-child td {
            border-bottom: none;
        }

        .modern-table tr:hover {
            background: #334155;
        }

        .modern-table a {
            color: #38bdf8;
            text-decoration: none;
            font-weight: 500;
        }

        .modern-table a:hover {
            text-decoration: underline;
        }

        /* Action Buttons */
        .btn {
            padding: 8px 16px;
            border: none;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
            transition: opacity 0.2s;
            color: white;
            font-size: 0.85rem;
        }

        .btn:hover {
            opacity: 0.8;
        }

        .btn-approve { background: #10b981; } /* Emerald Green */
        .btn-reject { background: #ef4444; }  /* Rose Red */
        .btn-dismiss { background: #64748b; } /* Slate Gray */

        /* Alert Messages */
        .alert {
            padding: 16px;
            border-radius: 8px;
            margin-bottom: 24px;
            font-weight: 500;
        }

        .alert-info {
            background: #dbeafe;
            color: #1e3a8a;
            border: 1px solid #bfdbfe;
        }
    </style>
</head>
<body>
<!-- Replace this with your actual navbar include -->
<%@ include file="includes/navbar.jsp" %>

<div class="admin-container">

    <h1 class="dashboard-header">Admin Dashboard</h1>

    <!-- Displays success/error messages after actions -->
    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="alert alert-info">
                ${sessionScope.flashMessage}
        </div>
        <c:remove var="flashMessage" scope="session"/>
    </c:if>

    <h2 class="dashboard-section-title" style="margin-top: 0; border: none;">System Analytics</h2>
    <div class="analytics-grid">
        <div class="stat-card">
            <h4>Total Users</h4>
            <h2>${systemStats.totalUsers}</h2>
        </div>
        <div class="stat-card">
            <h4>Approved Videos</h4>
            <h2>${systemStats.totalVideos}</h2>
        </div>
        <div class="stat-card">
            <h4>Pending Reports</h4>
            <h2>${systemStats.pendingReports}</h2>
        </div>
    </div>

    <h2 class="dashboard-section-title">Pending Video Submissions</h2>
    <table class="modern-table">
        <tr>
            <th>Title</th>
            <th>Link</th>
            <th>Actions</th>
        </tr>
        <c:forEach var="v" items="${pendingVideos}">
            <tr>
                <td>${v.title}</td>
                <td><a href="${v.youtubeUrl}" target="_blank">View Video</a></td>
                <td>
                    <form action="${pageContext.request.contextPath}/admin" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="updateVideoStatus">
                        <input type="hidden" name="videoId" value="${v.videoId}">
                        <input type="hidden" name="status" value="APPROVED">
                        <button type="submit" class="btn btn-approve">Approve</button>
                    </form>
                    <form action="${pageContext.request.contextPath}/admin" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="updateVideoStatus">
                        <input type="hidden" name="videoId" value="${v.videoId}">
                        <input type="hidden" name="status" value="REJECTED">
                        <button type="submit" class="btn btn-reject">Reject</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <!-- Show empty state message if no videos are pending -->
        <c:if test="${empty pendingVideos}">
            <tr>
                <td colspan="3" style="text-align: center; color: #94a3b8; padding: 32px;">No pending video submissions.</td>
            </tr>
        </c:if>
    </table>

    <h2 class="dashboard-section-title">Pending Content Reports</h2>
    <table class="modern-table">
        <tr>
            <th>Reported Content</th>
            <th>Reason</th>
            <th>Actions</th>
        </tr>
        <c:forEach var="r" items="${pendingReports}">
            <tr>
                <td>${r.videoTitle}</td>
                <td>${r.reason}</td>
                <td>
                    <form action="${pageContext.request.contextPath}/admin" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="updateReportStatus">
                        <input type="hidden" name="reportId" value="${r.reportId}">
                        <input type="hidden" name="status" value="RESOLVED">
                        <button type="submit" class="btn btn-approve">Resolve</button>
                    </form>
                    <form action="${pageContext.request.contextPath}/admin" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="updateReportStatus">
                        <input type="hidden" name="reportId" value="${r.reportId}">
                        <input type="hidden" name="status" value="DISMISSED">
                        <button type="submit" class="btn btn-dismiss">Dismiss</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <!-- Show empty state message if no reports are pending -->
        <c:if test="${empty pendingReports}">
            <tr>
                <td colspan="3" style="text-align: center; color: #94a3b8; padding: 32px;">No pending content reports.</td>
            </tr>
        </c:if>
    </table>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <h2 class="dashboard-section-title" style="margin: 0; border: none;">System Analytics</h2>
        <a href="${pageContext.request.contextPath}/admin/export-csv"
           class="btn"
           style="background: #3b82f6; text-decoration: none; display: inline-flex; align-items: center; gap: 8px;">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5z"/>
                <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708l3 3z"/>
            </svg>
            Export Analytics CSV
        </a>
    </div>
</div>
</body>
</html>
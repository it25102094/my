<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- it25102093 - Member 3: Content Posting Management (Read & Delete UI) --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Videos — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }
        .dashboard-card {
            background: var(--color-surface);
            border: 1px solid var(--color-border);
            border-radius: 12px;
            padding: 15px;
            display: flex;
            flex-direction: column;
        }
        .video-thumbnail {
            width: 100%;
            aspect-ratio: 16/9;
            object-fit: cover;
            border-radius: 8px;
            margin-bottom: 15px;
            background-color: var(--color-surface-alt);
        }
        .dashboard-actions {
            display: flex;
            gap: 10px;
            margin-top: auto; /* Pushes buttons to the bottom */
            padding-top: 15px;
        }
        .status-badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 0.75rem;
            font-weight: bold;
            margin-bottom: 10px;
        }
        .status-APPROVED { background-color: #10b981; color: #fff; }
        .status-PENDING { background-color: #f59e0b; color: #fff; }
        .status-REJECTED { background-color: #ef4444; color: #fff; }
    </style>
</head>
<body>

<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <h2>My Uploaded Videos</h2>
        <a href="${pageContext.request.contextPath}/user/add-video" class="btn-accent">+ Upload New</a>
    </div>

    <div style="margin-top: 15px;"><%@ include file="includes/flash.jsp" %></div>

    <div class="dashboard-grid">
        <c:forEach var="v" items="${myVideos}">
            <div class="dashboard-card">
                <!-- Thumbnail Integration -->
                <c:choose>
                    <c:when test="${not empty v.thumbnailUrl}">
                        <img src="${v.thumbnailUrl}" alt="Thumbnail" class="video-thumbnail">
                    </c:when>
                    <c:otherwise>
                        <div class="video-thumbnail" style="display: flex; align-items: center; justify-content: center; color: var(--color-text-muted);">
                            No Thumbnail
                        </div>
                    </c:otherwise>
                </c:choose>

                <!-- Status Badge Integration -->
                <div>
                    <span class="status-badge status-${v.status}">
                        <c:out value="${v.status}"/>
                    </span>
                </div>

                <h3 style="font-size: 1.1rem; margin-bottom: 5px;">
                    <a href="${pageContext.request.contextPath}/video?id=${v.videoId}"><c:out value="${v.title}"/></a>
                </h3>
                <p style="font-size: 0.85rem; color: var(--color-text-muted); margin-bottom: 5px;">
                    Sport: <c:out value="${v.sportName}"/> &bull; Type: <c:out value="${v.videoType}"/> &bull; <c:out value="${v.categoryName}"/>
                </p>
                <c:if test="${v.status == 'PENDING'}"><p class="muted">Waiting for admin approval &mdash; only you can preview it.</p></c:if>
                <c:if test="${v.status == 'REJECTED'}"><p class="muted">Rejected by a moderator &mdash; edit it to re-submit.</p></c:if>
                <p style="font-size: 0.85rem; color: var(--color-text-muted); margin-bottom: 10px;">
                    Views: ${v.viewCount}
                </p>

                <div class="dashboard-actions">
                    <!-- Update Button -->
                    <a href="${pageContext.request.contextPath}/user/edit-video?id=${v.videoId}" class="btn-outline" style="flex: 1; text-align: center;">Edit</a>

                    <!-- Delete Form (With JS confirmation) -->
                    <form method="post" action="${pageContext.request.contextPath}/user/my-videos" style="flex: 1;" onsubmit="return confirm('Are you sure you want to delete this video? This action cannot be undone.');">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="videoId" value="${v.videoId}">
                        <button type="submit" style="width: 100%; background: #EF4444; color: white; border: none; padding: 10px; border-radius: 6px; cursor: pointer; font-weight: bold;">Delete</button>
                    </form>
                </div>
            </div>
        </c:forEach>

        <c:if test="${empty myVideos}">
            <div style="grid-column: 1 / -1; text-align: center; padding: 40px; background: var(--color-surface); border-radius: 12px; border: 1px solid var(--color-border);">
                <p style="color: var(--color-text-muted); margin-bottom: 15px;">You haven't uploaded any videos yet.</p>
                <a href="${pageContext.request.contextPath}/user/add-video" class="btn-accent">Upload Your First Video</a>
            </div>
        </c:if>
    </div>
</section>

<%@ include file="includes/footer.jsp" %>
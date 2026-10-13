<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- it25102093 - Member 3: Content Posting Management (Update UI) --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Video — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .form-container {
            max-width: 680px;
            margin: 40px auto;
            background-color: var(--color-surface);
            padding: 30px;
            border-radius: 12px;
            border: 1px solid var(--color-border);
        }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; color: var(--color-text-muted); font-size: 0.9rem; font-weight: 600; }
        .form-control {
            width: 100%; padding: 10px; border-radius: 8px;
            border: 1px solid var(--color-border);
            background-color: var(--color-surface-alt);
            color: var(--color-title);
        }
        textarea.form-control { resize: vertical; min-height: 100px; }
    </style>
</head>
<body>

<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <div class="form-container">
        <h2 style="margin-bottom: 8px; color: var(--color-accent-readable);">Edit Video</h2>
        <p class="muted" style="margin-bottom: 20px;">Current status: <span class="badge ${video.status == 'APPROVED' ? 'badge-green' : (video.status == 'PENDING' ? 'badge-yellow' : 'badge-red')}">${video.status}</span>
            <c:if test="${video.status == 'REJECTED'}"> &mdash; saving your changes re-submits the video for approval.</c:if>
        </p>

        <!-- UI/UX: Error Feedback Loop -->
        <c:if test="${not empty error}">
            <div class="form-error" style="margin-bottom: 15px;"><c:out value="${error}"/></div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/user/edit-video">
            <input type="hidden" name="videoId" value="${video.videoId}">

            <%@ include file="includes/video-form-fields.jsp" %>

            <div style="display: flex; gap: 10px; margin-top: 15px;">
                <a href="${pageContext.request.contextPath}/user/my-videos" class="btn-outline" style="flex: 1; text-align: center; padding: 12px; border-radius: 8px;">Cancel</a>
                <button type="submit" class="btn-accent" style="flex: 2; padding: 12px; font-size: 1.1rem; border-radius: 8px; cursor: pointer;">Save Changes</button>
            </div>
        </form>
    </div>
</section>

<%@ include file="includes/footer.jsp" %>

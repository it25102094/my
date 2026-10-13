<form method="post" action="${pageContext.request.contextPath}/user/add-video"><form method="post" action="${pageContext.request.contextPath}/user/add-video"><%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- it25102093 : Member 3 - Content Posting Management (Create Video) --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Post New Video — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .form-container {
            max-width: 600px;
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
        .form-error { color: #ef4444; background: #fee2e2; padding: 12px; border-radius: 6px; border: 1px solid #f87171; }
    </style>
</head>
<body>

<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <div class="form-container">
        <h2 style="margin-bottom: 20px; color: var(--color-accent);">Upload a Video</h2>

        <c:if test="${not empty error}">
            <div class="form-error" style="margin-bottom: 20px;">
                <strong>Error:</strong> ${error}
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/user/add-video">

            <div class="form-group">
                <label>Video Title</label>
                <input type="text" name="title" class="form-control" required maxlength="150"
                       placeholder="e.g. Messi Best Goals 2024"
                       value="<c:out value='${param.title}'/>">
            </div>

            <div class="form-group">
                <label>YouTube Link (Watch, Shorts, or Live)</label>
                <input type="url" name="youtubeUrl" class="form-control" required
                       placeholder="e.g. https://www.youtube.com/watch?v=..."
                       value="<c:out value='${param.youtubeUrl}'/>">
            </div>

            <div class="form-group">
                <label>Description (Optional)</label>
                <textarea name="description" class="form-control"
                          placeholder="Add some context about this video..."><c:out value='${param.description}'/></textarea>
            </div>

            <div style="display: flex; gap: 15px;">
                <div class="form-group" style="flex: 1;">
                    <label>Sport</label>
                    <select name="sportId" class="form-control" required>
                        <option value="1" ${param.sportId == '1' ? 'selected' : ''}>Cricket</option>
                        <option value="2" ${param.sportId == '2' ? 'selected' : ''}>Football</option>
                        <option value="3" ${param.sportId == '3' ? 'selected' : ''}>Basketball</option>
                        <option value="4" ${param.sportId == '4' ? 'selected' : ''}>Formula 1</option>
                        <option value="5" ${param.sportId == '5' ? 'selected' : ''}>Tennis</option>
                        <option value="6" ${param.sportId == '6' ? 'selected' : ''}>Athletics</option>
                    </select>
                </div>

                <div class="form-group" style="flex: 1;">
                    <label>Video Type</label>
                    <select name="videoType" class="form-control" required>
                        <option value="HIGHLIGHT" ${param.videoType == 'HIGHLIGHT' ? 'selected' : ''}>Highlights</option>
                        <option value="LIVE" ${param.videoType == 'LIVE' ? 'selected' : ''}>Live Match</option>
                        <option value="SHORT" ${param.videoType == 'SHORT' ? 'selected' : ''}>Shorts</option>
                    </select>
                </div>
            </div>

            <button type="submit" class="btn-accent" style="width: 100%; margin-top: 15px; padding: 12px; font-size: 1.1rem; cursor: pointer;">
                Publish Video
            </button>
        </form>
    </div>
</section>

<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
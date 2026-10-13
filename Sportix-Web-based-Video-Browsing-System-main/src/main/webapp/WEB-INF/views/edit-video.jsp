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
        .form-error { color: #ef4444; background: #fee2e2; padding: 12px; border-radius: 6px; border: 1px solid #f87171; margin-bottom: 15px; }
    </style>
</head>
<body>

<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <div class="form-container">
        <h2 style="margin-bottom: 20px; color: var(--color-accent);">Edit Video</h2>

        <!-- UI/UX: Error Feedback Loop -->
        <c:if test="${not empty sessionScope.flashMessage}">
            <div class="form-error">
                <c:out value="${sessionScope.flashMessage}"/>
            </div>
            <% session.removeAttribute("flashMessage"); %>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/user/edit-video">
            <input type="hidden" name="videoId" value="${videoId}">

            <div class="form-group">
                <label>Video Title</label>
                <!-- Validation: maxlength="150" matches the backend Service layer -->
                <input type="text" name="title" class="form-control" required maxlength="150" value="<c:out value='${title}'/>">
            </div>

            <div class="form-group">
                <label>YouTube Link (Watch, Shorts, or Live)</label>
                <input type="url" name="youtubeUrl" class="form-control" required value="<c:out value='${youtubeUrl}'/>">
            </div>

            <div class="form-group">
                <label>Description (Optional)</label>
                <textarea name="description" class="form-control"><c:out value='${description}'/></textarea>
            </div>

            <div style="display: flex; gap: 15px;">
                <div class="form-group" style="flex: 1;">
                    <label>Video Type</label>
                    <select name="videoType" class="form-control" required>
                        <option value="HIGHLIGHT" <c:if test="${videoType eq 'HIGHLIGHT'}">selected</c:if>>Highlights</option>
                        <option value="LIVE" <c:if test="${videoType eq 'LIVE'}">selected</c:if>>Live Match</option>
                        <option value="SHORT" <c:if test="${videoType eq 'SHORT'}">selected</c:if>>Shorts</option>
                        <option value="INTERVIEW" <c:if test="${videoType eq 'INTERVIEW'}">selected</c:if>>Interview</option>
                    </select>
                </div>

                <div class="form-group" style="flex: 1;">
                    <label>Sport</label>
                    <select name="sportId" class="form-control" required>
                        <option value="1" <c:if test="${sportId == 1}">selected</c:if>>Cricket</option>
                        <option value="2" <c:if test="${sportId == 2}">selected</c:if>>Football</option>
                        <option value="3" <c:if test="${sportId == 3}">selected</c:if>>Basketball</option>
                        <option value="4" <c:if test="${sportId == 4}">selected</c:if>>Formula 1</option>
                        <option value="5" <c:if test="${sportId == 5}">selected</c:if>>Tennis</option>
                    </select>
                </div>
            </div>

            <div style="display: flex; gap: 10px; margin-top: 15px;">
                <a href="${pageContext.request.contextPath}/user/my-videos" class="btn-outline" style="flex: 1; text-align: center; padding: 12px; border-radius: 8px;">Cancel</a>
                <button type="submit" class="btn-accent" style="flex: 2; padding: 12px; font-size: 1.1rem; border-radius: 8px; cursor: pointer;">Save Changes</button>
            </div>
        </form>
    </div>
</section>

<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
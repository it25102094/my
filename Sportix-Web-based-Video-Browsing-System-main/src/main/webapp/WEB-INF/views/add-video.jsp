<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- it25102093 : Member 3 - Content Posting Management (Create Video)
     FIX: the file started with two stray <form> tags BEFORE the page directive (broken, nested HTML forms). --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Post New Video — Sportix</title>
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
        <h2 style="margin-bottom: 8px; color: var(--color-accent-readable);">Upload a Video</h2>
        <p class="muted" style="margin-bottom: 20px;">
            <c:choose>
                <c:when test="${sessionScope.role == 'CONTENT_MANAGER' || sessionScope.role == 'ADMIN'}">As a ${sessionScope.role == 'ADMIN' ? 'admin' : 'content manager'} your video is published immediately.</c:when>
                <c:otherwise>Your video will be visible after an admin approves it.</c:otherwise>
            </c:choose>
        </p>

        <c:if test="${not empty error}">
            <div class="form-error" style="margin-bottom: 20px;">
                <strong>Error:</strong> <c:out value="${error}"/>
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/user/add-video">
            <%@ include file="includes/video-form-fields.jsp" %>

            <button type="submit" class="btn-accent" style="width: 100%; margin-top: 15px; padding: 12px; font-size: 1.1rem; cursor: pointer;">
                Publish Video
            </button>
        </form>
    </div>
</section>

<%@ include file="includes/footer.jsp" %>

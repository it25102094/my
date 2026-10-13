<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Watch Later — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <div class="section-header"><h2>Watch Later</h2></div>
    <div class="video-grid">
        <c:forEach var="v" items="${watchLaterVideos}">
            <div>
                <%@ include file="includes/video-card.jsp" %>
                <form method="post" action="${pageContext.request.contextPath}/user/watchlater" style="margin-top:8px">
                    <input type="hidden" name="action" value="remove">
                    <input type="hidden" name="videoId" value="${v.videoId}">
                    <button class="btn-outline" type="submit">Remove</button>
                </form>
            </div>
        </c:forEach>
        <c:if test="${empty watchLaterVideos}">
            <p class="video-meta">Nothing saved yet. Open a video and click "Save to Watch Later".</p>
        </c:if>
    </div>
</section>

<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
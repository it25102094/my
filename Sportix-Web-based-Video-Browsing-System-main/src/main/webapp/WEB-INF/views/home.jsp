<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sportix — Sports Streaming &amp; Highlight Platform</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=2">
</head>
<body>

<%@ include file="includes/navbar.jsp" %>

<!-- FEATURED VIDEO (HERO BANNER) -->
<c:if test="${not empty featuredVideo}">
<section class="page-section">
    <div class="hero-banner"
         style="background-image: url('<c:out value="${featuredVideo.thumbnailUrl}"/>');">
        <div class="hero-content">
            <span class="badge-featured">FEATURED</span>
            <h1><c:out value="${featuredVideo.title}"/></h1>
            <p><c:out value="${featuredVideo.description}"/></p>
            <a href="${pageContext.request.contextPath}/video?id=${featuredVideo.videoId}"
               style="color: var(--color-accent); font-weight:700; display:inline-block; margin-top:10px;">
                Watch Now &rarr;
            </a>
        </div>
    </div>
</section>
</c:if>
<c:if test="${empty featuredVideo}">
<section class="page-section">
    <div class="hero-banner">
        <div class="hero-content">
            <span class="badge-featured">FEATURED</span>
            <h1>No featured video right now</h1>
            <p>Check back soon for our next highlighted match.</p>
        </div>
    </div>
</section>
</c:if>

<!-- TOP TRENDING VIDEOS -->
<section class="page-section">
    <div class="section-header">
        <h2>Top Trending</h2>
        <a href="${pageContext.request.contextPath}/trending">See all</a>
    </div>
    <div class="video-grid">
        <c:forEach var="v" items="${trendingVideos}">
            <%@ include file="includes/video-card.jsp" %>
        </c:forEach>
        <c:if test="${empty trendingVideos}">
            <p style="color: var(--color-text-muted);">No trending videos yet — check back soon.</p>
        </c:if>
    </div>
</section>

<!-- LIVE NOW -->
<section class="page-section">
    <div class="section-header">
        <h2>Live Now</h2>
        <a href="${pageContext.request.contextPath}/live">See all</a>
    </div>
    <div class="video-grid">
        <c:forEach var="v" items="${liveVideos}">
            <%@ include file="includes/video-card.jsp" %>
        </c:forEach>
        <c:if test="${empty liveVideos}">
            <p style="color: var(--color-text-muted);">Nothing live right now.</p>
        </c:if>
    </div>
</section>

<!-- SPORTS SHORTS -->
<section class="page-section">
    <div class="section-header">
        <h2>Sports Shorts</h2>
        <a href="${pageContext.request.contextPath}/shorts">See all</a>
    </div>
    <div class="shorts-row">
        <c:forEach var="v" items="${shortVideos}">
            <a href="${pageContext.request.contextPath}/video?id=${v.videoId}" class="short-card">
                <div class="short-thumb" style="background-image:url('<c:out value="${v.thumbnailUrl}"/>'); background-size:cover;"></div>
                <div class="video-info"><h3><c:out value="${v.title}"/></h3></div>
            </a>
        </c:forEach>
        <c:if test="${empty shortVideos}">
            <p style="color: var(--color-text-muted);">No shorts yet.</p>
        </c:if>
    </div>
</section>

<!-- MATCH HIGHLIGHTS -->
<section class="page-section">
    <div class="section-header">
        <h2>Match Highlights</h2>
        <a href="${pageContext.request.contextPath}/highlights">See all</a>
    </div>
    <div class="video-grid">
        <c:forEach var="v" items="${highlightVideos}">
            <%@ include file="includes/video-card.jsp" %>
        </c:forEach>
        <c:if test="${empty highlightVideos}">
            <p style="color: var(--color-text-muted);">No highlights yet.</p>
        </c:if>
    </div>
</section>

<!-- EXCLUSIVE INTERVIEWS (NEW SECTION) -->
<section class="page-section">
    <div class="section-header">
        <h2>Exclusive Interviews</h2>
    </div>
    <div class="video-grid">
        <c:forEach var="v" items="${interviewVideos}">
            <%@ include file="includes/video-card.jsp" %>
        </c:forEach>
        <c:if test="${empty interviewVideos}">
            <p style="color: var(--color-text-muted);">No interviews available right now.</p>
        </c:if>
    </div>
</section>

<!-- BROWSE BY SPORT CATEGORIES -->
<section class="page-section">
    <div class="section-header"><h2>Browse by Sport</h2></div>
    <div class="category-grid">
        <a href="${pageContext.request.contextPath}/categories?sport=Cricket" class="category-pill">Cricket</a>
        <a href="${pageContext.request.contextPath}/categories?sport=Football" class="category-pill">Football</a>
        <a href="${pageContext.request.contextPath}/categories?sport=Basketball" class="category-pill">Basketball</a>
        <a href="${pageContext.request.contextPath}/categories?sport=Formula 1" class="category-pill">Formula 1</a>
        <a href="${pageContext.request.contextPath}/categories?sport=Tennis" class="category-pill">Tennis</a>
    </div>
</section>

<!-- CONTACT & FAQ -->
<section class="page-section">
    <div class="section-header"><h2>Contact &amp; FAQ</h2></div>
    <div class="faq-item">
        <h4>How do I follow my favourite team?</h4>
        <p>Register an account, then visit any team's page and click "Follow" to prioritise their content on your homepage.</p>
    </div>
    <div class="faq-item">
        <h4>What is Hide Scores?</h4>
        <p>Enable it from the navbar toggle to hide match scores and results until you're ready to reveal them.</p>
    </div>
    <div class="faq-item">
        <h4>Need more help?</h4>
        <p>Email us at support@sportix.lk and our team will respond within 24 hours.</p>
    </div>
</section>

<%@ include file="includes/footer.jsp" %>
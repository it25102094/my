<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Original servlet path (after forward) so the current tab can be highlighted --%>
<c:set var="navPath" value="${requestScope['jakarta.servlet.forward.servlet_path']}"/>

<nav class="navbar" data-ctx="${pageContext.request.contextPath}">
    <!-- 1. Logo -->
    <a href="${pageContext.request.contextPath}/home" class="navbar-logo">
        SPORTI<span>X</span>
    </a>

    <!-- 2. Mobile Menu Button -->
    <button id="mobile-menu-toggle" class="mobile-menu-btn" type="button" aria-label="Menu">&#9776;</button>

    <!-- 3. Navigation Links -->
    <div class="navbar-links">
        <a href="${pageContext.request.contextPath}/home" class="${navPath == '/home' || empty navPath || navPath == '' ? 'active' : ''}">Home</a>
        <a href="${pageContext.request.contextPath}/live" class="${navPath == '/live' ? 'active' : ''}">Live</a>
        <a href="${pageContext.request.contextPath}/highlights" class="${navPath == '/highlights' ? 'active' : ''}">Highlights</a>
        <a href="${pageContext.request.contextPath}/shorts" class="${navPath == '/shorts' ? 'active' : ''}">Shorts</a>
        <a href="${pageContext.request.contextPath}/categories" class="${navPath == '/categories' ? 'active' : ''}">Sports</a>
    </div>

    <!-- 4. Search Bar -->
    <form action="${pageContext.request.contextPath}/search" method="get" class="navbar-search">
        <span>&#128269;</span>
        <input type="text" name="q" placeholder="Search teams, players, tournaments..." value="<c:out value='${param.q}'/>">
    </form>

    <!-- 5. Right Actions -->
    <div class="navbar-actions">

        <c:if test="${not empty sessionScope.userId}">
            <div class="pill-toggle-group" title="Hide match scores and spoilers">
                <span class="pill-toggle-label">Hide Scores</span>
                <button id="spoiler-toggle"
                        type="button"
                        class="switch-toggle"
                        role="switch"
                        aria-checked="${sessionScope.spoilerFree == true ? 'true' : 'false'}"></button>
            </div>
        </c:if>

        <div class="pill-toggle-group" title="Switch Dark / Light Theme">
            <button id="theme-toggle"
                    type="button"
                    class="switch-toggle"
                    role="switch"
                    aria-checked="true"></button>
        </div>

        <c:choose>
            <c:when test="${not empty sessionScope.userId}">
                <!-- Member 3 (Content Posting CRUD) -->
                <a href="${pageContext.request.contextPath}/user/add-video" class="btn-accent" style="padding: 4px 10px; font-size: 0.85rem;">+ Upload</a>
                <a href="${pageContext.request.contextPath}/user/my-videos">My Videos</a>

                <!-- Member 5 (Advertising) & Member 6 (Admin) role links -->
                <c:if test="${sessionScope.role == 'ADVERTISER' || sessionScope.role == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/advertiser/dashboard" title="Advertiser Dashboard">&#128226; Ads</a>
                </c:if>
                <c:if test="${sessionScope.role == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/admin" title="Admin Dashboard">&#128737; Admin</a>
                </c:if>

                <!-- Member 5 (Notifications) - unread badge set by NotificationBadgeFilter -->
                <a href="${pageContext.request.contextPath}/notifications" title="Notifications">&#128276;<c:if test="${unreadNotificationCount > 0}"><span class="notif-badge">${unreadNotificationCount > 99 ? '99+' : unreadNotificationCount}</span></c:if></a>
                <a href="${pageContext.request.contextPath}/user/watchlater">Watch Later</a>
                <a href="${pageContext.request.contextPath}/profile">Hi, <c:out value="${sessionScope.displayName}"/></a>
                <a href="${pageContext.request.contextPath}/logout">Logout</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/login" class="btn-outline" style="padding: 6px 14px;">Login</a>
                <a href="${pageContext.request.contextPath}/register" class="btn-accent" style="padding: 6px 14px;">Register</a>
            </c:otherwise>
        </c:choose>
    </div>
</nav>

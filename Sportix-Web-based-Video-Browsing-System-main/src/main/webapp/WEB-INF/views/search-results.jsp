<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Search: <c:out value="${keyword}"/> — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <div class="section-header" style="flex-wrap: wrap; gap: 15px;">
        <h2>Search Results for "<c:out value="${keyword}"/>" (${results.size()} found)</h2>

        <!-- Strategy Pattern Sort & Save Search Bar -->
        <div style="display: flex; align-items: center; gap: 12px;">
            <!-- Sort Form (Strategy Pattern - Safely routes back to /categories if filtering by sport, otherwise /search) -->
            <form method="get" action="${pageContext.request.contextPath}${not empty param.sport ? '/categories' : '/search'}" style="display: flex; align-items: center; gap: 8px;">
                <c:if test="${not empty param.sport}">
                    <input type="hidden" name="sport" value="<c:out value='${param.sport}'/>">
                </c:if>
                <input type="hidden" name="q" value="<c:out value='${keyword}'/>">
                <label style="font-size: 0.85rem; color: var(--color-text-muted);">Sort By:</label>
                <select name="sort" onchange="this.form.submit()" style="padding: 6px 10px; background: var(--color-surface-alt); color: var(--color-title); border: 1px solid var(--color-border); border-radius: 6px; font-size: 0.85rem;">
                    <option value="VIEWS" ${currentSort == 'VIEWS' ? 'selected' : ''}>Most Viewed</option>
                    <option value="NEWEST" ${currentSort == 'NEWEST' ? 'selected' : ''}>Newest First</option>
                </select>
            </form>

            <!-- Save Search Button (If User Logged In) -->
            <c:if test="${not empty sessionScope.userId and not empty keyword}">
                <button type="button" class="btn-outline" onclick="document.getElementById('saveSearchModal').style.display='flex'" style="padding: 6px 12px; font-size: 0.85rem;">
                    ★ Save Search
                </button>
            </c:if>
        </div>
    </div>

    <!-- Saved Searches Quick-Access Pills -->
    <c:if test="${not empty savedSearches}">
        <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 20px; flex-wrap: wrap;">
            <span style="font-size: 0.85rem; color: var(--color-text-muted);">Saved Filters:</span>
            <c:forEach var="ss" items="${savedSearches}">
                <div style="display: inline-flex; align-items: center; background: var(--color-surface); border: 1px solid var(--color-border); padding: 4px 10px; border-radius: 8px; font-size: 0.85rem;">
                    <a href="${pageContext.request.contextPath}/search?q=<c:out value='${ss.searchQuery}'/>&sort=<c:out value='${ss.sortPreference}'/>" style="color: var(--color-accent); font-weight: 600; margin-right: 8px;">
                        <c:out value="${ss.searchLabel}"/>
                    </a>
                    <form method="post" action="${pageContext.request.contextPath}/search" style="display:inline;">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="savedSearchId" value="${ss.savedSearchId}">
                        <input type="hidden" name="q" value="<c:out value='${keyword}'/>">
                        <button type="submit" style="background:none; border:none; color:#EF4444; cursor:pointer; font-weight:bold;">&times;</button>
                    </form>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <div class="video-grid">
        <c:forEach var="v" items="${results}">
            <%@ include file="includes/video-card.jsp" %>
        </c:forEach>
        <c:if test="${empty results}">
            <p style="color: var(--color-text-muted);">No results found. Try different keywords.</p>
        </c:if>
    </div>
</section>

<!-- Save Search Modal Popup -->
<div id="saveSearchModal" style="display:none; position:fixed; inset:0; background:rgba(0,0,0,0.6); z-index:1000; align-items:center; justify-content:center;">
    <div class="auth-card" style="width: 100%; max-width: 380px; position:relative;">
        <h3>Save Current Search</h3>
        <form method="post" action="${pageContext.request.contextPath}/search">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="q" value="<c:out value='${keyword}'/>">
            <input type="hidden" name="sortPref" value="<c:out value='${currentSort}'/>">

            <label style="font-size:0.85rem; color:var(--color-text-muted);">Filter Label Name:</label>
            <input type="text" name="label" placeholder="e.g., My Favorite Cricket" value="<c:out value='${keyword}'/>" required style="margin-top:6px; margin-bottom:14px;">

            <div style="display: flex; gap: 10px;">
                <button type="submit" class="btn-accent" style="flex:1;">Save</button>
                <button type="button" class="btn-outline" onclick="document.getElementById('saveSearchModal').style.display='none'" style="flex:1;">Cancel</button>
            </div>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
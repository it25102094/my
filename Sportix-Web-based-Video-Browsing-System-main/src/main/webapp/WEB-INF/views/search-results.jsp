<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${browseMode ? keyword : 'Search: '.concat(keyword)}"/> — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <%@ include file="includes/flash.jsp" %>

    <div class="section-header" style="flex-wrap: wrap; gap: 15px;">
        <h2>
            <c:choose>
                <c:when test="${browseMode}"><c:out value="${keyword}"/> (${results.size()} videos)</c:when>
                <c:otherwise>Search Results for "<c:out value="${keyword}"/>" (${results.size()} found)</c:otherwise>
            </c:choose>
        </h2>

        <!-- Strategy Pattern Sort & Save Search Bar -->
        <div style="display: flex; align-items: center; gap: 12px;">
            <%-- Sort Form (Strategy Pattern). FIX: it always submitted to /search, so sorting the
                 Live / Highlights / Shorts tabs ran a keyword search for "Live Now" etc. --%>
            <form method="get" action="${pageContext.request.contextPath}${empty sortAction ? '/search' : sortAction}" style="display: flex; align-items: center; gap: 8px;">
                <c:if test="${not empty param.sport}">
                    <input type="hidden" name="sport" value="<c:out value='${param.sport}'/>">
                </c:if>
                <c:if test="${not browseMode}">
                    <input type="hidden" name="q" value="<c:out value='${keyword}'/>">
                </c:if>
                <label style="font-size: 0.85rem; color: var(--color-text-muted);">Sort By:</label>
                <select name="sort" onchange="this.form.submit()" style="padding: 6px 10px; background: var(--color-surface-alt); color: var(--color-title); border: 1px solid var(--color-border); border-radius: 6px; font-size: 0.85rem;">
                    <option value="VIEWS" ${currentSort == 'VIEWS' ? 'selected' : ''}>Most Viewed</option>
                    <option value="NEWEST" ${currentSort == 'NEWEST' ? 'selected' : ''}>Newest First</option>
                </select>
            </form>

            <!-- Save Search Button (If User Logged In, real keyword searches only) -->
            <c:if test="${not empty sessionScope.userId and not empty keyword and not browseMode}">
                <button type="button" class="btn-outline" onclick="document.getElementById('saveSearchModal').style.display='flex'" style="padding: 6px 12px; font-size: 0.85rem;">
                    &#9733; Save Search
                </button>
            </c:if>
        </div>
    </div>

    <!-- Saved Searches (Member 2 CRUD: read / update / delete) -->
    <c:if test="${not empty savedSearches}">
        <div class="card" style="margin-bottom: 20px; padding: 14px 16px;">
            <div style="display:flex; justify-content:space-between; align-items:center; gap:10px; flex-wrap:wrap;">
                <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                    <span style="font-size: 0.85rem; color: var(--color-text-muted);">Saved Filters:</span>
                    <c:forEach var="ss" items="${savedSearches}">
                        <a href="${pageContext.request.contextPath}/search?q=<c:out value='${ss.searchQuery}'/>&sort=<c:out value='${ss.sortPreference}'/>"
                           class="category-pill" style="padding: 5px 12px; font-size: 0.82rem;">
                            <c:out value="${ss.searchLabel}"/> <span class="muted">(${ss.sortPreference == 'NEWEST' ? 'newest' : 'most viewed'})</span>
                        </a>
                    </c:forEach>
                </div>
                <button type="button" class="link-plain" onclick="document.getElementById('manageSaved').classList.toggle('show');">&#9998; Manage</button>
            </div>
            <div id="manageSaved" class="type-help" style="margin-top: 12px;">
                <c:forEach var="ss" items="${savedSearches}">
                    <div class="team-row">
                        <form method="post" action="${pageContext.request.contextPath}/search" class="inline-form">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="savedSearchId" value="${ss.savedSearchId}">
                            <input type="hidden" name="q" value="<c:out value='${keyword}'/>">
                            <input type="text" name="label" value="<c:out value='${ss.searchLabel}'/>" maxlength="100" required>
                            <select name="sortPref">
                                <option value="VIEWS" ${ss.sortPreference == 'VIEWS' ? 'selected' : ''}>Most Viewed</option>
                                <option value="NEWEST" ${ss.sortPreference == 'NEWEST' ? 'selected' : ''}>Newest First</option>
                            </select>
                            <span class="muted">keyword: "<c:out value='${ss.searchQuery}'/>"</span>
                            <button type="submit" class="btn-outline btn-sm">Save</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/search" class="inline-form" onsubmit="return confirm('Delete this saved search?');">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="savedSearchId" value="${ss.savedSearchId}">
                            <input type="hidden" name="q" value="<c:out value='${keyword}'/>">
                            <button type="submit" class="link-danger">Delete</button>
                        </form>
                    </div>
                </c:forEach>
            </div>
        </div>
    </c:if>

    <!-- Member 5: sponsored text ad -->
    <c:if test="${not empty searchAds}">
        <div class="ad-slot" style="margin-bottom: 20px; max-width: 720px;">
            <c:forEach var="ad" items="${searchAds}">
                <%@ include file="includes/ad-card.jsp" %>
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
            <input type="text" name="label" placeholder="e.g., My Favorite Cricket" value="<c:out value='${keyword}'/>" maxlength="100" required style="margin-top:6px; margin-bottom:14px;">

            <div style="display: flex; gap: 10px;">
                <button type="submit" class="btn-accent" style="flex:1;">Save</button>
                <button type="button" class="btn-outline" onclick="document.getElementById('saveSearchModal').style.display='none'" style="flex:1;">Cancel</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="includes/footer.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- =====================================================================================
     Member 5 (IT25102094) - Advertising Management : Advertiser Dashboard
     Served ONLY through CampaignServlet (/advertiser/dashboard). It was moved from the webapp
     root into WEB-INF/views so it can no longer be opened directly without data or login.
       * Campaign CRUD      : create form, list table, edit (pre-filled form), pause/resume, delete
       * Advertisement CRUD : create form, list table, edit (pre-filled form), pause/activate, delete
       * Performance        : impressions, clicks and click-through-rate per ad / campaign
     ===================================================================================== --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Advertiser Dashboard — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<%@ include file="includes/navbar.jsp" %>

<section class="page-section">

    <div class="dash-header">
        <div>
            <h1>&#128226; Advertiser Dashboard</h1>
            <div class="sub">
                <c:choose>
                    <c:when test="${isAdminView}">Admin view &mdash; you can manage the campaigns and ads of <strong>every</strong> advertiser.</c:when>
                    <c:otherwise>Signed in as <strong><c:out value="${sessionScope.loggedUser.companyName != null ? sessionScope.loggedUser.companyName : sessionScope.displayName}"/></strong>
                        &mdash; create campaigns, add ads and track how they perform.</c:otherwise>
                </c:choose>
            </div>
        </div>
        <a class="btn-outline btn-sm" href="${pageContext.request.contextPath}/home" target="_blank">View site as a viewer &#8599;</a>
    </div>

    <%@ include file="includes/flash.jsp" %>

    <!-- ===================== PERFORMANCE SUMMARY ===================== -->
    <div class="stats-grid">
        <div class="stat-card"><div class="stat-label">Campaigns</div><div class="stat-value">${stats.campaignCount}</div><div class="stat-hint">${stats.runningCampaigns} running now</div></div>
        <div class="stat-card"><div class="stat-label">Advertisements</div><div class="stat-value">${stats.adCount}</div><div class="stat-hint">${stats.activeAds} active</div></div>
        <div class="stat-card"><div class="stat-label">Impressions</div><div class="stat-value">${stats.impressions}</div><div class="stat-hint">times ads were shown</div></div>
        <div class="stat-card"><div class="stat-label">Clicks</div><div class="stat-value">${stats.clicks}</div><div class="stat-hint">visits to your site</div></div>
        <div class="stat-card"><div class="stat-label">Click-through rate</div><div class="stat-value"><fmt:formatNumber value="${stats.ctr}" maxFractionDigits="2" minFractionDigits="1"/>%</div><div class="stat-hint">clicks / impressions</div></div>
        <div class="stat-card"><div class="stat-label">Total budget</div><div class="stat-value" style="font-size:1.4rem;">LKR <fmt:formatNumber value="${stats.totalBudget}" maxFractionDigits="0"/></div><div class="stat-hint">all campaigns</div></div>
    </div>

    <!-- ===================== CAMPAIGNS ===================== -->
    <h2 class="section-title" id="campaigns">
        <span>Campaigns <small>(a campaign decides when and how often its ads are shown)</small></span>
    </h2>

    <%-- CREATE / UPDATE form (pre-filled when ?editCampaign=ID) --%>
    <div class="card" id="campaign-form">
        <c:set var="ec" value="${editCampaign}"/>
        <h3>${not empty ec ? '&#9998; Edit campaign' : '&#10133; Create a new campaign'}</h3>
        <form action="${pageContext.request.contextPath}/advertiser/campaign" method="post">
            <input type="hidden" name="action" value="${not empty ec ? 'update' : 'create'}">
            <c:if test="${not empty ec}"><input type="hidden" name="campaignId" value="${ec.campaignId}"></c:if>
            <div class="form-grid">
                <div class="form-field full">
                    <label for="campaignName">Campaign name <span class="required">*</span></label>
                    <input type="text" id="campaignName" name="campaignName" required maxlength="120"
                           placeholder="e.g. Summer Cricket Gear Promotion" value="<c:out value='${ec.campaignName}'/>">
                </div>
                <div class="form-field">
                    <label for="budget">Budget (LKR)</label>
                    <input type="number" id="budget" name="budget" min="0" max="99999999.99" step="0.01"
                           placeholder="e.g. 50000" value="${not empty ec ? ec.budget : ''}">
                </div>
                <div class="form-field">
                    <label for="priority">Priority</label>
                    <select id="priority" name="priority">
                        <option value="1" ${ec.priority == 1 ? 'selected' : ''}>High (shown first on the home page)</option>
                        <option value="2" ${empty ec || ec.priority == 2 ? 'selected' : ''}>Normal</option>
                        <option value="3" ${ec.priority == 3 ? 'selected' : ''}>Low</option>
                    </select>
                </div>
                <div class="form-field">
                    <label for="startDate">Start date <span class="required">*</span></label>
                    <input type="date" id="startDate" name="startDate" required
                           value="${not empty ec ? ec.startDate : today}" ${empty ec ? 'min='.concat(today) : ''}>
                </div>
                <div class="form-field">
                    <label for="endDate">End date <span class="required">*</span></label>
                    <input type="date" id="endDate" name="endDate" required value="${ec.endDate}">
                </div>
                <div class="form-field">
                    <label for="campaignStatus">Status</label>
                    <select id="campaignStatus" name="status">
                        <option value="ACTIVE" ${empty ec || ec.status == 'ACTIVE' ? 'selected' : ''}>Active</option>
                        <option value="PAUSED" ${ec.status == 'PAUSED' ? 'selected' : ''}>Paused</option>
                        <option value="COMPLETED" ${ec.status == 'COMPLETED' ? 'selected' : ''}>Completed</option>
                    </select>
                </div>
            </div>
            <div class="form-actions">
                <c:if test="${not empty ec}">
                    <a class="btn-outline" href="${pageContext.request.contextPath}/advertiser/dashboard#campaigns">Cancel</a>
                </c:if>
                <button type="submit" class="btn-accent">${not empty ec ? 'Save changes' : 'Create campaign'}</button>
            </div>
        </form>
    </div>

    <%-- READ: campaign list with UPDATE / DELETE actions --%>
    <div class="table-wrap" style="margin-top:16px;">
        <table class="data-table">
            <thead>
            <tr>
                <th>Campaign</th>
                <c:if test="${isAdminView}"><th>Advertiser</th></c:if>
                <th>Dates</th>
                <th>Priority</th>
                <th>Budget (LKR)</th>
                <th>State</th>
                <th>Ads</th>
                <th>Impr. / Clicks / CTR</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="cmp" items="${campaigns}">
                <tr>
                    <td><strong><c:out value="${cmp.campaignName}"/></strong><div class="muted">#${cmp.campaignId}</div></td>
                    <c:if test="${isAdminView}"><td><c:out value="${cmp.companyName != null ? cmp.companyName : cmp.advertiserName}"/></td></c:if>
                    <td class="muted">${cmp.startDate}<br>&rarr; ${cmp.endDate}</td>
                    <td><span class="badge ${cmp.priority == 1 ? 'badge-blue' : 'badge-gray'}">${cmp.priorityLabel}</span></td>
                    <td><fmt:formatNumber value="${cmp.budget}" minFractionDigits="2" maxFractionDigits="2"/></td>
                    <td>
                        <c:set var="rs" value="${cmp.runState}"/>
                        <span class="badge ${rs == 'RUNNING' ? 'badge-green' : (rs == 'PAUSED' || rs == 'SCHEDULED' ? 'badge-yellow' : 'badge-gray')}">${rs}</span>
                    </td>
                    <td>${cmp.adCount}</td>
                    <td class="muted">${cmp.totalImpressions} / ${cmp.totalClicks} / <fmt:formatNumber value="${cmp.clickThroughRate}" maxFractionDigits="1"/>%</td>
                    <td>
                        <div class="actions">
                            <a class="btn-outline btn-sm" href="${pageContext.request.contextPath}/advertiser/dashboard?editCampaign=${cmp.campaignId}#campaign-form">Edit</a>
                            <form class="inline-form" action="${pageContext.request.contextPath}/advertiser/campaign" method="post">
                                <input type="hidden" name="action" value="status">
                                <input type="hidden" name="campaignId" value="${cmp.campaignId}">
                                <input type="hidden" name="status" value="${cmp.status == 'ACTIVE' ? 'PAUSED' : 'ACTIVE'}">
                                <button type="submit" class="${cmp.status == 'ACTIVE' ? 'btn-warning' : 'btn-success'} btn-sm">${cmp.status == 'ACTIVE' ? 'Pause' : 'Resume'}</button>
                            </form>
                            <form class="inline-form" action="${pageContext.request.contextPath}/advertiser/campaign" method="post"
                                  onsubmit="return confirm('Delete this campaign AND all of its ads? This cannot be undone.');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="campaignId" value="${cmp.campaignId}">
                                <button type="submit" class="btn-danger btn-sm">Delete</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty campaigns}">
                <tr><td colspan="9" class="empty">No campaigns yet &mdash; create your first campaign with the form above.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <!-- ===================== ADVERTISEMENTS ===================== -->
    <h2 class="section-title" id="ads">
        <span>Advertisements <small>(what viewers actually see on Sportix)</small></span>
    </h2>

    <div class="card" id="ad-form">
        <c:set var="ea" value="${editAd}"/>
        <h3>${not empty ea ? '&#9998; Edit advertisement' : '&#10133; Create a new advertisement'}</h3>
        <c:choose>
            <c:when test="${empty campaigns}">
                <p>You need a campaign first. Create one above, then come back here to add ads to it.</p>
            </c:when>
            <c:otherwise>
                <form action="${pageContext.request.contextPath}/advertiser/ad" method="post">
                    <input type="hidden" name="action" value="${not empty ea ? 'update' : 'create'}">
                    <c:if test="${not empty ea}"><input type="hidden" name="adId" value="${ea.adId}"></c:if>
                    <div class="form-grid">
                        <div class="form-field">
                            <label for="campaignId">Campaign <span class="required">*</span></label>
                            <select id="campaignId" name="campaignId" required>
                                <c:forEach var="cmp" items="${campaigns}">
                                    <option value="${cmp.campaignId}" ${ea.campaignId == cmp.campaignId ? 'selected' : ''}><c:out value="${cmp.campaignName}"/> (${cmp.runState})</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="form-field">
                            <label for="adType">Ad type <span class="required">*</span></label>
                            <select id="adType" name="adType" required onchange="showTypeHelp(this.value)">
                                <option value="BANNER" ${empty ea || ea.adType == 'BANNER' ? 'selected' : ''}>Banner (home page)</option>
                                <option value="SPONSORED_VIDEO" ${ea.adType == 'SPONSORED_VIDEO' ? 'selected' : ''}>Sponsored video (video page)</option>
                                <option value="TEXT" ${ea.adType == 'TEXT' ? 'selected' : ''}>Text ad (video &amp; search pages)</option>
                            </select>
                        </div>
                        <div class="form-field">
                            <label for="adStatus">Status</label>
                            <select id="adStatus" name="status">
                                <option value="ACTIVE" ${empty ea || ea.status == 'ACTIVE' ? 'selected' : ''}>Active</option>
                                <option value="PAUSED" ${ea.status == 'PAUSED' ? 'selected' : ''}>Paused</option>
                            </select>
                        </div>
                        <div class="form-field full">
                            <label for="adTitle">Ad title <span class="required">*</span></label>
                            <input type="text" id="adTitle" name="adTitle" required maxlength="150"
                                   placeholder="e.g. Dialog 5G - Stream Live Cricket With Zero Buffering!" value="<c:out value='${ea.adTitle}'/>">
                        </div>
                        <div class="form-field full">
                            <label for="adContent">Ad text <span class="hint">(required for text ads, max 500 characters)</span></label>
                            <textarea id="adContent" name="adContent" maxlength="500"
                                      placeholder="Short message shown under the title"><c:out value="${ea.adContent}"/></textarea>
                        </div>
                        <div class="form-field">
                            <label for="mediaUrl">Media URL</label>
                            <input type="url" id="mediaUrl" name="mediaUrl" maxlength="500"
                                   placeholder="https://..." value="<c:out value='${ea.mediaUrl}'/>">
                            <span class="hint type-help" data-type="BANNER">Banner: link to an image (jpg / png / webp).</span>
                            <span class="hint type-help" data-type="SPONSORED_VIDEO">Sponsored video: a YouTube link (watch / youtu.be / shorts).</span>
                            <span class="hint type-help" data-type="TEXT">Text ad: not needed.</span>
                        </div>
                        <div class="form-field">
                            <label for="targetUrl">Click-through (target) URL <span class="required">*</span></label>
                            <input type="url" id="targetUrl" name="targetUrl" required maxlength="500"
                                   placeholder="https://your-brand.lk/offer" value="<c:out value='${ea.targetUrl}'/>">
                            <span class="hint">Viewers who click the ad are sent here (the click is counted first).</span>
                        </div>
                    </div>
                    <div class="form-actions">
                        <c:if test="${not empty ea}">
                            <a class="btn-outline" href="${pageContext.request.contextPath}/advertiser/dashboard#ads">Cancel</a>
                        </c:if>
                        <button type="submit" class="btn-accent">${not empty ea ? 'Save changes' : 'Create advertisement'}</button>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- READ: advertisement list with UPDATE / DELETE actions --%>
    <div class="table-wrap" style="margin-top:16px;">
        <table class="data-table">
            <thead>
            <tr>
                <th>Preview</th>
                <th>Advertisement</th>
                <th>Type</th>
                <th>Campaign</th>
                <th>Status</th>
                <th>Impr. / Clicks / CTR</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="ad" items="${ads}">
                <tr>
                    <td>
                        <c:choose>
                            <c:when test="${ad.banner and not empty ad.mediaUrl}"><img class="ad-preview-img" src="<c:out value='${ad.mediaUrl}'/>" alt="banner preview" loading="lazy"></c:when>
                            <c:when test="${ad.sponsoredVideo}"><img class="ad-preview-img" src="<c:out value='${ad.videoThumbnailUrl}'/>" alt="video preview" loading="lazy"></c:when>
                            <c:otherwise><div class="ad-preview-text">TEXT AD</div></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <strong><c:out value="${ad.adTitle}"/></strong>
                        <c:if test="${not empty ad.adContent}"><div class="muted"><c:out value="${ad.adContent}"/></div></c:if>
                        <div class="muted"><a href="<c:out value='${ad.targetUrl}'/>" target="_blank" rel="noopener"><c:out value="${ad.targetUrl}"/></a></div>
                    </td>
                    <td><span class="badge badge-gray">${ad.adType}</span></td>
                    <td><c:out value="${ad.campaignName}"/><c:if test="${isAdminView}"><div class="muted"><c:out value="${ad.companyName}"/></div></c:if></td>
                    <td>
                        <span class="badge ${ad.status == 'ACTIVE' ? 'badge-green' : 'badge-yellow'}">${ad.status}</span>
                        <div style="margin-top:4px;">
                            <c:choose>
                                <c:when test="${ad.runningNow}"><span class="badge badge-blue" title="Viewers can see this ad right now">LIVE ON SITE</span></c:when>
                                <c:otherwise><span class="muted" title="The ad or its campaign is paused, or today is outside the campaign dates">not showing</span></c:otherwise>
                            </c:choose>
                        </div>
                    </td>
                    <td class="muted">${ad.impressions} / ${ad.clicks} / <fmt:formatNumber value="${ad.clickThroughRate}" maxFractionDigits="1"/>%</td>
                    <td>
                        <div class="actions">
                            <a class="btn-outline btn-sm" href="${pageContext.request.contextPath}/advertiser/dashboard?editAd=${ad.adId}#ad-form">Edit</a>
                            <form class="inline-form" action="${pageContext.request.contextPath}/advertiser/ad" method="post">
                                <input type="hidden" name="action" value="status">
                                <input type="hidden" name="adId" value="${ad.adId}">
                                <input type="hidden" name="status" value="${ad.status == 'ACTIVE' ? 'PAUSED' : 'ACTIVE'}">
                                <button type="submit" class="${ad.status == 'ACTIVE' ? 'btn-warning' : 'btn-success'} btn-sm">${ad.status == 'ACTIVE' ? 'Pause' : 'Activate'}</button>
                            </form>
                            <form class="inline-form" action="${pageContext.request.contextPath}/advertiser/ad" method="post"
                                  onsubmit="return confirm('Delete this advertisement?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="adId" value="${ad.adId}">
                                <button type="submit" class="btn-danger btn-sm">Delete</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty ads}">
                <tr><td colspan="7" class="empty">No advertisements yet &mdash; add one with the form above.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>

    <div class="card" style="margin-top:22px;">
        <h3>Where are my ads shown?</h3>
        <p><strong>Banner</strong> ads appear at the top of the <a href="${pageContext.request.contextPath}/home" style="color:var(--color-accent-readable)">home page</a> (high-priority campaigns first).
           <strong>Sponsored video</strong> and <strong>text</strong> ads appear next to the player on every video page, and text ads also on search / browse pages (fair rotation).
           An ad is only shown while it is <em>Active</em>, its campaign is <em>Active</em> and today is between the campaign start and end dates.</p>
    </div>
</section>

<script>
    // Shows the media-URL hint that matches the selected ad type
    function showTypeHelp(type) {
        document.querySelectorAll('.type-help').forEach(function (el) {
            el.classList.toggle('show', el.getAttribute('data-type') === type);
        });
    }
    (function () {
        var sel = document.getElementById('adType');
        if (sel) showTypeHelp(sel.value);
    })();
</script>
<%@ include file="includes/footer.jsp" %>

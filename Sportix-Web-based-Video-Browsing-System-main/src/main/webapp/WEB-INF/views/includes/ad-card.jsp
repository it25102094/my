<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Member 5 (IT25102094): renders ONE advertisement held in the page variable "ad".
     Clicking goes through /ad/click so the click is counted before the viewer is redirected. --%>
<a class="ad-card ad-${ad.adType}" href="${pageContext.request.contextPath}/ad/click?id=${ad.adId}"
   target="_blank" rel="noopener sponsored" title="Sponsored by <c:out value='${ad.companyName}'/>">
    <span class="ad-label">SPONSORED</span>
    <c:choose>
        <c:when test="${ad.banner}">
            <img src="<c:out value='${ad.mediaUrl}'/>" alt="<c:out value='${ad.adTitle}'/>" loading="lazy">
            <div class="ad-body">
                <h4><c:out value="${ad.adTitle}"/></h4>
                <c:if test="${not empty ad.adContent}"><p><c:out value="${ad.adContent}"/></p></c:if>
                <span class="ad-cta">Learn more at <c:out value="${ad.companyName}"/> &rarr;</span>
            </div>
        </c:when>
        <c:when test="${ad.sponsoredVideo}">
            <div class="ad-thumb">
                <img src="<c:out value='${ad.videoThumbnailUrl}'/>" alt="<c:out value='${ad.adTitle}'/>" loading="lazy">
                <span class="play-icon">&#9654;</span>
            </div>
            <div class="ad-body">
                <h4><c:out value="${ad.adTitle}"/></h4>
                <c:if test="${not empty ad.adContent}"><p><c:out value="${ad.adContent}"/></p></c:if>
                <span class="ad-cta">Watch now &rarr;</span>
            </div>
        </c:when>
        <c:otherwise>
            <div class="ad-body">
                <h4><c:out value="${ad.adTitle}"/></h4>
                <p><c:out value="${ad.adContent}"/></p>
                <span class="ad-cta"><c:out value="${ad.companyName}"/> &rarr;</span>
            </div>
        </c:otherwise>
    </c:choose>
</a>

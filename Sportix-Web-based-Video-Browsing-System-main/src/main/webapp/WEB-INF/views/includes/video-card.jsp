<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- LIVE videos are exempt: nothing to spoil in a match still in progress --%>
<c:set var="hide" value="${sessionScope.spoilerFree == true && !v.isLive}"/>
<a href="${pageContext.request.contextPath}/video?id=${v.videoId}" class="video-card">
    <div class="video-thumb"
         <c:if test="${!hide}">style="background-image:url('<c:out value="${v.thumbnailUrl}"/>'); background-size:cover; background-position:center;"</c:if>>
        <c:if test="${v.isLive}"><span class="badge-live">&#9679; LIVE</span></c:if>
        <c:if test="${hide}">
            <div class="spoiler-overlay"><span class="reveal-btn">&#128274; Result hidden</span></div>
        </c:if>
    </div>
    <div class="video-info">
        <h3>
            <c:choose>
                <c:when test="${hide}">Result hidden to prevent spoilers</c:when>
                <c:otherwise><c:out value="${v.title}"/></c:otherwise>
            </c:choose>
        </h3>
        <div class="video-meta"><c:out value="${v.sportName}"/> &bull; ${v.viewCount} views</div>
    </div>
</a>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- One-time message stored by WebUtil.flash(...) in the servlet; shown once, then removed. --%>
<c:if test="${not empty sessionScope.flashMessage}">
    <c:set var="flashText" value="${sessionScope.flashMessage}"/>
    <c:set var="flashIsError" value="${fn:startsWith(flashText, 'Error') or fn:startsWith(flashText, 'Action') or fn:startsWith(flashText, 'Validation')
                                     or fn:startsWith(flashText, 'Could not') or fn:startsWith(flashText, 'Please') or fn:startsWith(flashText, 'Invalid')
                                     or fn:startsWith(flashText, 'You have already') or fn:containsIgnoreCase(flashText, 'cannot')
                                     or fn:containsIgnoreCase(flashText, 'must be') or fn:containsIgnoreCase(flashText, 'too long')}"/>
    <div class="flash ${flashIsError ? 'flash-error' : 'flash-success'}" role="status"><c:out value="${flashText}"/></div>
    <c:remove var="flashMessage" scope="session"/>
</c:if>

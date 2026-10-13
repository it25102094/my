<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Friendly error page (configured in web.xml) instead of the raw Tomcat stack trace --%>
<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${empty code ? 'Error' : code} — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="includes/navbar.jsp" %>
<section class="page-section">
    <div class="error-box card">
        <div class="code">${empty code ? 'Oops' : code}</div>
        <h2>
            <c:choose>
                <c:when test="${code == 404}">Page not found</c:when>
                <c:when test="${code == 403}">Access denied</c:when>
                <c:otherwise>Something went wrong</c:otherwise>
            </c:choose>
        </h2>
        <p style="margin: 10px 0 18px;">
            <c:choose>
                <c:when test="${not empty requestScope['jakarta.servlet.error.message'] and code != 500}"><c:out value="${requestScope['jakarta.servlet.error.message']}"/></c:when>
                <c:when test="${code == 404}">The page or video you are looking for does not exist.</c:when>
                <c:when test="${code == 403}">Your account does not have permission to open this page.</c:when>
                <c:otherwise>An unexpected error occurred. Please try again. (Check that MySQL is running and the database was created.)</c:otherwise>
            </c:choose>
        </p>
        <a href="${pageContext.request.contextPath}/home" class="btn-accent">Back to Home</a>
    </div>
</section>
<%@ include file="includes/footer.jsp" %>

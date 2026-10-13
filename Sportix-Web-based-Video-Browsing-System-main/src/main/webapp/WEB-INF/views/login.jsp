<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Log in — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="includes/navbar.jsp" %>
<section class="page-section">
    <form method="post" action="${pageContext.request.contextPath}/login" class="auth-card">
        <h2>Log in</h2>
        <%-- FIX: these success messages were never shown although the servlets redirect with them --%>
        <c:if test="${param.registered == 'true'}"><p class="flash flash-success">Account created! Please log in.</p></c:if>
        <c:if test="${param.resetSuccess == 'true'}"><p class="flash flash-success">Password reset successfully. Log in with your new password.</p></c:if>
        <c:if test="${param.deleted == 'true'}"><p class="flash flash-success">Your account was deleted.</p></c:if>
        <c:if test="${not empty error}"><p class="form-error"><c:out value="${error}"/></p></c:if>

        <input type="text" name="username" placeholder="Username or email" required value="<c:out value='${param.username}'/>">
        <input type="password" name="password" placeholder="Password" required>

        <button class="btn-accent" type="submit">Log in</button>
        <p class="video-meta"><a href="${pageContext.request.contextPath}/forgot-password" style="color:var(--color-accent-readable)">Forgot password?</a></p>
        <p class="video-meta">No account? <a href="${pageContext.request.contextPath}/register" style="color:var(--color-accent-readable)">Register</a></p>
        <p class="muted" style="border-top:1px solid var(--color-border); padding-top:10px;">
            Demo accounts (password <strong>Sportix@123</strong>): kasun_p (viewer), vihan_cm (content manager),
            thilini_ad (advertiser), nadun_adm (admin)
        </p>
    </form>
</section>
<%@ include file="includes/footer.jsp" %>

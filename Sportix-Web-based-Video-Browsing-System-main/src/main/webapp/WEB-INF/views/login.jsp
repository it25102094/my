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
        <c:if test="${not empty error}"><p class="form-error"><c:out value="${error}"/></p></c:if>

        <input type="text" name="username" placeholder="Username" required>
        <input type="password" name="password" placeholder="Password" required>

        <button class="btn-accent" type="submit">Log in</button>
        <p class="video-meta">No account? <a href="${pageContext.request.contextPath}/register" style="color:var(--color-accent)">Register</a></p>
    </form>
</section>
<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
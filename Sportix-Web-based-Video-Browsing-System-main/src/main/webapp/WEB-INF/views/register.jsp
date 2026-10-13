<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create account — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="includes/navbar.jsp" %>
<section class="page-section">
    <form method="post" action="${pageContext.request.contextPath}/register" class="auth-card">
        <h2>Create account</h2>

        <c:if test="${not empty error}">
            <p class="form-error"><c:out value="${error}"/></p>
        </c:if>

        <input type="text" name="username" placeholder="Username" required>
        <input type="text" name="firstName" placeholder="First Name" required>
        <input type="text" name="lastName" placeholder="Last Name" required>
        <input type="email" name="email" placeholder="Email" required>
        <input type="password" name="password" placeholder="Password" required>

        <button class="btn-accent" type="submit">Register</button>

        <p class="video-meta">Already have an account? <a href="${pageContext.request.contextPath}/login" style="color:var(--color-accent)">Log in</a></p>
    </form>
</section>
<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
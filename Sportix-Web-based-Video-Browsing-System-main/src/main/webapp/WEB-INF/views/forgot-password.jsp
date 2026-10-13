<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <form method="post" action="${pageContext.request.contextPath}/forgot-password" class="auth-card">
        <h2>Reset Password</h2>
        <p class="video-meta">Enter your registered email and contact number to set a new password.</p>

        <c:if test="${not empty error}">
            <p class="form-error"><c:out value="${error}"/></p>
        </c:if>

        <input type="email" name="email" placeholder="Registered Email" required>
        <input type="text" name="contactNumber" placeholder="Registered Contact Number (e.g. 0771234561)" required>
        <input type="password" name="newPassword" placeholder="New Password (min 8 chars)" minlength="8" required>

        <button class="btn-accent" type="submit">Reset Password</button>

        <p class="video-meta">
            Remembered your password? <a href="${pageContext.request.contextPath}/login" style="color:var(--color-accent)">Log in</a>
        </p>
    </form>
</section>

<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
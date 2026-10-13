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

        <label style="font-size:0.85rem; font-weight:600;">I want to</label>
        <select name="accountType" id="accountType" onchange="toggleCompany()"
                style="padding: 11px 14px; border-radius: 8px; border: 1px solid var(--color-border); background: var(--color-surface-alt); color: var(--color-title);">
            <option value="USER" ${param.accountType != 'ADVERTISER' ? 'selected' : ''}>Watch sports videos (Viewer)</option>
            <option value="ADVERTISER" ${param.accountType == 'ADVERTISER' ? 'selected' : ''}>Advertise on Sportix (Advertiser)</option>
        </select>

        <input type="text" name="username" placeholder="Username (letters, numbers, _ .)" required minlength="3" maxlength="50" value="<c:out value='${param.username}'/>">
        <input type="text" name="firstName" placeholder="First Name" required maxlength="50" value="<c:out value='${param.firstName}'/>">
        <input type="text" name="lastName" placeholder="Last Name" required maxlength="50" value="<c:out value='${param.lastName}'/>">
        <input type="email" name="email" placeholder="Email" required maxlength="100" value="<c:out value='${param.email}'/>">
        <input type="text" name="contactNumber" placeholder="Contact Number (e.g. 0771234567) - used to reset your password" pattern="[0-9+ ]{9,15}" title="9-15 digits" value="<c:out value='${param.contactNumber}'/>">
        <input type="text" name="companyName" id="companyName" placeholder="Company / brand name (advertisers)" maxlength="120" value="<c:out value='${param.companyName}'/>">
        <input type="password" name="password" placeholder="Password (min 8 characters)" required minlength="8">

        <button class="btn-accent" type="submit">Register</button>

        <p class="video-meta">Already have an account? <a href="${pageContext.request.contextPath}/login" style="color:var(--color-accent-readable)">Log in</a></p>
    </form>
</section>
<script>
    function toggleCompany() {
        var advertiser = document.getElementById('accountType').value === 'ADVERTISER';
        var company = document.getElementById('companyName');
        company.style.display = advertiser ? '' : 'none';
        company.required = advertiser;
    }
    toggleCompany();
</script>
<%@ include file="includes/footer.jsp" %>

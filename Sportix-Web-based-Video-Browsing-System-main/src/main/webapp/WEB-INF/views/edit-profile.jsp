<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Profile — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="includes/navbar.jsp" %>

<section class="page-section">

    <!-- 1. Edit Profile Details (UPDATE CRUD) -->
    <form method="post" action="${pageContext.request.contextPath}/profile" class="auth-card">
        <input type="hidden" name="action" value="updateProfile">
        <h2>Edit Profile</h2>

        <c:if test="${not empty error}">
            <p class="form-error"><c:out value="${error}"/></p>
        </c:if>

        <input type="text" name="firstName" value="<c:out value='${user.firstName}'/>" placeholder="First Name" required>
        <input type="text" name="lastName" value="<c:out value='${user.lastName}'/>" placeholder="Last Name" required>
        <input type="text" name="contactNumber" value="<c:out value='${user.contactNumber}'/>" placeholder="Contact Number">
        <input type="text" name="favouriteSports" value="<c:out value='${user.favouriteSports}'/>" placeholder="Favourite Sports (e.g. Cricket, Football)">

        <label style="display:flex; align-items:center; gap:8px; font-size:0.9rem;">
            <input type="checkbox" name="hideScores" value="true" ${user.hideScores ? 'checked' : ''} style="width:auto;">
            Hide Scores by default
        </label>

        <button class="btn-accent" type="submit">Save Changes</button>
        <a href="${pageContext.request.contextPath}/profile" class="btn-outline" style="text-align:center;">Cancel</a>
    </form>

    <!-- 2. Change Password (UPDATE CRUD) -->
    <form method="post" action="${pageContext.request.contextPath}/profile" class="auth-card">
        <input type="hidden" name="action" value="changePassword">
        <h2>Change Password</h2>

        <input type="password" name="currentPassword" placeholder="Current Password" required>
        <input type="password" name="newPassword" placeholder="New Password (min 8 chars)" required minlength="8">

        <button class="btn-outline" type="submit">Update Password</button>
    </form>

    <!-- 3. Delete Account (DELETE CRUD) -->
    <form method="post" action="${pageContext.request.contextPath}/profile" class="auth-card"
          onsubmit="return confirm('Are you sure you want to delete your account?');">
        <input type="hidden" name="action" value="deleteAccount">
        <h2>Delete Account</h2>
        <p class="video-meta">Permanently delete your account and saved lists.</p>
        <button class="btn-outline" type="submit" style="color:var(--color-live); border-color:var(--color-live);">
            Delete My Account
        </button>
    </form>

</section>

<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Advertiser Dashboard — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .dashboard-container {
            max-width: 800px;
            margin: 40px auto;
            padding: 30px;
            background: var(--color-surface);
            border-radius: 12px;
            border: 1px solid var(--color-border);
        }
        .form-group {
            margin-bottom: 20px;
            display: flex;
            flex-direction: column;
        }
        .form-group label {
            margin-bottom: 8px;
            font-weight: 600;
            color: var(--color-title);
        }
        .form-group input, .form-group select {
            padding: 12px;
            border-radius: 6px;
            border: 1px solid var(--color-border);
            background: var(--color-surface-alt);
            color: var(--color-text);
            font-family: inherit;
        }
        .flash-message {
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 25px;
            font-weight: 600;
        }
        .flash-success {
            background: rgba(16, 185, 129, 0.1);
            color: #10B981;
            border: 1px solid rgba(16, 185, 129, 0.3);
        }
        .flash-error {
            background: rgba(239, 68, 68, 0.1);
            color: #EF4444;
            border: 1px solid rgba(239, 68, 68, 0.3);
        }
        </button>
    </style>
</head>
<body>

<%@ include file="WEB-INF/views/includes/navbar.jsp" %>

<section class="page-section">
    <div class="dashboard-container">
        <h1 style="margin-bottom: 25px; color: var(--color-title); border-bottom: 2px solid var(--color-border); padding-bottom: 10px;">
            Advertiser Dashboard
        </h1>

        <%-- Notification Flash Banner --%>
        <c:if test="${not empty sessionScope.flashMessage}">
            <c:set var="isError" value="${sessionScope.flashMessage.startsWith('Error')}" />
            <div class="flash-message ${isError ? 'flash-error' : 'flash-success'}">
                <c:out value="${sessionScope.flashMessage}"/>
            </div>
            <c:remove var="flashMessage" scope="session"/>
        </c:if>

        <h2 style="margin-bottom: 20px; font-size: 1.25rem;">Schedule New Campaign</h2>

        <form action="${pageContext.request.contextPath}/campaign" method="post">
            <div class="form-group">
                <label for="campaignName">Campaign Name</label>
                <input type="text" id="campaignName" name="campaignName" required placeholder="e.g., Summer Cricket Gear Promotion">
            </div>

            <div class="form-group">
                <label for="priorityLevel">Priority Level</label>
                <select id="priorityLevel" name="priorityLevel" required>
                    <option value="STANDARD">Standard (Default visibility)</option>
                    <option value="PREMIUM">Premium (High visibility during major tournaments)</option>
                </select>
            </div>

            <div style="display: flex; gap: 20px;">
                <div class="form-group" style="flex: 1;">
                    <label for="startDate">Start Date</label>
                    <input type="date" id="startDate" name="startDate" required>
                </div>

                <div class="form-group" style="flex: 1;">
                    <label for="endDate">End Date</label>
                    <input type="date" id="endDate" name="endDate" required>
                </div>
            </div>

            <button type="submit" class="btn-accent" style="width: 100%; padding: 14px; border-radius: 8px; font-size: 1.05rem; font-weight: bold; margin-top: 10px; border: none; cursor: pointer;">
                Create Campaign
            </button>
        </form>
    </div>
</section>

<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
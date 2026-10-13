<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Member 2: Watch Later — saved videos (create/read/update/delete) and folders (create/read/update/delete) --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Watch Later — Sportix</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="includes/navbar.jsp" %>

<section class="page-section">
    <div class="section-header"><h2>Watch Later</h2></div>

    <%@ include file="includes/flash.jsp" %>

    <!-- Folder filter bar (READ folders) -->
    <div class="folder-bar">
        <a href="${pageContext.request.contextPath}/user/watchlater" class="${activeFolder == 0 ? 'active' : ''}">All saved</a>
        <c:forEach var="f" items="${folders}">
            <a href="${pageContext.request.contextPath}/user/watchlater?folder=${f.folderId}" class="${activeFolder == f.folderId ? 'active' : ''}">
                &#128193; <c:out value="${f.folderName}"/> (${f.itemCount})
            </a>
        </c:forEach>
    </div>

    <!-- Folder management (CREATE / UPDATE / DELETE folders) -->
    <div class="card" style="margin-bottom: 22px;">
        <h3>Folders</h3>
        <form method="post" action="${pageContext.request.contextPath}/user/watchlater" class="inline-form" style="margin-bottom: 10px;">
            <input type="hidden" name="action" value="createFolder">
            <input type="text" name="folderName" placeholder="New folder name, e.g. Weekend Highlights" maxlength="100" required>
            <button type="submit" class="btn-accent btn-sm">+ Create folder</button>
        </form>
        <c:forEach var="f" items="${folders}">
            <div class="team-row">
                <form method="post" action="${pageContext.request.contextPath}/user/watchlater" class="inline-form">
                    <input type="hidden" name="action" value="renameFolder">
                    <input type="hidden" name="folderId" value="${f.folderId}">
                    <input type="hidden" name="returnFolder" value="${activeFolder}">
                    <input type="text" name="folderName" value="<c:out value='${f.folderName}'/>" maxlength="100" required>
                    <button type="submit" class="btn-outline btn-sm">Rename</button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/user/watchlater" class="inline-form"
                      onsubmit="return confirm('Delete this folder? The videos inside stay in your Watch Later list.');">
                    <input type="hidden" name="action" value="deleteFolder">
                    <input type="hidden" name="folderId" value="${f.folderId}">
                    <button type="submit" class="link-danger">Delete folder</button>
                </form>
            </div>
        </c:forEach>
        <c:if test="${empty folders}"><p class="muted">No folders yet. Folders help you organise saved videos.</p></c:if>
    </div>

    <!-- Saved videos (READ / UPDATE / DELETE items) -->
    <div class="video-grid">
        <c:forEach var="v" items="${watchLaterVideos}">
            <div>
                <%@ include file="includes/video-card.jsp" %>
                <div class="saved-item-tools">
                    <c:if test="${not empty v.notes}"><span class="note">&#128221; <c:out value="${v.notes}"/></span></c:if>
                    <form method="post" action="${pageContext.request.contextPath}/user/watchlater" style="display:flex; flex-direction:column; gap:6px;">
                        <input type="hidden" name="action" value="updateItem">
                        <input type="hidden" name="itemId" value="${v.itemId}">
                        <input type="hidden" name="returnFolder" value="${activeFolder}">
                        <div class="inline-form" style="width:100%;">
                            <select name="folderId" style="flex:1;">
                                <option value="0">No folder</option>
                                <c:forEach var="f" items="${folders}">
                                    <option value="${f.folderId}" ${v.folderId == f.folderId ? 'selected' : ''}><c:out value="${f.folderName}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="inline-form" style="width:100%;">
                            <input type="text" name="notes" value="<c:out value='${v.notes}'/>" placeholder="Add a note" maxlength="255" style="flex:1;">
                            <button type="submit" class="btn-outline btn-sm">Save</button>
                        </div>
                    </form>
                    <form method="post" action="${pageContext.request.contextPath}/user/watchlater">
                        <input type="hidden" name="action" value="remove">
                        <input type="hidden" name="videoId" value="${v.videoId}">
                        <input type="hidden" name="returnFolder" value="${activeFolder}">
                        <button class="link-danger" type="submit">Remove from Watch Later</button>
                    </form>
                </div>
            </div>
        </c:forEach>
        <c:if test="${empty watchLaterVideos}">
            <p class="video-meta">Nothing saved here yet. Open a video and click "Save to Watch Later".</p>
        </c:if>
    </div>
</section>

<%@ include file="includes/footer.jsp" %>

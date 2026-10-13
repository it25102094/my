<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- it25102093 - Member 3: fields shared by add-video.jsp and edit-video.jsp.
     FIX: sports / categories / teams now come from the database (the old form offered
     "Athletics" = sport 6, which does not exist, had no Interview type, no category and no teams). --%>
<div class="form-group">
    <label>Video Title</label>
    <input type="text" name="title" class="form-control" required maxlength="200"
           placeholder="e.g. Messi Best Goals 2024" value="<c:out value='${video.title}'/>">
</div>

<div class="form-group">
    <label>YouTube Link (Watch, Shorts, or Live)</label>
    <input type="url" name="youtubeUrl" class="form-control" required maxlength="500"
           placeholder="e.g. https://www.youtube.com/watch?v=..." value="<c:out value='${video.sourceUrl}'/>">
</div>

<div class="form-group">
    <label>Description (Optional)</label>
    <textarea name="description" class="form-control" maxlength="2000"
              placeholder="Add some context about this video..."><c:out value="${video.description}"/></textarea>
</div>

<div style="display: flex; gap: 15px; flex-wrap: wrap;">
    <div class="form-group" style="flex: 1; min-width: 160px;">
        <label>Sport</label>
        <select name="sportId" id="sportSelect" class="form-control" required onchange="filterTeams()">
            <c:forEach var="s" items="${sports}">
                <option value="${s.sportId}" ${video.sportId == s.sportId ? 'selected' : ''}><c:out value="${s.sportName}"/></option>
            </c:forEach>
        </select>
    </div>

    <div class="form-group" style="flex: 1; min-width: 160px;">
        <label>Video Type</label>
        <select name="videoType" id="typeSelect" class="form-control" required onchange="syncCategory()">
            <option value="HIGHLIGHT" ${video.videoType == 'HIGHLIGHT' || empty video.videoType ? 'selected' : ''}>Highlights</option>
            <option value="LIVE" ${video.videoType == 'LIVE' ? 'selected' : ''}>Live Match</option>
            <option value="SHORT" ${video.videoType == 'SHORT' ? 'selected' : ''}>Shorts</option>
            <option value="INTERVIEW" ${video.videoType == 'INTERVIEW' ? 'selected' : ''}>Interview</option>
        </select>
    </div>

    <div class="form-group" style="flex: 1; min-width: 160px;">
        <label>Category</label>
        <select name="categoryId" id="categorySelect" class="form-control" required>
            <c:forEach var="cat" items="${categories}">
                <option value="${cat.categoryId}" ${video.categoryId == cat.categoryId ? 'selected' : ''}><c:out value="${cat.categoryName}"/></option>
            </c:forEach>
        </select>
    </div>
</div>

<div class="form-group">
    <label>Teams in this video <span style="font-weight:400;">(followers of these teams get a notification when the video is published)</span></label>
    <div class="checkbox-list" id="teamList">
        <c:forEach var="t" items="${teams}">
            <c:set var="checkedTeam" value="${video.teamIds.contains(t.teamId)}"/>
            <label data-sport="${t.sportId}">
                <input type="checkbox" name="teamIds" value="${t.teamId}" ${checkedTeam ? 'checked' : ''}> <c:out value="${t.teamName}"/>
            </label>
        </c:forEach>
    </div>
    <span id="noTeams" style="display:none; font-size:0.8rem; color:var(--color-text-muted);">No teams registered for this sport.</span>
</div>

<div class="form-group">
    <label>Match score (Optional, hidden when "Hide Scores" is on)</label>
    <input type="text" name="matchScore" class="form-control" maxlength="100"
           placeholder="e.g. SL 178/6 (20) vs IND 170/9 (20)" value="<c:out value='${video.matchScoreText}'/>">
</div>

<script>
    // Show only the teams of the selected sport
    function filterTeams() {
        var sport = document.getElementById('sportSelect').value;
        var visible = 0;
        document.querySelectorAll('#teamList label').forEach(function (label) {
            var match = label.getAttribute('data-sport') === sport;
            label.style.display = match ? '' : 'none';
            if (!match) label.querySelector('input').checked = false;
            if (match) visible++;
        });
        document.getElementById('noTeams').style.display = visible === 0 ? '' : 'none';
    }
    // Pick the matching default category when the video type changes
    function syncCategory() {
        var map = {LIVE: 'Live', HIGHLIGHT: 'Highlight', SHORT: 'Short', INTERVIEW: 'Interview'};
        var word = map[document.getElementById('typeSelect').value];
        var cat = document.getElementById('categorySelect');
        for (var i = 0; i < cat.options.length; i++) {
            if (cat.options[i].text.indexOf(word) >= 0) { cat.selectedIndex = i; break; }
        }
    }
    filterTeams();
    <c:if test="${empty video.categoryId or video.categoryId == 0}">syncCategory();</c:if>
</script>

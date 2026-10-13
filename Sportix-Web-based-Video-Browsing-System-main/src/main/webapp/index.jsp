<%-- Welcome file declared in web.xml (it was missing). Sends visitors to the HomeController. --%>
<% response.sendRedirect(request.getContextPath() + "/home"); %>

<%@ page import="us.mn.state.dot.secure.JsecRole;us.mn.state.dot.secure.JsecRole" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Manage Users in Groups</title>
    <g:javascript library="prototype" />
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
      <span class="menuButton"><g:link class="update" action="list" controller="jsecUserRoleRel">List User/Role Assignments</g:link></span>
    </div>
    <div class="body">
      <h1>Manage User Role Assignments</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <g:form style='width:300px;' method="post" onsubmit="selectAllOptions('Group.users');">
        <g:select from="${roleList}"
          name="role.id"
          optionKey="id"
          value="${role.id}"
          onchange="${remoteFunction(controller:'jsecUserRoleRel',
            action:'roleSelected',
            update:'templateResult',
            params:'\'role=\' + this.value' )}">
        </g:select>

<g:render template="userRoleManage"></g:render>
      </g:form>
    </div>
  </body>
</html>
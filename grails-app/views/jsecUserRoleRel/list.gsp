<%@ page import="us.mn.state.dot.secure.JsecUserRoleRel" %>


<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>MnROAD User/Role Relation List</title>
    </head>
    <body>
        <div class="nav">
          <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
          <span class="menuButton"><g:link class="create" action="create">New User/Role Relation</g:link></span>
          <span class="menuButton"><g:link class="edit" action="index" controller="jsecRole">Manage Groups</g:link></span>
          <span class="menuButton"><g:link class="update" action="manage" controller="jsecUserRoleRel">Manage Rights</g:link></span>
          <span class="menuButton"><g:link class="create" action="create" controller="jsecUser">New MnROAD User</g:link></span>
        </div>
        <div class="body">
            <h1>User/Role Relations List</h1>
            <g:if test="${flash.message}">
              <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                          <g:sortableColumn property="role" title="Role" />
                          <g:sortableColumn property="user" title="User" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${jsecUserRoleRelInstanceList}" status="i" var="jsecUserRoleRelInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="show" id="${jsecUserRoleRelInstance.id}">${fieldValue(bean:jsecUserRoleRelInstance, field:'role')}</g:link></td>
                            <td>${fieldValue(bean:jsecUserRoleRelInstance, field:'user')}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${JsecUserRoleRel.count()}" />
            </div>
        </div>
    </body>
</html>

<%@ page import="us.mn.state.dot.secure.JsecUser;us.mn.state.dot.secure.JsecUserRoleRel" %>


<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>MnROAD User List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New MnROAD User</g:link></span>
        </div>
        <div class="body">
            <h1>MnROAD User List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="username" title="Username" />
                        
                   	        <th>Roles</th>
                        %{----}%
                   	        %{--<g:sortableColumn property="passwordHash" title="Password Hash" />--}%
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${jsecUserInstanceList}" status="i" var="jsecUserInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${jsecUserInstance.id}">${fieldValue(bean:jsecUserInstance, field:'username')}</g:link></td>
                        
                            <td>${JsecUserRoleRel.userRoles(jsecUserInstance.username)}</td>
                        %{----}%
                            %{--<td>${fieldValue(bean:jsecUserInstance, field:'passwordHash')}</td>--}%
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${JsecUser.count()}" />
            </div>
        </div>
    </body>
</html>

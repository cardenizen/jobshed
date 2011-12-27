<%@ page import="us.mn.state.dot.secure.JsecRole" %>


<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>MnROAD Role List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New Role/Group</g:link></span>
        </div>
        <div class="body">
            <h1>Role/Group List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                   	       <g:sortableColumn property="id" title="Id" />
                           <g:sortableColumn property="name" title="Name" />
                           <g:sortableColumn property="inDirectory" title="In Directory" />
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${jsecRoleInstanceList}" status="i" var="jsecRoleInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="show" id="${jsecRoleInstance.id}">${fieldValue(bean:jsecRoleInstance, field:'id')}</g:link></td>
                            <td>${fieldValue(bean:jsecRoleInstance, field:'name')}</td>
                            <td>${fieldValue(bean:jsecRoleInstance, field:'inDirectory')}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${JsecRole.count()}" />
            </div>
        </div>
    </body>
</html>

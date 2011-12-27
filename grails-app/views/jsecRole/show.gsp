

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show JsecRole</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">JsecRole List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New JsecRole</g:link></span>
        </div>
        <div class="body">
            <h1>Show JsecRole</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:jsecRoleInstance, field:'id')}</td>
                            
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name">Name:</td>

                            <td valign="top" class="value">${fieldValue(bean:jsecRoleInstance, field:'name')}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name">From Directory?:</td>

                            <td valign="top" class="value">${fieldValue(bean:jsecRoleInstance, field:'inDirectory')}</td>

                        </tr>

                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${jsecRoleInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('This will also delete all assignments of users to this role. Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>



<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit JsecRole</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">JsecRole List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New JsecRole</g:link></span>
        </div>
        <div class="body">
            <h1>Edit JsecRole</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${jsecRoleInstance}">
            <div class="errors">
                <g:renderErrors bean="${jsecRoleInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${jsecRoleInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:jsecRoleInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:jsecRoleInstance,field:'name')}"/>
                                </td>
                            </tr>
<%--
                       This should not be normally be changed by the user
                       but is here until code is written to maintain it
--%>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label>In Directory?</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:jsecRoleInstance,field:'inDirectory','errors')}">
                                    <g:checkBox name="inDirectory" value="${jsecRoleInstance?.inDirectory}" ></g:checkBox>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('This will also delete all assignments of users to this role. Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>

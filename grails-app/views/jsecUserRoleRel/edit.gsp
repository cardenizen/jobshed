<%@ page import="us.mn.state.dot.secure.JsecRole; us.mn.state.dot.secure.JsecUser" %>


<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit JsecUserRoleRel</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">JsecUserRoleRel List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New JsecUserRoleRel</g:link></span>
        </div>
        <div class="body">
            <h1>Edit JsecUserRoleRel</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${jsecUserRoleRelInstance}">
            <div class="errors">
                <g:renderErrors bean="${jsecUserRoleRelInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${jsecUserRoleRelInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="role">Role:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:jsecUserRoleRelInstance,field:'role','errors')}">
                                    <g:select
                                            optionKey="id"
                                            from="${JsecRole.list()}"
                                            name="role.id"
                                            value="${jsecUserRoleRelInstance?.role?.id}" >
                                    </g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="user">User:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:jsecUserRoleRelInstance,field:'user','errors')}">
                                    <g:select
                                            optionKey="id"
                                            from="${JsecUser.list()}"
                                            name="user.id"
                                            value="${jsecUserRoleRelInstance?.user?.id}" >
                                    </g:select>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>

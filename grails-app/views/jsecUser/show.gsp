

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show MnROAD User</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">MnROAD User List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New MnROAD User</g:link></span>
            <span class="menuButton"><g:link class="edit" action="index" controller="jsecRole">Manage Groups</g:link></span>
        </div>
        <div class="body">
            <h1>Show MnROAD User</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:jsecUserInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Username:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:jsecUserInstance, field:'username')}</td>
                            
                        </tr>

                    <tr class="prop">
                        <td valign="top" class="name">Roles:</td>
                        <td valign="top" class="value">
                            <ul>
                            <g:each in="${roles}" var='rolerel'>
                                %{--<li>${name}</li>--}%
                              <li>
                                <g:def var="rid" value="${rolerel.role.id}"/>
                                <g:link 
                                        action="manage"
                                        params="['id':rid]"
                                        controller="jsecUserRoleRel">${rolerel.role.name}</g:link>
                              </li>
                            </g:each>
                            </ul>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${jsecUserInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>



<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create MnRoad Group/Role</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">MnRoad Group/Role List</g:link></span>
        </div>
        <div class="body">
            <h1>Create MnRoad Group/Role</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${jsecRoleInstance}">
            <div class="errors">
                <g:renderErrors bean="${jsecRoleInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
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
                                    <g:checkBox name="inDirectory" value="${fieldValue(bean:jsecRoleInstance,field:'inDirectory')}" ></g:checkBox>
                                </td>
                            </tr>

                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>

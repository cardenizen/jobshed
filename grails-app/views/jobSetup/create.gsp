<%@ page import="us.mn.state.dot.mnroad.JobSetup;us.mn.state.dot.mnroad.MrUtils" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'jobSetup.label', default: 'JobSetup')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${jobSetupInstance}">
            <div class="errors">
                <g:renderErrors bean="${jobSetupInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" >
              <input type=hidden name="parameterMap" value="${jobSetupInstance.parameterMap}"/>
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="jobName"><g:message code="jobSetup.jobName.label" default="Job Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: jobSetupInstance, field: 'jobName', 'errors')}">
                                    <g:textField name="jobName" size="50" value="${jobSetupInstance?.jobName}" />
                                </td>
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="cronExpr"><g:message code="jobSetup.cronExprlabel" default="Schedule" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: jobSetupInstance, field: 'cronExpr', 'errors')}">
                                  <g:textField name="cronExpr" value="${jobSetupInstance?.cronExpr}" />
                                  <g:if test="${jobSetupInstance.cronExpr}">${MrUtils.cronSchedule(jobSetupInstance.cronExpr)}</g:if>
                                  <g:set var="jid" value="${jobSetupInstance?.id}" />
                                  <g:set var="cexpr" value="${jobSetupInstance?.cronExpr}" />
                                  <div class="buttons" style="width:120px;">
                                    <span class="button">
                                      <mr:button class="edit" action="editCronExpr"
                                            params="['id':jid,'theExpression':cexpr]"
                                            value="Schedule" />
                                    </span>
                                  </div>
                                </td>
                            </tr>

                            %{--<tr class="prop">--}%
                                %{--<td valign="top" class="name">--}%
                                    %{--<label for="parameterMap"><g:message code="jobSetup.parameterMap.label" default="Parameter Map" /></label>--}%
                                %{--</td>--}%
                                %{--<td valign="top" class="value ${hasErrors(bean: jobSetupInstance, field: 'parameterMap', 'errors')}">--}%
                                    %{--<g:textArea name="parameterMap" rows="30" cols="100"--}%
                                            %{--value="${jobSetupInstance?.parameterMap}"/>--}%
                                %{--</td>--}%
                            %{--</tr>--}%
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>

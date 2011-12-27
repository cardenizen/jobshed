<%@ page import="us.mn.state.dot.mnroad.JobSetup;us.mn.state.dot.mnroad.MrUtils" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'jobSetup.label', default: 'JobSetup')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <g:javascript library="prototype" />
        <g:javascript>
          // Call the function 'init()' on the 'load' event of the window.
          Event.observe(window, 'load', init, false)

          function init() {
            JSONeditor.start('tree','jform','${jobSetupInstance.parameterMap}',true)
            Opera=(navigator.userAgent.toLowerCase().indexOf("opera")!=-1)
            Safari=(navigator.userAgent.toLowerCase().indexOf("safari")!=-1)
            Explorer=(document.all && (!(Opera || Safari)))
            Explorer7=(Explorer && (navigator.userAgent.indexOf("MSIE 7.0")>=0))

            if(Explorer7 && location.href.indexOf("file:")!=0){prompt("This is just to get input boxes started in IE7 - who deems them unsecure.","I like input boxes...")}
          }
        </g:javascript>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${jobSetupInstance}">
            <div class="errors">
                <g:renderErrors bean="${jobSetupInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${jobSetupInstance?.id}" />
                <g:hiddenField name="version" value="${jobSetupInstance?.version}" />
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
                              <g:if test="${jobSetupInstance.cronExpr}"><br>${MrUtils.cronSchedule(jobSetupInstance.cronExpr)}</g:if>
                              <g:set var="jobid" value="${jobSetupInstance?.id}" />
                              <g:set var="cexpr" value="${jobSetupInstance?.cronExpr}" />
                              <div class="buttons" style="width:120px;">
                                <span class="button">
                                  <mr:button class="edit" action="editCronExpr"
                                        params="['jobid':jobid,'theExpression':cexpr]"
                                        value="Edit Schedule" />
                                </span>
                              </div>
                            </td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name">
                              <label for="parameterMap"><g:message code="jobSetup.parameterMap.label" default="Parameter Map" /></label>
                            </td>
                            <td valign="top" class="value ${hasErrors(bean: jobSetupInstance, field: 'parameterMap', 'errors')}">
                              <g:textArea name="prettyParameterMap" rows="30" cols="100"
                                      value="${jobSetupInstance.prettyParameterMap()}"/>
                            </td>
                        </tr>

                        %{--<g:textArea name="parameterMap" rows="30" cols="100"--}%
                                %{--value="${jobSetupInstance.editableParameterMap()}"/>--}%

                        %{--<tr class="prop">--}%
                            %{--<td valign="top" class="name">--}%
                              %{--<div style="position:relative;top:10px;left:10px" id="tree"></div>--}%
                              %{--<div style="position:relative;top:10px;left:400px" id="jform"></div>--}%
                            %{--</td>--}%
                          %{--<td>--}%
                          %{--</td>--}%
                        %{--</tr>--}%


                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>

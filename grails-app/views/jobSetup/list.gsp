
<%@ page import="us.mn.state.dot.mnroad.MrUtils; us.mn.state.dot.mnroad.JobSetup" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'jobSetup.label', default: 'JobSetup')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
          <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
          <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
          <span class="menuButton"><g:link action="list" controller="quartz">Job Control</g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                          <g:sortableColumn property="jobName" title="${message(code: 'jobSetup.jobName.label', default: 'Job Name')}" />
                          <th>Job Status</th>
                          <th>Scheduler Status</th>
                          <th>Next Scheduled Time</th>
                          <th>Most Recent Result</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${jobSetupInstanceList}" status="i" var="jobSetupInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                          <td><g:link action="show" id="${jobSetupInstance.id}">${jobSetupInstance.jobName.substring(23)}</g:link></td>
                          <td>${jobsRunningMap.get(jobSetupInstance.id)}
                          </td>
                          <td>${schedulerStatusMap.get(jobSetupInstance.id)}</td>
                          <td>${MrUtils.nextScheduledTime(jobSetupInstance.cronExpr)}</td>
                          <td>${jrm.get(jobSetupInstance.id)}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${jobSetupInstanceTotal}" />
            </div>
        </div>
    </body>
</html>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="us.mn.state.dot.mnroad.MrUtils;org.quartz.JobExecutionContext;org.quartz.JobDetail" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>MnRoad Data Request Page</title>
    <style type="text/css">
      dl#jobinfo {width: 800px;}
      dl#jobinfo dt {float: left; width: 120px; background: #ffc;}
      dl#jobinfo dd {margin-left: 130px; margin-bottom: 2px;}
    </style>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
      <span class="button"><g:link action="show" id="${jobid}" controller="jobSetup">Show</g:link></span>
    </div>
    <div class="body">
      <h2>${jobTitle}</h2>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <g:form method="post">
        <div class="dialog">
          <dl id="jobinfo">
            <dt>Schedule:<hr></dt>
            <dd>
              <g:textField name="cronExpr" value="${cronExpr}" />
              <g:hiddenField name="jobName" value="${jsi?.jobName}" />
              <g:hiddenField name="jobid" value="${jobid}" />
              <g:if test="${cronExpr}"><br>${MrUtils.cronSchedule(cronExpr)}</g:if>
              <g:set var="jid" value="${jsi?jsi.id:jobid}" />
              <g:set var="cexpr" value="${cronExpr}" />
              <br>
              <div class="buttons" style="width:120px;">
                <span class="button">
                  <mr:button class="edit" action="editCronExpr"
                    params="['jobid':jid,'theExpression':cexpr]"
                    value="Edit Schedule" />
                </span>
              </div>
              <br>
            </dd>
            <g:if test="${mrj}">
            <dt>Most Recent Job:</dt>
            <dd>
              ${mrj.startTime},&nbsp;&nbsp;${mrj.duration},&nbsp;&nbsp;${mrj.result}
            </dd>
            </g:if>
            <g:if test="${runningJobs}">
              <dt>Executing Jobs:</dt>
              <dd>
                <ul>
                <g:each var="job" in="${runningJobs}">
                    <li>
                      ${job.jobDetail.name}
                      %{--<span class="button">--}%
                      %{--<g:actionSubmit value="Interrupt" action="interrupt" class="edit"/>--}%
                      %{--<g:hiddenField name="jobDetailName" value="${job.jobDetail.name}" />--}%
                      %{--<g:hiddenField name="jobDetailGroup" value="${job.jobDetail.group}" />--}%
                      %{--</span>--}%
                    </li>
                </g:each>
                </ul>
              </dd>
            </g:if>
            <dt><hr><b>RunNow:</b></dt>   <dd><hr>Executes the job immediately</dd>
            <dt><b>Resume/Pause:</b></dt> <dd>Enables/Disables the trigger that makes this job schedule active.</dd>
            <dt><b>Reschedule:</b></dt>   <dd>Resets the trigger timer but does not store it in the database.</dd>
          </dl>
        </div>
        <div class="buttons">
          <g:actionSubmit name="runNow" value="RunNow" action="runNow" class="edit"/>
          <g:if env="development">
            <g:actionSubmit name="testNow" value="TestNow" action="testNow" class="edit"/>
          </g:if>
          <g:if test="${pauseResume}">
            <g:if test="${pauseResume=='Pause'}">
              <span class="button">
              <g:actionSubmit name="${pauseResume}" value="Pause" action="pause" class="edit"/>
              </span>
            </g:if>
            <g:if test="${pauseResume=='Resume'}">
              <span class="button">
              <g:actionSubmit name="${pauseResume}" value="Resume" action="resume" class="edit"/>
              </span>
            </g:if>
          </g:if>
          <g:if test = "${cronExpr}">
            <span class="button">
            <g:actionSubmit value="Reschedule" action="reschedule" class="edit"/>
            </span>
          </g:if>
        </div>
      </g:form>
    </div>
  </body>
</html>
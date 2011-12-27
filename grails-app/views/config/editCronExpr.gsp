<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Quartz Cron Expression Builder (wjw465150@gmail.com)</title>
  <script src="http://java.com/js/deployJava.js"></script>
  <meta name="layout" content="main" />
  <g:javascript library="prototype" />
  <g:javascript>
    // Call the function 'init()' on the 'load' event of the window.
    Event.observe(window, 'load', waituntilok, false)
    function waituntilok() {
       if (document.cronApplet.isActive()) {
             doit();
             }
       else {
           settimeout(waituntilok(),5000)
           }
       }

    function doit() {
      var expr = document.a.theExpression.value
      document.cronApplet.put(expr)
      }
  </g:javascript> </head>
<body>
<div class="nav">
  <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
</div>
    <div id="pageBody">
        <script>
         var attributes = {
           name:'cronApplet',
           codebase:'.',
           code:'wjw.cron.ex.CronExprBldrApplet.class',
           archive:'../cronExprBldrApplet.jar',
           width:800,
           height:800
         } ;
         var parameters = {fontSize:16, labNumber: 6, year: 2006};
         var version = '1.6' ;
         deployJava.runApplet(attributes, parameters, version);
        </script>
      <g:form name="a" controller="config" action="index">
        <input type=hidden name="jobid" value="${jobid}"/>
        <input type=hidden name="theExpression" value="${theExpression}"/>
        <input type=hidden name="theSchedule"/>
        <div class="buttons" style="width:60px;text-align:center;">
          <span class="button">
            <!-- http://www.galiel.net/el/howto/jvjvs.html -->
            <input type=button value="Submit" onClick="document.a.theExpression.value = document.cronApplet.get();document.a.submit();"/>
          </span>
        </div>
      </g:form>
    </div>
</body>
</html>
<%--
  Created by IntelliJ IDEA.
  User: carr1den
  Date: Jun 19, 2009
  Time: 11:24:02 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <title>Not Authorized</title>
  </head>
  <body>
    <br>
    <br>
    <g:if test="${subject.authenticated}">
      You are logged in but not authorized for this operation.
    </g:if>
    <g:else>
      You do not appear to be logged in.
    </g:else>
    <br>
    <br>Did the left hand (i.e. the page that lured you into trying this operation)
    <br>forget to tell the right hand (i.e. the security system that is preventing the operation)
    <br>what is going on?
    <br>
    <br>Contact your application administrator for access.  Hit the "<-" or "Back" button to continue.
  </body>
</html>


<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create MnROAD User</title>         
  <script type="text/javascript">

    // Checks the corresponding role if "Include in Role" is selected.
    function roleRels(numroles, selectedIdx) {
      document.forms[0].elements["selectedRoles"].value = "";
      var roles = new Array(numroles);
      var rroles = new Array(numroles);
      for (var idx=0; idx < numroles; idx++)
      {
        var roleId='role'+idx
        var roleRefId='roleRef'+idx
        if (selectedIdx == idx) {
          document.getElementById(roleId).checked=document.getElementById(roleRefId).checked
        }
        roles[idx] = document.getElementById(roleId).checked ? true : false;
        rroles[idx] = document.getElementById(roleRefId).checked ? true : false;
      }

      var selectedRoles = "";
      for (x = 0; x < roles.length; x++) {
        if (roles[x]) {
          selectedRoles += "" + x + ",";
        }
      }
      if (selectedRoles.charAt(selectedRoles.length - 1) == ',') {
        selectedRoles = selectedRoles.substr(0, selectedRoles.length - 1)
      }
      document.forms[0].elements["selectedRoles"].value = selectedRoles;

      var selectedRoleRels = "";
      for (x = 0; x < rroles.length; x++) {
        if (rroles[x]) {
          selectedRoleRels += "" + x + ",";
        }
      }
      if (selectedRoleRels.charAt(selectedRoleRels.length - 1) == ',') {
        selectedRoleRels = selectedRoleRels.substr(0, selectedRoleRels.length - 1)
      }
      document.forms[0].elements["selectedRoleRels"].value = selectedRoleRels;
    }

    // Ensures that if "Include in Role" is selected that role will also be selected.
    function checkRoleRels(numroles, selectedIdx) {
      document.forms[0].elements["selectedRoles"].value = "";
      var roles = new Array(numroles);
      var rroles = new Array(numroles);
      for (var idx=0; idx < numroles; idx++)
      {
        var roleId='role'+idx;
        var roleRefId='roleRef'+idx;
        if (selectedIdx == idx) {
          if (document.getElementById(roleRefId).checked) {
            document.getElementById(roleId).checked=document.getElementById(roleRefId).checked
          }
          }
        roles[idx] = document.getElementById(roleId).checked ? true : false;
        rroles[idx] = document.getElementById(roleRefId).checked ? true : false;
        }

      var selectedRoles = "";
      for (x = 0; x < roles.length; x++) {
        if (roles[x]) {
          selectedRoles += "" + x + ",";
        }
      }
      if (selectedRoles.charAt(selectedRoles.length - 1) == ',') {
        selectedRoles = selectedRoles.substr(0, selectedRoles.length - 1)
      }
      document.forms[0].elements["selectedRoles"].value = selectedRoles;

      var selectedRoleRels = "";
      for (x = 0; x < rroles.length; x++) {
        if (rroles[x]) {
          selectedRoleRels += "" + x + ",";
        }
      }
      if (selectedRoleRels.charAt(selectedRoleRels.length - 1) == ',') {
        selectedRoleRels = selectedRoleRels.substr(0, selectedRoleRels.length - 1)
      }
      document.forms[0].elements["selectedRoleRels"].value = selectedRoleRels;
    }
  </script>

  </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">JsecUser List</g:link></span>
        </div>
        <div class="body">
            <h1>Create MnROAD User</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${jsecUserInstance}">
            <div class="errors">
                <g:renderErrors bean="${jsecUserInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <!-- | separated list of roles -->
              <g:hiddenField name="selectedRoles" value="" />
              <g:hiddenField name="selectedRoleRels" value="" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label width="100" for="username">Username:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:jsecUserInstance,field:'username','errors')}">
                                    <input type="text" id="username" name="username" value="${fieldValue(bean:jsecUserInstance,field:'username')}"/>
                                </td>
                            </tr> 
                            <tr><td colspan="2"><table><tbody>
                            <tr><th width="25%">Roles</th><th width="15%">MemberOf</th><th width="60%">Attributes</th></tr>
                            <tr>
                              <td>
                                <g:if test="${roles != null}">
                                    <g:each in="${roles}" status="i" var="role">
                                      <g:def value="${role in dbRoles}" var="thisrole"></g:def>
                                      <g:if test="${role in dbRoles}">
                                        <g:checkBox  name="role${i}"
                                                onclick="checkRoleRels(${roles.size}, ${i})"/>
                                      </g:if>
                                      <g:else>
                                      <g:checkBox  name="role${i}"
                                        disabled="true"/>
                                      </g:else>
                                      &nbsp;${role}<br>
                                    </g:each>
                                </g:if>
                              </td>
                              <td>
                                <g:if test="${roles != null}">
                                    <g:each in="${roles}" status="i" var="role">
                                        <g:checkBox  name="roleRef${i}" id="roleRef${i}"
                                        onclick="roleRels(${roles.size}, ${i})"
                                        />&nbsp;Include in Role<br>
                                    </g:each>
                                </g:if>
                              </td>
                              <td>
                                <g:if test="${attrs != null}">
                                    <g:each in="${attrs}" status="i" var="attr">
                                      ${attr}<br>
                                    </g:each>
                                </g:if>
                              </td>
                            </tr>
                            </tbody></table></td></tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                  <span class="button"><g:actionSubmit value="Lookup User" action="lookupUser"/></span>
                  <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    <script type="text/javascript" language="JavaScript">document.forms[0].elements['username'].focus();</script>
    </body>
</html>

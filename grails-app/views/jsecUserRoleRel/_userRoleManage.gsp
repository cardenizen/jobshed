<div id="templateResult">
<FIELDSET>
<LEGEND align="center">${roleName} Users</LEGEND><P>
<table>
  <th>Members</th><th>&nbsp;</th><th>Non-members</th>
  <tr>
  <td>
    <g:select optionKey="id"
      id="Group.users"
      from="${groupUsers}"
      name="Group.users"
      value="${user?.id}"
      multiple="multiple"
      size="10" />
  </td><td>  <!--  moveXxxx functions located in application.js -->
      <span style="float:left;vertical-align: middle; padding: 1px; padding-top: 1px; width: 30px;">
          <input value="&gt;"     type="button" onclick="moveSelectedOptions('Group.users', 'availableUsers');" />
          <input value="&gt;&gt;" type="button" onclick="moveAllOptions('Group.users', 'availableUsers');" />
          <input value="&lt;"     type="button" onclick="moveSelectedOptions('availableUsers', 'Group.users');" />
          <input value="&lt;&lt;" type="button" onclick="moveAllOptions('availableUsers', 'Group.users');" />
      </span>
  </td><td>
    <g:select optionKey="id"
        id="availableUsers"
        from="${availableUsers}"
        name="availableUsers"
        value="${user?.id}"
        multiple="multiple"
        size="10"
        />
  </td>
</tr></table>
</FIELDSET>
<div class="buttons">
  <span class="button"><g:actionSubmit class="save" value="Update" action="updateGroupUsers"/></span>
</div>
</div>


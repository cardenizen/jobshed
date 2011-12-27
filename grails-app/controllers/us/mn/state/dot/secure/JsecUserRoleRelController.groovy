package us.mn.state.dot.secure

class JsecUserRoleRelController {
    
    def index = { redirect(action:list,params:params) }
//    def index = {
//        JsecRole r = JsecRole.list()[0]
//        render(view:'manage',model:['role':r,'jsecUserRoleRelInstance':JsecUser.list()])
//    }

  def manage = {
    def m = [:]
    def r = (params.id)?JsecRole.get(Long.parseLong(params.id)):JsecRole.list()[0]
    m.put('role',r)
    m.put('roleList',JsecRole.list())

    def id = r.id
    def rn = r.name
    def usersInRole = getUsersInRole(rn)
    def otherUsers = getOtherUsers(usersInRole)
    m.put('roleName',rn)
    m.put('selectedRole',id)
    m.put('availableUsers',otherUsers)
    m.put('groupUsers',usersInRole)
    return m
  }

    // the delete, save and update actions only accept POST requests
    static def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ jsecUserRoleRelInstanceList: JsecUserRoleRel.list( params ) ]
    }

    def show = {
        def jsecUserRoleRelInstance = JsecUserRoleRel.get( params.id )

        if(!jsecUserRoleRelInstance) {
            flash.message = "JsecUserRoleRel not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ jsecUserRoleRelInstance : jsecUserRoleRelInstance ] }
    }

    def delete = {
        def jsecUserRoleRelInstance = JsecUserRoleRel.get( params.id )
        if(jsecUserRoleRelInstance) {
            jsecUserRoleRelInstance.delete()
            flash.message = "JsecUserRoleRel ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "JsecUserRoleRel not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def jsecUserRoleRelInstance = JsecUserRoleRel.get( params.id )

        if(!jsecUserRoleRelInstance) {
            flash.message = "JsecUserRoleRel not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ jsecUserRoleRelInstance : jsecUserRoleRelInstance ]
        }
    }

    def update = {
        def jsecUserRoleRelInstance = JsecUserRoleRel.get( params.id )
        if(jsecUserRoleRelInstance) {
            jsecUserRoleRelInstance.properties = params
            if(!jsecUserRoleRelInstance.hasErrors() && jsecUserRoleRelInstance.save()) {
                flash.message = "JsecUserRoleRel ${params.id} updated"
                redirect(action:show,id:jsecUserRoleRelInstance.id)
            }
            else {
                render(view:'edit',model:[jsecUserRoleRelInstance:jsecUserRoleRelInstance])
            }
        }
        else {
            flash.message = "JsecUserRoleRel not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
//        def jsecUserRoleRelInstance = new us.mn.state.dot.secure.JsecUserRoleRel()
//        jsecUserRoleRelInstance.properties = params
//        return ['jsecUserRoleRelInstance':jsecUserRoleRelInstance]
      redirect(action:index)
    }

    def save = {
        def jsecUserRoleRelInstance = new JsecUserRoleRel(params)
        if(!jsecUserRoleRelInstance.hasErrors() && jsecUserRoleRelInstance.save()) {
            flash.message = "JsecUserRoleRel ${jsecUserRoleRelInstance.id} created"
            redirect(action:show,id:jsecUserRoleRelInstance.id)
        }
        else {
            render(view:'create',model:[jsecUserRoleRelInstance:jsecUserRoleRelInstance])
        }
    }

    def roleSelected = {
      def id = Long.parseLong(params['role'])
      def rn = JsecRole.get(id).name
      def usersInRole = getUsersInRole(rn)
      def otherUsers = getOtherUsers(usersInRole)
      render template:"userRoleManage",model:['roleName':rn,'selectedRole':id,'availableUsers':otherUsers,'groupUsers':usersInRole]
    }

    def getOtherUsers(List uir) {
      def allUsers = JsecUser.list()
      def otherUsers = []
      allUsers.each {
        if (!isUserInList(it.username, uir)) {
          otherUsers.add(it)
        }
      }
      return otherUsers
    }

    def updateGroupUsers = {
      def msg = "Result: "
      def group = JsecRole.get( params.role.id )
      def users = []
      if(params.Group){
        if (params.Group.users instanceof java.lang.String) {
          users[0]=JsecUser.get(params.Group.users.toInteger())
        } else {
          users=JsecUser.getAll(params.Group.users*.toInteger())
        }
      }
      def dbUserList = getUsersInRole(group.name)
      // first add users that were appended
      def addedUsers = []
      users.each {
        if (!dbUserList.contains(it)) {
          addedUsers << it.username
          def jsecUserRoleRelInstance = new JsecUserRoleRel()
          jsecUserRoleRelInstance.user = it
          jsecUserRoleRelInstance.role = group
          jsecUserRoleRelInstance.save(flush:true)
        }
      }
      if (addedUsers.size() > 0) {
        msg += "User(s) ${addedUsers} added to role ${group.name}."
      }
      // then remove users that were excised
      def removedUsers = []
      def userNotInGroup = JsecUser.list() - users
      userNotInGroup.each {
        if (isUserInList(it.username, dbUserList)) {
          def criteria = JsecUserRoleRel.createCriteria()
          def username = it.username
          def roleRel = criteria.list {
            role { eq('name', group.name) }
            user { eq('username', username) }
          }
          if (roleRel) {
            if (roleRel.size() == 1) {
              removedUsers << it.username
              roleRel[0].delete(flush:true)
              dbUserList -= it
            }
          }
        }
      }
      if (removedUsers.size() > 0) {
        msg += "User(s) ${removedUsers} removed from role ${group.name}."
      }
      flash.message = msg

      redirect(action:index)
    }

    boolean isUserInList(String username, def l) {
      boolean rc = false
      l.each {
        if (it.username == username)
          rc = true
      }
      return rc
    }

    def getUsersInRole(rolename) {
      def userList = []
      def criteria = JsecUserRoleRel.createCriteria()
      def rolesNusers = criteria.list {
          role { eq('name', rolename) }
      }
      rolesNusers.each  {
        userList << it.user
      }
      return userList
    }
}

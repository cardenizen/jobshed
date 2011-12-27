package us.mn.state.dot.secure

import org.jsecurity.crypto.hash.Sha1Hash
import us.mn.state.dot.mnroad.MultiValuedMap

class JsecUserController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ jsecUserInstanceList: JsecUser.list( params ) ]
    }

    def show = {
        def jsecUserInstance = JsecUser.get( params.id )

        if(!jsecUserInstance) {
            flash.message = "JsecUser not found with id ${params.id}"
            redirect(action:list)
        }
        else {
          def roleRel =  JsecUserRoleRel.createCriteria().list {
            user { eq('username', jsecUserInstance.username) }
          }
          def roles = []
          if (roleRel) {
            roleRel.each {
              roles.add(it)
            }
          }
          return [ jsecUserInstance : jsecUserInstance, roles:roles ]
        }
    }

    def delete = {
        def jsecUserInstance = JsecUser.get( params.id )
        if(jsecUserInstance) {
            def inRoles = JsecUserRoleRel.findAllByUser(jsecUserInstance)
            def roleRelsDeleted = 0
            if (inRoles?.size() > 0) {
              inRoles.each {
                it.delete()
                roleRelsDeleted++
              }
            }
            jsecUserInstance.delete()
            flash.message = "JsecUser ${params.id} (and $roleRelsDeleted role relations) deleted"
            redirect(action:list)
        }
        else {
            flash.message = "JsecUser not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def jsecUserInstance = JsecUser.get( params.id )

        if(!jsecUserInstance) {
            flash.message = "JsecUser not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ jsecUserInstance : jsecUserInstance ]
        }
    }

    def update = {
        def jsecUserInstance = JsecUser.get( params.id )
        if(jsecUserInstance) {
            jsecUserInstance.properties = params
            if(!jsecUserInstance.hasErrors() && jsecUserInstance.save()) {
                flash.message = "JsecUser ${params.id} updated"
                redirect(action:show,id:jsecUserInstance.id)
            }
            else {
                render(view:'edit',model:[jsecUserInstance:jsecUserInstance])
            }
        }
        else {
            flash.message = "JsecUser not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def jsecUserInstance = new JsecUser()
        jsecUserInstance.properties = params
        return ['jsecUserInstance':jsecUserInstance]
    }

    static def ldapAttrs =["whenCreated"
            ,"objectCategory"
            ,"badPwdCount"
//            ,"memberOf"
            ,"badPasswordTime"
            ,"objectClass"
            ,"name"
            ,"description"
            ,"lastLogon"
            ,"accountExpires"
            ,"lockoutTime"
            ,"msNPAllowDialin"
            ,"cn"
            ,"logonCount"
            ,"displayName"
            ,"pwdLastSet"
            ,"userPrincipalName"
            ,"whenChanged"
            ,"lastLogonTimestamp"
            ,"distinguishedName"
            ,"loginShell"
            ,"sAMAccountName"]

    def lookupUser = {
      def jsecUserInstance = new JsecUser(username: params.username, passwordHash: new Sha1Hash("").toHex())
      Ad ad = new Ad()
      ad.configure(grailsApplication.config)
      if (ad.findUser(params.username) == null) {
        flash.message = "User ${params.username} not found in MnDOT directory. Try again with 'Create' to set up an exernal user."
        redirect(uri:"/jsecUser/create")
        return
      }
      def roleNames = []
      JsecRole.list().each {
        roleNames.add(it.name)  }

      def adRoles = ad.getUserRoles(params.username, true)
      def newRoles = adRoles - roleNames
      MultiValuedMap al = ad.getUserAttrs(params.username)
      def displayList = []
      al.theMap().keySet().each { key ->
        if (!(key in ldapAttrs))
          return
        if (al.getValue(key) instanceof String)
          displayList.add("1 value of ${key}: ${al.getValue(key)}.")
        else {
          displayList.add("${al.getValueCount(key)} values of ${key}: ${al.getValue(key)}.")
        }
      }
      String nras = adRoles.toString()
      session.newRoles = nras.substring(1,nras.size()-1)
      render(view:'create',model:[jsecUserInstance:jsecUserInstance
              ,roles:adRoles,dbRoles:newRoles, attrs:displayList])
    }

    def ldapUserService
  
    def save = {

      def newRoles = session.newRoles
      String[] sa = params['selectedRoles'].split(",")
      def roleIdxs = []
      sa.each{roleIdxs.add(it)}
      sa = params['selectedRoleRels'].split(",")
      def roleRelIdxs = []
      sa.each{roleRelIdxs.add(it)}
      JsecUser user = JsecUser.findByUsername(params.username.trim()) //ldapUserService.getUser(params.username)
      if (user != null) {
        flash.message = "User ${params.username.trim()} is already registered."
        redirect(action:show,id:user.id)
      }
      else {
        Long newId = ldapUserService.addUser(params.username, newRoles?.split(","), roleIdxs, roleRelIdxs)
        if (newId > 0L) {
          def newUser = JsecUser.get(newId)
          flash.message = "JsecUser ${newUser} created"

          redirect(action:show,id:newId)
        }
        else {
            flash.message = "Failed to create user ${params.username}."
            render(view:'create')
        }
      }

    }

}

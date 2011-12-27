package us.mn.state.dot.secure
class JsecUserRoleRel {
    JsecUser user
    JsecRole role

    String toString() {
      "${user.toString()}:${role.toString()}"
    }

    static mapping = {
      columns { id column:'id' }
      id generator:'sequence', params:[sequence:'JSEC_ID_SEQ']
    }

    static List userRoles(String userName) {
      def roleRel =  JsecUserRoleRel.createCriteria().list {
        user { eq('username', userName) }
      }
      def roles = []
      if (roleRel) {
        roleRel.each {
          roles.add(it)
        }
      }
      return roles
    }
}

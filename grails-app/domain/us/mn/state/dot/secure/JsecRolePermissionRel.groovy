package us.mn.state.dot.secure
class JsecRolePermissionRel {
    JsecRole role
    JsecPermission permission
    String target
    String actions

    static constraints = {
        actions(nullable: false, blank: false)
    }
  
    static mapping = {
      columns { id column:'id' }
      id generator:'sequence', params:[sequence:'JSEC_ID_SEQ']
    }
}

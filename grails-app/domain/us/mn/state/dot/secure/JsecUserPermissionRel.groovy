package us.mn.state.dot.secure
class JsecUserPermissionRel {
    JsecUser user
    JsecPermission permission
    String target
    String actions

    static constraints = {
        target(nullable: true, blank: false)
        actions(nullable: false, blank: false)
    }

    static mapping = {
      columns { id column:'id' }
      id generator:'sequence', params:[sequence:'JSEC_ID_SEQ']
    }
}

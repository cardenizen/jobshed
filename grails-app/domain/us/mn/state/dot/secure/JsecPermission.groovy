package us.mn.state.dot.secure
class JsecPermission {
    String type
    String possibleActions

    static constraints = {
        type(nullable: false, blank: false, unique: true)
        possibleActions(nullable:false, blank: false)
    }

    static mapping = {
      columns { id column:'id' }
      id generator:'sequence', params:[sequence:'JSEC_ID_SEQ']
    }
}

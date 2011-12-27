package us.mn.state.dot.secure
class JsecRole {
    String name
    Boolean inDirectory

    static constraints = {
      name(nullable: false, blank: false, unique: true)
      inDirectory(nullable: true)
    }

    def String toString() {
      name
    }

    static mapping = {
      columns { id column:'id' }
      id generator:'sequence', params:[sequence:'JSEC_ID_SEQ']
    }
}

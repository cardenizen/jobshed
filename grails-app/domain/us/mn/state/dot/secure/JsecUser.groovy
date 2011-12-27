package us.mn.state.dot.secure
class JsecUser {
    String username
    String passwordHash

    static constraints = {
        username(nullable: false, blank: false)
    }

    def String toString() {
      username
    }


    static mapping = {
      columns { id column:'id' }
      id generator:'sequence', params:[sequence:'JSEC_ID_SEQ']
    }
}

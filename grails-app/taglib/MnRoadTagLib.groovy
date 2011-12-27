class MnRoadTagLib {
    static namespace = "mr"

    def signOutLink={attrs ->
        out << render(template:"/templates/common/signoutLinkTemplate")
    }

  def button = { attrs ->
    def writer = out
    writer << '<input type="button" onclick="location.href=\''
    // create the link
    if(request['flowExecutionKey']) {
      if(!attrs.params) attrs.params = [:]
      attrs.params."_flowExecutionKey" = request['flowExecutionKey']
    }

    writer << createLink(attrs).encodeAsHTML()
    writer << '\'"'
    // process remaining attributes
    attrs.each { k,v ->
      writer << " $k=\"$v\""
    }
    writer << '/>'
  }
  
}

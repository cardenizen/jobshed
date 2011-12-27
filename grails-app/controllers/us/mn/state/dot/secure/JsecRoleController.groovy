package us.mn.state.dot.secure
class JsecRoleController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ jsecRoleInstanceList: JsecRole.list( params ) ]
    }

    def show = {
        def jsecRoleInstance = JsecRole.get( params.id )

        if(!jsecRoleInstance) {
            flash.message = "JsecRole not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ jsecRoleInstance : jsecRoleInstance ] }
    }

    def delete = {
        def jsecRoleInstance = JsecRole.get( params.id )
        if(jsecRoleInstance) {
            jsecRoleInstance.delete()
            flash.message = "JsecRole ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "JsecRole not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def jsecRoleInstance = JsecRole.get( params.id )

        if(!jsecRoleInstance) {
            flash.message = "JsecRole not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ jsecRoleInstance : jsecRoleInstance ]
        }
    }

    def update = {
        def jsecRoleInstance = JsecRole.get( params.id )
        if(jsecRoleInstance) {
            jsecRoleInstance.properties = params
            if(!jsecRoleInstance.hasErrors() && jsecRoleInstance.save()) {
                flash.message = "JsecRole ${params.id} updated"
                redirect(action:show,id:jsecRoleInstance.id)
            }
            else {
                render(view:'edit',model:[jsecRoleInstance:jsecRoleInstance])
            }
        }
        else {
            flash.message = "JsecRole not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def jsecRoleInstance = new JsecRole()
        jsecRoleInstance.properties = params
        return ['jsecRoleInstance':jsecRoleInstance]
    }

    def save = {
        def jsecRoleInstance = new JsecRole(params)
        if(!jsecRoleInstance.hasErrors() && jsecRoleInstance.save()) {
            flash.message = "JsecRole ${jsecRoleInstance.id} created"
            redirect(action:show,id:jsecRoleInstance.id)
        }
        else {
            render(view:'create',model:[jsecRoleInstance:jsecRoleInstance])
        }
    }
}

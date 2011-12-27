class SecurityFilters {
  def filters = {
    // Ensure that all controllers and actions require an authenticated user,
    // except for the "public" controller
    auth(controller: "*", action: "*") {
        before = {
            // Exclude the "public" controller.
            if (controllerName == "public")
            	return true
            // This just means that the user must be authenticated. He does
            // not need any particular role or permission.
            accessControl { true }
        }
    }
    // These groups of statements specify what JSecurity calls "requirements".
    // What JSecurity calls "assigned rights" are stored in the JSEC_USER_ROLE_REL
    // table of the database.

    // Delete, Edit, Update, Create, Save, List and Show
    console(controller: "console", action: "*") {
        before = {
            accessControl {
                role("MRL Administrators") ||
                  role("MRL Dfs IS Section")
            }
        }
    }
    // Delete, Edit, Update, Create, Save, List and Show
    Zapping(controller: "*", action: "(delete)") {
        before = {
            accessControl {
                role("MRL Administrators") ||
                  role("MRL Dfs IS Section")
            }
        }
    }
    // Edit, Update, Create, Save, List and Show
    Editing(controller: "*", action: "(edit|update)") {
        before = {
            accessControl {
                role("MRL Administrators") ||
                  role("MRL Dfs IS Section") ||
                  role("MRL Dfs Research Section")
//                  role("MRL USERS")
//                        java.lang.IllegalArgumentException: The 'type' parameter must be provided to 'permission()'

//                ||  permission(user: "user1", target: 'facility', actions: ['edit', 'update' ])
            }
        }
    }
    // Create, Save, List and Show
    Building(controller: "*", action: "create|save") {
        before = {
            accessControl {
                role("MRL Administrators") ||
                  role("MRL Dfs IS Section") ||
                  role("MRL Dfs Research Section") ||
                  role("MRL USERS")
            }
        }
    }
    // List and Show
    Display(controller: "*", action: "list|show") {
        before = {
            accessControl {
                role("MRL Administrators") ||
                  role("MRL Dfs IS Section") ||
                  role("MRL Dfs Research Section") ||
                  role("MRL USERS") ||
                  role("DOTemployees")
            }
        }
    }

    
  } // filters

}
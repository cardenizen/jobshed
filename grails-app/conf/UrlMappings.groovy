class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

      "/" (controller: 'jobSetup', action: 'index')
//      "/" (controller: 'jobControl', action: 'index')
		"500"(view:'/error')
	}
}

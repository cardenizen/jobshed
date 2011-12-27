package com.leebutts

class Environment {

    static environments = []

    static {
        environments << [id: 1, name: 'XE', prefix: 'local',
                          host: 'localhost']
        environments << [id: 2, name: 'MNRTest'
                , prefix: 'test'
                , host: 'MRL2K3dev.ad.dot.state.mn.us'
                , port: 1521
                , passwordRequired: true]
        environments << [id: 3, name: 'MNRProd', prefix: 'prod'
                , host: 'MRL2K3MRDB.ad.dot.state.mn.us'
                , user:'grails'
                , port: 1521
                , passwordRequired: true]

        //unique id check
        environments.each {env ->
            assert environments
                .findAll {it.id == env.id}.size() == 1}
    }

    static list() {
        return environments
    }
}


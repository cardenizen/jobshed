package us.mn.state.dot.mnroad

class DatabaseService {

    def TEST = "Test"
    def PRODUCTION = "Production"

    def dataSourceTest
    def dataSourceProduction

    boolean transactional = true

    def getDataSource(dbEnv) {
        switch(dbEnv) {
            case TEST:
                return dataSourceTest
            case PRODUCTION:
                return dataSourceProduction
        }
    }
}
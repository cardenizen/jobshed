package us.mn.state.dot.mnroad

import groovy.sql.Sql

class TableNameTests extends GroovyTestCase {

    def databaseService
//    def dataSource
    def grailsApplication

    Sql sql

    protected void setUp() {
      super.setUp()
      sql = Sql.newInstance(databaseService.getDataSource(databaseService.TEST))
      //sql = Sql.newInstance(dataSource)
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testTcTables2011() {

      def name = "TC_VALUES"
      if (grailsApplication.config.largeTables.contains(name.substring(0,2))) {
        assert MrUtils.tableName(sql, "TC_VALUES", 2011) == "TC_VALUES"
      }
    }

    void testTcTables2010() {

      def name = "TC_VALUES"
      if (grailsApplication.config.largeTables.contains(name.substring(0,2))) {
        assert MrUtils.tableName(sql, "TC_VALUES", 2010) == "TC_VALUES"
      }
    }

    void testTcTables2009() {

      def name = "TC_VALUES"
      if (grailsApplication.config.largeTables.contains(name.substring(0,2))) {
        assert MrUtils.tableName(sql, "TC_VALUES", 2009) == "TC_VALUES_2009"
      }
    }
}

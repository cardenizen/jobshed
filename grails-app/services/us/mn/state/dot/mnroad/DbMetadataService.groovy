package us.mn.state.dot.mnroad

import groovy.sql.Sql
import us.mn.state.dot.mnroad.data.ExportLayersJobParams
//import com.google.gson.Gson

class DbMetadataService {

    static transactional = true
    def dataSource

//    ArrayList getColumnDefs(String tableName) {
    Map getColumnDefs(String tableName) {
      Sql sql = new groovy.sql.Sql(dataSource)
      def cmap = [:]
      def names = []
      def types = []
      def nulls = []
      //def cdefs = []
      def rnum = "ROWNUM"
      def mdq = "SELECT C.*, ${rnum} FROM (SELECT * FROM ${tableName}) C WHERE ${rnum} < 2"
      sql.query(mdq.toString()) { rs ->
        def meta = rs.getMetaData()
        (1..meta.columnCount).each { col ->
          def type = ""
          def typeName = meta.getColumnTypeName(col)
          switch (typeName) {
          case 'NUMBER':
            type = (meta.getScale(col)==0)?"NUMBER(${meta.getPrecision(col)})":"NUMBER(${meta.getPrecision(col)},${meta.getScale(col)})"
            break
          case 'VARCHAR2':
            type = "VARCHAR2(${meta.getPrecision(col)})"
            break
          case 'DATE':
            type = "DATE"
            break
          case 'TIMESTAMP':
            type = (meta.getScale(col)==0)?"TIMESTAMP":"TIMESTAMP(${meta.getScale(col)})"
            break
          default:
            println "Unknown ColumnTypeName: ${meta.getColumnTypeName(col)} for column ${col}"
          }
        def cn = meta.getColumnName(col)
        if (cn != rnum) {
          names << cn.toString()
          types << type
          nulls << meta.isNullable(col)
          }
        }
      }
      cmap.put('names',names)
      cmap.put('types',types)
      cmap.put('nulls',nulls)
//      return cdefs
      return cmap
    }

//    def toJson(def jobInst) {
//      //def rc = [:]
//      def rc = ""
//
//      String cn = "${jobInst.jobName.substring(0,jobInst.jobName.lastIndexOf('.')+1)}data.${jobInst.jobName.substring(jobInst.jobName.lastIndexOf('.')+1)}Params"
//      println cn
////      def clazz = Class.forName(cn)
////      Gson gson = new Gson()
////      def paramsString = gson.toJson(this);
////      def params = gson.fromJson(paramsString,clazz)
//      def clazz = ExportLayersJobParams.class
//      Gson gson = new Gson()
//      def paramsString = gson.toJson(jobInst.toMap());
////      def params = gson.fromJson(paramsString,clazz)
//      if (paramsString)
//        rc = paramsString
//
//      return rc
//    }

}

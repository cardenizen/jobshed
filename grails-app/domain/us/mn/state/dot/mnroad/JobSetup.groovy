package us.mn.state.dot.mnroad

//import com.google.gson.Gson
import org.codehaus.groovy.grails.web.json.JSONObject
import com.sdicons.json.parser.JSONParser

class JobSetup {

    String  jobName
    String  cronExpr
    String  parameterMap

    Date    dateCreated
    Date    lastUpdated
    String  createdBy
    String  lastUpdatedBy

    def dbMetadataService
    
    static constraints = {
      parameterMap  (validator: { val, obj ->
        def rc = true
        switch (obj.jobName) {
          case "us.mn.state.dot.mnroad.ExportMaterialSamplesJob":
            rc = true //obj.validateMatTestColumns(obj.toMap())
            break
          case "us.mn.state.dot.mnroad.ExportDistressJob":
            rc = true //obj.validateDistressColumns(obj.toMap())
            break
        }
        return rc
      }   ) // end parameterMap constraint
    }

    private def validateDistressColumns(def config) {
      def propertyMessage = 'invalid.sql'
      def list = []
      list << propertyMessage
      if (config) {
        if (config["distressTableFolderMap"]) {
          def tableNames = config.distressTableFolderMap.keySet().collect {it.toLowerCase()} as List
          def aliasMap = config.columnAliasMap
          if (tableNames) {
            tableNames.each { tblName ->
              def searchTblName = "${tblName.toLowerCase().startsWith("mnr.") ? tblName.substring(4) : tblName}"
              def aliasMapEntry = aliasMap[searchTblName.toLowerCase()]
              def columnList = aliasMapEntry?.split(",")
              if (!columnList) {
                aliasMapEntry = aliasMap[searchTblName.toUpperCase()]
                columnList = aliasMapEntry?.split(",")
              }
              def cleanNames = []
              if (columnList) {
                columnList.each { pname ->
                  cleanNames << columnName(pname)
                }
                def schemaTableName = !tblName.toLowerCase().startsWith("mnr.")?"MNR.${searchTblName}":searchTblName
                def columns = dbMetadataService.getColumnDefs(schemaTableName)["names"]
                cleanNames.each {cname ->
                  def selectName = cname.toString()
                  if (!columns.contains(selectName)) {
                    list << "Invalid column: ${tblName}.${selectName}"
                  }
                }
              }
            }
          }
        }
      }
    if (list.size() > 1)
      return list
    else
      return true
    }

  private def validateMatTestColumns(def config) {
    def propertyMessage = 'invalid.sql'
    def list = []
    list << propertyMessage
    if (config) {
      if (config["materialTestsFileMap"]) {
        def tableNames = config.materialTestsFileMap.keySet().collect {it.toLowerCase()} as List
        if (tableNames) {
          tableNames.each { tblName ->
            def searchTblName = "${tblName.toLowerCase().startsWith("mnr.") ? tblName.substring(4) : tblName}"
            def columnList = config[searchTblName.toLowerCase()]
            if (!columnList)
              columnList = config[searchTblName.toUpperCase()]
            def cleanNames = []
            if (columnList) {
              columnList.each { pname ->
                cleanNames << columnName(pname)
              }
            }
            def columns = dbMetadataService.getColumnDefs(tblName)["names"]
            cleanNames.each {cname ->
              def selectName = cname.toString()
              if (!columns.contains(selectName)) {
                list << "Invalid column: ${tblName}.${selectName}"
              }
            }
          }
        }
      }
    }
    if (list.size() > 1)
      return list
    else
      return true
  }
    /*
    Accepts a string that may have a table qualifier and a column alias
    and returns just the column name
     */
    String columnName(pname) {
      def name = pname
      try {
        def n2 = pname.trim().split("\\s") // regex for any whitespace
        def n1 = (n2.size() > 1)?n2[0]:pname
        name = (n1.contains("."))?n1.substring(n1.indexOf(".") + 1).trim():n1
      } catch (ArrayIndexOutOfBoundsException aiobe) {
        println "${pname} index out of bounds"
      }
      return name.toString()
    }

    static mapping = {
      id generator:'sequence', params:[sequence:'JOBSEQ']
      columns {
        cronExpr      (nullable:true)
        parameterMap  (nullable:true, type:'text')
        createdBy     (nullable:true)
        lastUpdatedBy (nullable:true)
      }
    }

    String toString() {
      jobName
    }

    def toMap() {
      new JSONObject(parameterMap)
    }

    String prettyParameterMap() {
      def jv = new JSONParser(new ByteArrayInputStream(parameterMap.getBytes("UTF-8")))
      return jv.nextValue().render(true)
    }

    String prettyToJson(prettyJson) {
      def lines = prettyJson.split("\n")
      def al = []
      for (line in lines) {
          al << line.trim().replace(" :",":").replace(": ",":")
          }
      al.join("")
    }
}

/* Debug console
import us.mn.state.dot.mnroad.*

def jsi = JobSetup.get(3L)
def jobmap = jsi.toMap()
jobmap.each() { outsidemap ->
   if (outsidemap.value instanceof String) {
      println "${outsidemap.key}:  ${outsidemap.value} (${outsidemap.value.class})"
   } else if (outsidemap.value instanceof String[]) {
      println "${outsidemap}:  (${outsidemap.class})"
   } else {
      println "${outsidemap.key}: ${outsidemap.value.size()} entries."
      def insidemap = outsidemap.getValue()
      println insidemap
      insidemap.each() {
         if (it instanceof String) {
            println "${it}"
         } else {
            println "\t${it.key}: ${it.value}"
         }
      }
   }
}
"Done!"

*/
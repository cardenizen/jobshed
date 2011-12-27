package us.mn.state.dot.mnroad.data
/**
 * Created by IntelliJ IDEA.
 * User: Carr1Den
 * Date: Jun 8, 2011
 * Time: 7:32:20 AM
 */
class ExportFwdJobParams {
  String rdrive
  String dataProductDataFolder
  String ddByType
  String measuredDataFolder
  String physMeasureFolder
  String loadFolders
  LinkedHashMap<String,String> pavementTypes
  String overwriteFwdFiles
  Integer throughYear
  Integer batchSize
  String cellsQuery
  String fwdQuery
  String queryCriteria
  String queryOrder
  String countQuery
  String designNumQuery

  String toString() {
    "${rdrive}\n${dataProductDataFolder}\n${ddByType}\n${measuredDataFolder}\n${physMeasureFolder}\n${loadFolders}\n${overwriteFwdFiles}\n${throughYear}\n${batchSize}\n${cellsQuery}\n{fwdQuery}\n${queryCriteria}\n${queryOrder}\n${countQuery}\n${designNumQuery}\n${pavementTypes}"
  }
}

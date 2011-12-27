package us.mn.state.dot.mnroad.data

/**
 * Created by IntelliJ IDEA.
 * User: Carr1Den
 * Date: Jun 8, 2011
 * Time: 10:06:39 AM
 */
class ExportDcpJobParams {
  String rdrive
  String dataProductDataFolder
  String ddByType
  String measuredDataFolder
  String physMeasureFolder
  String overwriteDcpFiles
  Integer throughYear
  String dcp_folder
  String moisture_content_folder
  String cellsQuery
  String dcpQuery
  String unbound_field_moistureQuery
  LinkedHashMap<String,String> pavementTypes

  String toString() {
    "${rdrive}\n${dataProductDataFolder}\n${ddByType}\n${measuredDataFolder}\n${physMeasureFolder}\n${overwriteDcpFiles}\n${throughYear}\n${dcp_folder}\n${moisture_content_folder}\n${cellsQuery}\n${dcpQuery}\n${unbound_field_moistureQuery}\n${pavementTypes}"
  }
}

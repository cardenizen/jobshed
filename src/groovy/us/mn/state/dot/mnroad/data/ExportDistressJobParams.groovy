package us.mn.state.dot.mnroad.data
/**
 * Created by IntelliJ IDEA.
 * User: Carr1Den
 * Date: Jun 8, 2011
 * Time: 9:53:30 AM
 */
class ExportDistressJobParams {
  String rdrive
  String dataProductDataFolder
  String ddByType
  String measuredDataFolder
  String physMeasureFolder
  LinkedHashMap<String,String> pavementTypes
  String overwriteDistressFiles
  Integer throughYear
  String distressCellQuery
  LinkedHashMap<String,String> distressTableFolderMap
  LinkedHashMap<String,String> distressFileNameMap
  LinkedHashMap<String,String> columnAliasMap

  String toString() {
    "${rdrive}\n${dataProductDataFolder}\n${ddByType}\n${measuredDataFolder}\n${physMeasureFolder}\n${overwriteDistressFiles}\n${throughYear}\n${distressCellQuery}\n${pavementTypes}\n${distressTableFolderMap}\n${distressFileNameMap}\n${columnAliasMap}"
  }

}

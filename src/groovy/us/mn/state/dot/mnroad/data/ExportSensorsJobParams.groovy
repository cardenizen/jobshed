package us.mn.state.dot.mnroad.data
/**
 * Created by IntelliJ IDEA.
 * User: Carr1Den
 * Date: Jun 8, 2011
 * Time: 9:40:16 AM
 */
class ExportSensorsJobParams {

  String rdrive
  String dataProductDataFolder
  String sensorInfo
  String sensorByType
  LinkedHashMap<String,String> pavementTypes
  String overwriteSensorFiles
  String cellsQuery
  String sensorQuery
  String designNumQuery

  String toString() {
    "${rdrive}\n${dataProductDataFolder}\n${sensorInfo}\n${sensorByType}\n${overwriteSensorFiles}\n${cellsQuery}\n${sensorQuery}\n${designNumQuery}\n${pavementTypes}"
  }
}

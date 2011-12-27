package us.mn.state.dot.mnroad.data

/**
 * Created by IntelliJ IDEA.
 * User: Carr1Den
 * Date: Jun 8, 2011
 * Time: 9:34:47 AM
 * To change this template use File | Settings | File Templates.
 */
class ExportLayersJobParams {
  String rdrive
  String dataProductDataFolder
  String ddByType
  LinkedHashMap<String,String> pavementTypes
  String overwriteLayersFiles
  String cellsQuery
  String AggCellQuery
  String CompositeCellQuery
  String HmaCellQuery
  String PccCellQuery
  String designNumQuery

  String toString() {
    "${rdrive}\n${dataProductDataFolder}\n${ddByType}\n${overwriteLayersFiles}\n${cellsQuery}\n${AggCellQuery}\n${CompositeCellQuery}\n${HmaCellQuery}\n${PccCellQuery}\n${designNumQuery}\n${pavementTypes}"
  }
}

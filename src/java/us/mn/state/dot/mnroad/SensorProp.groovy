package us.mn.state.dot.mnroad

import java.text.SimpleDateFormat;
/**
 * Created by IntelliJ IDEA.
 * User: Carr1Den
 * Date: Jun 30, 2011
 * Time: 1:34:43 PM
 */
class SensorProp implements Comparable{
  Integer pavementType
  Integer facility
  Integer cell
  Integer design
  Integer tree
  Integer seq
  Date    installedDate
  Date    removedDate
  Double  depth
  Integer depthGroup
  Double  station
  Double  offset
  Integer sensorIdx
  Date    cellFromdDate
  Date    cellToDate
  String  mat
  Double  thickness
  
  int compareTo(def other) {
    other.seq > seq
  }

  static def colNames = ["PavementType","Facility","Cell","Design","Tree","Seq","InstalledDate","RemovedDate","Depth","DepthGroup","Station","Offset","SensorIdx","CellFromdDate","CellToDate","Mat","Thickness"]

  SensorProp(  Integer  a_pavementType
, Integer  a_facility
, Integer  a_cell
, Integer  a_design
, Integer  a_tree
, Integer  a_seq
, Date     a_installedDate
, Date     a_removedDate
, Double   a_depth
, Integer  a_depthGroup
, Double   a_station
, Double   a_offset
, Integer  a_sensorIdx
, Date     a_cellFromdDate
, Date     a_cellToDate
, String   a_mat
, Double   a_thickness
) {
      pavementType  = a_pavementType
      facility      = a_facility
      cell          = a_cell
      design        = a_design
      tree          = a_tree
      seq           = a_seq
      installedDate = a_installedDate
      removedDate   = a_removedDate
      depth         = a_depth
      depthGroup    = a_depthGroup
      station       = a_station
      offset        = a_offset
      sensorIdx     = a_sensorIdx
      cellFromdDate = a_cellFromdDate
      cellToDate    = a_cellToDate
      mat           = a_mat
      thickness     = a_thickness
    }

  String toString() {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    String.format("%d,%d,%d,%d,%d,%d,%s,%s,%f,%d,%f,%f,%d,%s,%s,%s,%f"
      ,pavementType
      ,facility
      ,cell
      ,design
      ,tree
      ,seq
      ,df.format(installedDate)
      ,df.format(removedDate  )
      ,depth
      ,depthGroup
      ,station
      ,offset
      ,sensorIdx
      ,df.format(cellFromdDate)
      ,df.format(cellToDate)
      ,mat
      ,thickness    )
  }
}

package us.mn.state.dot.mnroad

import groovy.sql.Sql
import org.apache.log4j.Logger
import java.sql.SQLException

class SensorQueryService {

  static transactional = true
  static public String DATA_QUERY = "dataQuery"
  static public String FIXED_COLUMN_NAMES = "fixedColumns"
  static public String VALUE_COLUMN_NAMES = "valueColumns"
  static public String COUNT_QUERY = "countQuery"
  static public String ARGLIST = "arglist"
  static public String FILE_NAME = "fullyQualifiedFileName"

  def dataSource
  def grailsApplication
  static Logger log = Logger.getLogger(SensorQueryService.class)

  def tcQueryAll(Map jobParams) {
    def rc = []
//    Calendar c = Calendar.getInstance();
//    int nCells = 0
//    int fromYear
//    int toYear
//    try {
//      fromYear = Integer.parseInt(jobParams.get('fromYear')?:"1996")
//      toYear = Integer.parseInt(jobParams.get('toYear')?:"2010")
//    } catch (NumberFormatException nfe) {
//      return "NumberFormatException reading 'fromYear' or 'toYear' job parameters."
//    }
//    def rowsWritten = 0
//    def sql = Sql.newInstance(dataSource)
//    println "Params: fromYear: $fromYear, toYear: $toYear"
//
//    println jobParams.cellsQuery
//    def q = jobParams.cellsQuery
//    def rows = sql.rows(q.toString(),[]) as List
//    for (row in rows) {
//       println "${row}"
//    }

    Map cm = cellSensors(jobParams)
    rc << cm.result  
/*
    for (int year=fromYear; year<toYear; year++) {
      def ca = MrUtils.cellAttrs(sql,null,2010)
      for (cell in ca) {
        if (jobParams.get('fromCell')) {
          if (cell.CELL.intValue() < Integer.parseInt(jobParams.get('fromCell')))
            continue
        }
        if (jobParams.get('toCell')) {
          if (cell.CELL.intValue() > Integer.parseInt(jobParams.get('toCell')))
            continue
        }
        c.setTime(cell.FROM_DATE);
        int cellFromYear = c.get(Calendar.YEAR);
        c.setTime(cell.TO_DATE);
        int cellToYear = c.get(Calendar.YEAR);
        if (year >= cellFromYear && year <= cellToYear) {
          log.info "$year -> Cell $cell.CELL Design $cell.DESIGN $cellFromYear - $cellToYear"
          def qm = [:]
          try  {
              qm = queryMap(sql, cell.CELL.intValue(), year)
            if (!qm) {
              log.info "No data for Cell ${cell.CELL.intValue()} in table MNR.TC_VALUES_${year}"
            } else {
              String dir = MrUtils.mkdir(jobParams.get('rdrive'),jobParams.get('matlabInputDir'))
              if (dir) {
                String subdir = "Cell${cell.CELL.intValue()}".toString()
                dir = MrUtils.mkdir(dir,subdir)
                String fileName = "${year}${subdir}.csv"
                qm.put(FILE_NAME, "${dir}\\${fileName}".toString())
                def nrows = 0
                try {
                 log.info qm[DATA_QUERY].toString()
                 log.info qm[ARGLIST].toString()
                 def csvw = new TcCsvWriter()
                 nrows = csvw.writeResultSet(sql.getDataSource().getConnection(), qm)
                 log.info("$nrows written to $fileName")
                 rowsWritten += nrows
                } catch (Exception e) {
                 rc << e.message
                 e.printStackTrace()
                }
              }
            }
          } catch (SQLException sqle) {
            rc << "${sqle.message} \n"
          }
        }
      } // cells loop
    } // years loop
*/

//    rc <<  "${rowsWritten} row in ${nCells} cells => ${jobParams.get('matlabInputDir')}"
  return rc
  }

  Map queryMap(def sql, int cell, int year) {
    def rc = [:]
    def tableName = "TC_VALUES_${year}"
    def from_date = MrUtils.startOfYearSqlDate(year)
    def to_date = MrUtils.endOfYearSqlDate(year)
    def whereClause = "WHERE CELL = ? AND DAY BETWEEN ? AND ?"
    def psq = "SELECT UNIQUE SEQ FROM MNR.${tableName} WHERE CELL=? ORDER BY SEQ"
    def q = ""
    ArrayList columnNames = new ArrayList()
    ArrayList fixedColumnNames = new ArrayList()
    def ans = sql.rows(psq, [cell])
    if (ans) {
      def q1 = "SELECT CELL Cell, DAY Day, HOUR Hour, QHR Qhr, MINUTE Minute, QUALITY_FLAG QualityFlag,"
      fixedColumnNames.add("Cell")
      fixedColumnNames.add("Day")
      fixedColumnNames.add("Hour")
      fixedColumnNames.add("Qhr")
      fixedColumnNames.add("Minute")
      fixedColumnNames.add("QualityFlag")
      def q2 = " FROM (SELECT CELL,DAY,HOUR,QHR,MINUTE,QUALITY_FLAG,"
      def q3 = " FROM MNR.${tableName} ${whereClause} GROUP BY CELL,DAY,HOUR,QHR,MINUTE,QUALITY_FLAG) ORDER BY CELL,DAY,HOUR,QHR,MINUTE".toString()
      def a2 = []
      ans.each {row ->
        columnNames.add("s_${row.SEQ}".toString())
        a2 << "MIN(DECODE(SEQ,${row.SEQ},VALUE)) AS s_${row.SEQ}"
      }
      q = "${q1}${columnNames.join(',')}${q2}${a2.join(',')}${q3}"
    }
    if (q) {
      rc.put (DATA_QUERY,q.toString())
      rc.put (VALUE_COLUMN_NAMES, columnNames)
      rc.put (FIXED_COLUMN_NAMES, fixedColumnNames)
      rc.put (ARGLIST, [new Integer(cell),from_date,to_date])
    }
    return rc
  }

  /*
  Use annual tables for the large volume sensor types (TC, VW, WM, XV).
  These are created in the first half of the year when all of the data for the
  previous year is certain to be in the current year table.
   */
  String tableName(def name, def fr_year) {
    String rc = name
    String model = name.substring(0,2)
    if (grailsApplication.config.largeTables.contains(model)) {
      int currentYear = Calendar.getInstance().get(Calendar.YEAR)
      if (fr_year < currentYear-1) {
        rc = "${model}_VALUES_${fr_year}"
      }
    }
    return rc
  }

  Map cellSensors(def params) {
    def rm = [:]
    def cellSensors = []
    def sql = Sql.newInstance(dataSource)
    def q = params.cellsQuery
    println q
    try {
      def nSensors = 0
      def cellList = sql.rows(q.toString(),[]) as List//params.model]) as List
      println "${params.model}, ${q}"
      for (row in cellList) {
        println row
        def sensorList = sensors(params.model, params.sensorsQuery, row.CELL,row.FROM_DATE, row.TO_DATE)
        nSensors += sensorList.size()
        row.put(params.model,sensorList)
        cellSensors << row
//        logOutliers(params.model,row.CELL,row.FROM_DATE, row.TO_DATE,)
      }
      rm.put("data",cellSensors)
      rm.put("result","${cellSensors.size()} cells and ${nSensors} sensors found.")
    } catch (Exception e) {
      rm.put("result",e.message)
    }
    return rm
  }

  void logOutliers(model,cell,dtfr,dtto) {
    if (!dtto) {
      dtto = new java.sql.Date((new Date()).getTime())
    }
    def sql = Sql.newInstance(dataSource)
    def qo = "select count(*) NUM from MNR.${model}_values_all where cell=? and day not between ? and ?"
    println qo
    def cnt = sql.firstRow(qo,[cell,dtfr,dtto])
    if (cnt.NUM) {
      log.info "On cell ${cell}, ${cnt.NUM} TC_VALUES found outside of dates ${dtfr} to ${dtto}"
    }

  }

  List sensors(def model, def q, def cellNumber, def fdate, def tdate) {
    def rc = []
    def sql = Sql.newInstance(dataSource)
    if (!q || !sql) {
      return rc
    }
    if (!tdate) {
      tdate = new java.sql.Date((new Date()).getTime())
    }
    def ans = sql.rows("${q} order by seq",[model, model, fdate, tdate, cellNumber])
    ans.each {
      SensorProp sp = new SensorProp()
      sp.cell       = it.CELL.intValue()
      sp.type       = it.TYPE.intValue()
      sp.location   = it.LOCATION.intValue()
      sp.sensorIdx  = it.SENSOR_IDX.intValue()
      sp.station    = it.STATION.doubleValue()
      sp.offset     = it.OFFSET.doubleValue()
      sp.depth      = it.DEPTH.doubleValue()
      sp.seq        = it.SEQ.intValue()
      sp.mat        = it.MAT
      sp.thickness  = it.THICKNESS.intValue()
      sp.depthGroup = it.DEPTH_GROUP.intValue()
      sp.tree       = it.TREE.intValue()
      rc << sp
    }
    return rc
  }
}

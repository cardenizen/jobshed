package us.mn.state.dot.mnroad

import groovy.sql.Sql
import java.sql.SQLException

import org.codehaus.groovy.grails.web.json.JSONException

class TcSensorService {

  static transactional = true
  static String DATA_QUERY = "dataQuery"
  static String COUNT_QUERY = "countQuery"
  static String ARGLIST = "arglist"

  def dataSource
  def grailsApplication

  def tcQueryAll(Map jobParams) {
    def rc = []
    int nCells = 0
    def sql = Sql.newInstance(dataSource)
    int fromYear
    int toYear
    try {
      fromYear = Integer.parseInt(jobParams.get('fromYear')?:"1996")
      toYear = Integer.parseInt(jobParams.get('toYear')?:"2010")
    } catch (NumberFormatException nfe) {
      return "NumberFormatException reading 'fromYear' or 'toYear' job parameters."
    }
    for (int year=fromYear; year<toYear; year++) {
      def ans = cells(toYear)
      for (row in ans) {
        def tableName = "TC_VALUES_$year"
        def qm = queryMap(sql, row.CELL.intValue(), tableName, valueColumns(tableName, jobParams), java.sql.Date.valueOf(row.from_date), java.sql.Date.valueOf(row.to_date))
        String dir = MrUtils.mkdir(jobParams.get('rdrive'),jobParams.get('matlabInputDir'))
        if (dir) {
          String subdir = "Cell${row.CELL.intValue()}".toString()
          dir = MrUtils.mkdir(dir,subdir)
          String fileName = "${year}${subdir}.csv"
          def nrows = 0
          try {
            log.info qm[DATA_QUERY].toString()
            log.info qm[ARGLIST].toString()
            nrows = batch(qm[COUNT_QUERY], qm[DATA_QUERY], qm[ARGLIST], "${dir}\\${fileName}", null) // omit batchSize
          } catch (Exception e) {
            rc << e.message
          }
          rc << "${fileName} -> ${nrows}"
        }
      }
    }
  return "${nCells} => ${jobParams.get('matlabInputDir')} (${rc.join(',')})"
  }

  List valueColumns(def tableName, def jobParams) {
    def rc = []
    def tt = jobParams.get("valueColumnNames")
    def t = ""
    try {
      t = tt.get(tableName)
      rc = t.split(",")
    } catch(JSONException je) {
      rc << "VALUE"
    }
    return rc
  }

    Map queryMap(def sql, int cell, String tableName, ArrayList valueColumnNames, java.sql.Date from_date, java.sql.Date to_date) {
      def rc = [:]
      def whereClause = "WHERE cell = ? AND DAY BETWEEN ? and ?"
      def psq = "SELECT UNIQUE SEQ FROM MNR.SENSOR_COUNTS WHERE CELL=? AND SUBSTR(TABLE_NAME,1,9)=? AND ((FROM_DAY BETWEEN ? and ?) or (to_day between ? and ?)) order by seq"
      def q = ""
      def ans = sql.rows(psq, [cell, tableName.substring(0,9), from_date,to_date, from_date,to_date])
      if (ans) {
        def q1 = "SELECT CELL Cell, DAY Day, HOUR Hour, QHR Qhr, MINUTE Minute,"
        def q2 = " FROM (SELECT CELL,DAY,HOUR,QHR,MINUTE,"
        def q3 = " FROM MNR.${tableName} ${whereClause} GROUP BY CELL,DAY,HOUR,QHR,MINUTE) ORDER BY CELL,DAY,HOUR,QHR,MINUTE".toString()
        def a1 = []
        def a2 = []
        ans.each {row ->
          if (valueColumnNames.size()==1) {
            a1 << "s_${row.SEQ}"
            a2 << "MIN(DECODE(SEQ,${row.SEQ},VALUE)) AS \"s_${row.SEQ}\""
          }
          else {
            valueColumnNames.each { cn ->
              a1 << "${cn}_${row.SEQ}"
              a2 << "MIN(DECODE(SEQ,${row.SEQ},${cn})) AS ${cn}_${row.SEQ}"
            }
          }
        }
        q = "${q1}${a1.join(',')}${q2}${a2.join(',')}${q3}"
      }
      if (q) {
        rc.put (DATA_QUERY,q.toString())
        rc.put (COUNT_QUERY,"SELECT COUNT(*) ROW_COUNT FROM (SELECT COUNT(*) FROM MNR.${tableName} ${whereClause} GROUP BY CELL,DAY,HOUR,QHR,MINUTE)".toString())
        rc.put (ARGLIST, [cell,from_date,to_date])
      }
      return rc
    }


  long rowCount(def q, def arglist, Sql sql) {
    long rc = 0L
    try {
      def row = sql.firstRow(q,arglist)
      if (row?.ROW_COUNT) {
        rc = row.ROW_COUNT
      }
    } catch (SQLException sqle) {
      def msg = q?"Qry: ${q}\n":""
      println "Unable to get row count - ${msg}Msg: ${sqle.getMessage()}"
    }
    return rc
  }

  int batch (countQuery, dataQuery, arglist, fqfn, argBatchsize) {
    def rowcnt = 0
    int numrows = 0
    def iddf = new File(fqfn)
    FileWriter iddfw = new FileWriter(iddf, false)
    BufferedWriter bw = new BufferedWriter(iddfw)

    try {
      Sql sql = new groovy.sql.Sql(dataSource)
      if (countQuery) {
        rowcnt = rowCount(countQuery,arglist,sql)
        if (!rowcnt)
          return rowcnt
      }
      Map columnTypeMap = [:]
      Map scaleMap = [:]

      def writeRow={ row ->
        def rr = row.toRowResult()
        def a = []
        def nl = rr.keySet().toList()
        rr.eachWithIndex { it,i ->
          if (nl[i]!="RNUM") {
            Object o = rr[i]
            def typ = columnTypeMap.get(i+1)
            def scale = scaleMap.get(i+1)
            a << SqlDataFormat.formatSqlValue(o, typ, scale, false)
          }
        }
        bw.writeLine "${a.join(',')}"
        numrows++
      }

      def writeHeader = { meta ->
        def cn = [:]
        columnTypeMap.clear()
        (1..meta.columnCount).each {
          def colName = meta.getColumnLabel(it)
          if (colName != "RNUM") {
            cn.put(it,colName)
            columnTypeMap.put(it,meta.getColumnTypeName(it))
            scaleMap.put(it, meta.getScale(it))
            }
          }
        bw.writeLine "${cn.keySet().collect {cn.get(it)}.join(",")}"
       }

      def defaultBatchsize = 1000
      if (argBatchsize) {
        if (argBatchsize instanceof String) {
          try {
             defaultBatchsize = Integer.parseInt(argBatchsize)
          } catch (NumberFormatException nfe) {
            println "Ignoring batch size '${argBatchsize}'. Using 1000 instead."
          }
        } else {
          defaultBatchsize = argBatchsize
        }
      }

      def batchSize = rowcnt/10
      int numBatches = ((int)(rowcnt/batchSize)) + 1
      int batchNum = 0
      int fromOffset = 0
      print "Writing ${rowcnt} rows to ${fqfn} "
      while (batchNum < numBatches) { //} && batchNum < 2)  {
        def sqs = "SELECT * FROM ( SELECT C.*, ROWNUM RNUM FROM (${dataQuery} ) C WHERE ROWNUM <= ${fromOffset+batchSize} ) where rnum  >= ${fromOffset+1}"
        if (batchNum == 0) {
          sql.eachRow(sqs.toString(),arglist,writeHeader,writeRow)
        }
        else {
          sql.eachRow(sqs.toString(), arglist) { writeRow(it) }
        }
        bw.flush()
        fromOffset += batchSize
        batchNum++
        print ". "
        if (batchNum && batchNum%5 == 0 ) {//&& batchNum <= numBatches) {
          println  "Processing batch ${batchNum} of ${numBatches}."
        }
      }
      println "Done"
    } catch (Exception e) {
      println "Batch exception writing rows:${e.message}."
    } finally {
      if (bw) {
        bw.flush()
        bw.close()
        if (!rowcnt && iddf) {
          iddf.delete()
        }
      }
    }

  return numrows
  }

  /*
  Use annual tables for the large volume sensor types (TC, VW, WM, XV).
  These are created in the first half of the year when all of the data for the
  previous year is certain to be in the current year table.
   *
  String makeTableName(def name, def fr_year) {
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
*/

  List cells(def to_year) {
    def ra = []
    try {
      Sql sql = new groovy.sql.Sql(dataSource)
      def q = """
      SELECT ID,CELL,DESIGN_NUMBER DESIGN,CONSTRUCTION_BEGAN_DATE FROM_DATE,NVL(CELL_END_DATE,?) CELL_END_DATE,TO_NUMBER(TO_CHAR(CONSTRUCTION_BEGAN_DATE,'yyyy')) FROM_YEAR,TO_NUMBER(TO_CHAR(NVL(CELL_END_DATE, ?),'yyyy')) TO_YEAR
      FROM (SELECT ID,CELL,CELL_TYPE,CONSTRUCTION_BEGAN_DATE,CONSTRUCTION_ENDED_DATE,NVL(DEMOLISHED_DATE,LEAD(CONSTRUCTION_BEGAN_DATE-1,1,NULL) OVER (PARTITION BY CELL ORDER BY CONSTRUCTION_BEGAN_DATE)) CELL_END_DATE,DESIGN_NUMBER FROM (SELECT ID,CELL_NUMBER CELL,SUBSTR(CLASS,24) CELL_TYPE,(SELECT FIRST_LAYER_DATE FROM MNR.CELLS WHERE ID=C.ID) CONSTRUCTION_BEGAN_DATE,CONSTRUCTION_ENDED_DATE,DEMOLISHED_DATE,CASE WHEN LEAD(ID,1,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 1 WHEN LEAD(ID,2,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 2 WHEN LEAD(ID,3,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 3 WHEN LEAD(ID,4,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 4 WHEN LEAD(ID,5,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 5 ELSE 1 END DESIGN_NUMBER FROM MNR.CELL C))
      ORDER BY CELL, CONSTRUCTION_BEGAN_DATE
      """
      def eoyts = MrUtils.endOfYearSqlTimestamp(to_year)
      def rows = sql.rows(q.toString(),[eoyts,eoyts]) as List
      for (row in rows) {
         def designCell = [:]
         designCell.put("id",row.ID)
         designCell.put("cell",row.CELL)
         designCell.put("design",row.DESIGN)
         designCell.put("from_date",row.FROM_DATE)
         designCell.put("to_date",row.CELL_END_DATE)
         designCell.put("years", [row.FROM_YEAR..row.TO_YEAR])
         ra << designCell
      }
    } catch (SQLException sqle) {
      def msg = q?"Qry: ${q}\n":""
      println "Unable to get cells - ${msg}Msg: ${sqle.getMessage()}"
    }
    return ra
  }


}
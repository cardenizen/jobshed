package us.mn.state.dot.mnroad

import groovy.sql.Sql
import org.apache.log4j.Logger
import java.sql.SQLException
//import groovyx.gpars.GParsPool

class TcDataQualityService {

  static transactional = true
  static public String DATA_QUERY = "dataQuery"
  static public String FIXED_COLUMN_NAMES = "fixedColumns"
  static public String VALUE_COLUMN_NAMES = "valueColumns"
  static public String SEQ_QUERY_ERROR = "seqQueryError"
  static public String COUNT_QUERY = "countQuery"
  static public String ARGLIST = "arglist"
  static public String FILE_NAME = "fullyQualifiedFileName"

  def dataSource
  def grailsApplication
  static Logger log = Logger.getLogger(TcDataQualityService.class)

  def tcQueryAll(Map jobParams) {
    def rc = []
    def sql = Sql.newInstance(dataSource)
    List cellDesigns = getCellDesigns(sql)
    def rowsWritten = 0
    if (cellDesigns) {
      List tcTableNames = getAnnualTblNames(sql)
      String baseDir = MrUtils.mkdir(jobParams.get('rdrive'),jobParams.get('matlabInputDir'))
      for (d in cellDesigns) {
        int cell = d.cell
        String subdir = "Cell${d.cell}.${d.design}".toString()
        def dir = MrUtils.mkdir(baseDir,subdir)
        String sensorFileName = "${dir}/${subdir}.csv".toString()
        if (!(new File(sensorFileName)).exists()) {
          println "${writeTcSensors(cell, sensorFileName)} sensor written to ${sensorFileName}. "
        }
        java.sql.Date fromDate = new java.sql.Date(d.cellFromDate.getMillis())
        java.sql.Date toDate = new java.sql.Date(d.cellToDate.getMillis())
        try  {
          def ydm = yearDates(fromDate,toDate)
  //        println "Processing ${ydm.keySet().size()} years."
  //        GParsPool.withPool {
  //        ydm.keySet().eachParallel { year ->
          ydm.keySet().each { year ->
            print '.'
            def tableName = tableName('TC_VALUES',year)
            String fileName = "${year}${subdir}.csv"
            String annualFileName = "${dir}\\${fileName}".toString()
            if (!(new File(annualFileName)).exists() && tcTableNames.contains(tableName)) {
              def qm = queryMap(sql, cell, ydm.get(year).get(0), ydm.get(year).get(1), tableName)
              if (qm) {
                qm.put(FILE_NAME, annualFileName)
                def nrows = 0
                try {
                 def csvw = new TcCsvWriter()
                 nrows = csvw.writeResultSet(sql.getDataSource().getConnection(), qm)
                 log.info("$nrows rows -> ${annualFileName}")
                 rowsWritten += nrows
                } catch (Exception e) {
                 rc << e.message
                 e.printStackTrace()
                }
              }
            }
          }
        } catch (SQLException sqle) {
          if (!sqle.message.contains("table or view does not exist")) {
            throw new Exception(sqle.message)
          }
          rc << "${sqle.message} \n"
        }
      }
    }
    rc <<  "${rowsWritten} row in ${cellDesigns.size()} cells => ${jobParams.get('matlabInputDir')}"
    println rc
  return rc
  }

  protected def getCellDesigns(sql) {
    def cellDesigns = []
// DEVELOPMENT
//    def cdq = """
//SELECT CD.ID,CD.CELL,CD.DESIGN_NUMBER DESIGN,CD.TYPE CELL_TYPE
//,MIN(CD.first_layer_date) CELL_FROM_DATE,NVL(MAX(CD.to_DATE),SYSDATE) CELL_TO_DATE
//,MIN(S.DATE_INSTALLED) SENSOR_FROM_DATE,NVL(MAX(S.DATE_REMOVED),SYSDATE) SENSOR_TO_DATE
//,MIN(S.SEQ) SEQ_MIN,MAX(S.SEQ) SEQ_MAX,COUNT(*) NUM_SENSORS
//FROM MNR.CELL_SUMMARY CD JOIN MNR.LANE LN ON LN.CELL_ID=CD.ID JOIN MNR.LAYER LY ON LY.LANE_ID=LN.ID JOIN MNR.SENSOR S ON S.LAYER_ID=LY.ID JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID=SM.ID
//WHERE SM.MODEL='TC'
//GROUP BY CD.ID,CD.CELL,CD.DESIGN_NUMBER,CD.TYPE ORDER BY CD.CELL,CD.DESIGN_NUMBER
//    """
//  PRODUCTION (until CELL_SUMMARY is available in production
       def cdq = """
SELECT CD.ID,CD.CELL,CD.DESIGN_NUMBER DESIGN,CD.CELL_TYPE
,MIN(CD.FIRST_LAYER_DATE) CELL_FROM_DATE,NVL(MAX(CD.TO_DATE),SYSDATE) CELL_TO_DATE
,MIN(S.DATE_INSTALLED) SENSOR_FROM_DATE,NVL(MAX(S.DATE_REMOVED),SYSDATE) SENSOR_TO_DATE
,MIN(S.SEQ) SEQ_MIN,MAX(S.SEQ) SEQ_MAX,COUNT(*) NUM_SENSORS
FROM MNR.CELL_DESIGN CD JOIN MNR.LANE LN ON LN.CELL_ID=CD.ID JOIN MNR.LAYER LY ON LY.LANE_ID=LN.ID JOIN MNR.SENSOR S ON S.LAYER_ID=LY.ID JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID=SM.ID
WHERE SM.MODEL='TC'
GROUP BY CD.ID,CD.CELL,CD.DESIGN_NUMBER,CD.CELL_TYPE ORDER BY CD.CELL,CD.DESIGN_NUMBER
        """
    try {
      sql.eachRow(cdq.toString()) { row ->
        cellDesigns << new CellDesign(row.ID.intValue(), row.CELL.intValue(), row.DESIGN.intValue(), row.CELL_TYPE, row.CELL_FROM_DATE, row.CELL_TO_DATE, row.SENSOR_FROM_DATE, row.SENSOR_TO_DATE, row.SEQ_MIN.intValue(), row.SEQ_MAX.intValue(), row.NUM_SENSORS.intValue()
        )
      }
    } catch (SQLException sqle) {
      log.error(sqle.message)
      //return rc
    }
    return cellDesigns
  }

  protected int writeTcSensors(int cell, String sensorFileName) {
    def tcs = getTcSensors(cell)
    int nLines = 0
    if (tcs) {
      def tcsfw
      try {
        tcsfw = new FileWriter(new File(sensorFileName))
        tcsfw.write("${SensorProp.colNames.toString()[1..-2]}\n")
        tcs.each {sensorProp ->
          tcsfw.write("${sensorProp}\n")
          nLines++
        }
      } catch (Exception e) {
        if (tcsfw != null) {
          tcsfw.flush();
          tcsfw.close();
          tcsfw = null;
        }
      } finally {
        if (tcsfw != null) {
          tcsfw.flush();
          tcsfw.close();
        }
      }
    }
    return nLines
  }

  List getAnnualTblNames(def sql) {
    def ann
    def annualTableNames = []
    annualTableNames << "TC_VALUES"
    def tnq = "SELECT UNIQUE TABLE_NAME FROM DBA_IND_COLUMNS WHERE SUBSTR(TABLE_NAME,1,10)=? AND TABLE_OWNER in ('MNR') ORDER BY TABLE_NAME"
    try {
      ann = sql.rows(tnq, ["TC_VALUES_"])
      ann.each {
        def tn = it.TABLE_NAME
        if (!tn.endsWith('1992'))
          annualTableNames << tn
      }
    } catch (SQLException sqle) {
      log.info "Error getting table names: ${sqle.message}"
    }
    return annualTableNames
  }

    ArrayList getTcSensors(int cell) {
      def sensorProps = []
      def sql = Sql.newInstance(dataSource)
      try {
        def cdq // uses view MNR.CELL_SENSORS  
        //"SELECT SENSOR_ID,CELL_SEQ,DENSE_RANK() OVER (ORDER BY DATE_INSTALLED) CELL_DESIGN FROM (SELECT CS.SENSOR_ID,CS.CELL_ID,SEQ CELL_SEQ,DATE_INSTALLED FROM MNR.CELL_SENSORS CS WHERE CELL=? AND MODEL=?)"
        cdq = """
SELECT SENSOR_ID,CELL_SEQ,DENSE_RANK() OVER (ORDER BY DATE_INSTALLED) CELL_DESIGN FROM (SELECT CS.SENSOR_ID,CS.CELL_ID,SEQ CELL_SEQ,DATE_INSTALLED FROM (
-- MNR.CELL_SENSORS
-- from here
SELECT
CELLS.ID CELL_ID ,
CELLS.CELL ,
CELLS.DESIGN ,
CELLS.CELL_TYPE ,
CELLS.FROM_DATE ,
CELLS.CELL_END_DATE ,
LN.NAME LANE ,
M.BASIC_MATERIAL ,
ROUND((LY.THICKNESS/2.54),2) THICKNESS_IN ,
SM.MODEL ,
S.ID SENSOR_ID ,
S.SEQ ,
S.DATE_INSTALLED ,
S.DATE_REMOVED ,
S.SENSOR_DEPTH_IN
FROM
(
   SELECT
   ID ,
   CELL ,
   DESIGN_NUMBER DESIGN ,
   CELL_TYPE ,
   CONSTRUCTION_BEGAN_DATE FROM_DATE ,
   CELL_END_DATE CELL_END_DATE ,
   TO_NUMBER(TO_CHAR(CONSTRUCTION_BEGAN_DATE,'yyyy')) FROM_YEAR ,
   TO_NUMBER(TO_CHAR(CELL_END_DATE,'yyyy')) TO_YEAR
   FROM
   (
      SELECT
      ID ,
      CELL ,
      CELL_TYPE ,
      CONSTRUCTION_BEGAN_DATE ,
      CONSTRUCTION_ENDED_DATE ,
      CASE WHEN CELL=1 THEN NULL ELSE NVL(DEMOLISHED_DATE,LEAD(CONSTRUCTION_BEGAN_DATE-1,1,NULL) OVER (PARTITION BY CELL ORDER BY CONSTRUCTION_BEGAN_DATE)) END CELL_END_DATE ,
      DESIGN_NUMBER
      FROM
      (
         SELECT
         ID ,
         CELL_NUMBER CELL ,
         SUBSTR(CLASS,24) CELL_TYPE ,
         (SELECT FIRST_LAYER_DATE FROM MNR.CELLS WHERE ID=C.ID) CONSTRUCTION_BEGAN_DATE ,
         CONSTRUCTION_ENDED_DATE ,
         DEMOLISHED_DATE ,
         CASE WHEN LEAD
         (
            ID,1,NULL
         )
         OVER
         (
            PARTITION BY CELL_NUMBER
            ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
         )
         IS NULL THEN 1 WHEN LEAD
         (
            ID,2,NULL
         )
         OVER
         (
            PARTITION BY CELL_NUMBER
            ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
         )
         IS NULL THEN 2 WHEN LEAD
         (
            ID,3,NULL
         )
         OVER
         (
            PARTITION BY CELL_NUMBER
            ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
         )
         IS NULL THEN 3 WHEN LEAD
         (
            ID,4,NULL
         )
         OVER
         (
            PARTITION BY CELL_NUMBER
            ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
         )
         IS NULL THEN 4 WHEN LEAD
         (
            ID,5,NULL
         )
         OVER
         (
            PARTITION BY CELL_NUMBER
            ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
         )
         IS NULL THEN 5 ELSE 1 END DESIGN_NUMBER
         FROM MNR.CELL C
      )
   )
)
CELLS JOIN MNR.LANE LN ON LN.CELL_ID=CELLS.ID JOIN MNR.LAYER LY ON LY.LANE_ID=LN.ID JOIN MNR.SENSOR S ON S.LAYER_ID=LY.ID JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID=SM.ID JOIN MNR.MATERIAL M ON LY.MATERIAL_ID=M.ID
-- to here
) CS  WHERE CELL=? AND MODEL=?
)"""
        def cdr = sql.rows(cdq,[cell,"TC"])
        /*
        This is necessary because a sensor, specifically an embedded sensor, does not
        necessarily belong to the cell that its layer belongs to.  e.g. sensor sequences
         111-116 in cell 2 belong to design 2, not design 1.
         */
        def sensorToDesignMap = [:]
        cdr.each{
          sensorToDesignMap.put(it.SENSOR_ID.intValue(), [it.CELL_SEQ.intValue(),it.CELL_DESIGN.intValue()])
        }

        // uses views MNR.SENSOR_LOCATION
        //cdq = "SELECT SENSOR_ID,SEQ, DENSE_RANK() OVER (ORDER BY STATION,OFFSET) TREE FROM (SELECT * FROM MNR.SENSOR_LOCATION S WHERE S.CELL=? AND S.MODEL=?)"
        cdq = """
SELECT SENSOR_ID,SEQ, DENSE_RANK() OVER (ORDER BY STATION,OFFSET) TREE
FROM (SELECT * FROM
--MNR.SENSOR_LOCATION
(SELECT
C.CELL_NUMBER CELL,
SUBSTR (C.CLASS, 24) CLASS,
SM.MODEL,
S.STATION_FT STATION,
S.OFFSET_FT OFFSET,
S.SEQ,
M.BASIC_MATERIAL,
S.SENSOR_DEPTH_IN DEPTH,
S.DATE_REMOVED,
S.ORIENTATION,
C.ID CELL_ID,
L.ID LAYER_ID,
S.ID SENSOR_ID
FROM MNR.SENSOR S JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID = SM.ID JOIN MNR.LAYER L ON S.LAYER_ID = L.ID JOIN MNR.LANE LN ON L.LANE_ID = LN.ID JOIN MNR.CELL C ON LN.CELL_ID = C.ID JOIN MNR.MATERIAL M ON M.ID = L.MATERIAL_ID
ORDER BY CELL, MODEL, SEQ
)
S WHERE S.CELL=? AND S.MODEL=?)
        """
        cdr = sql.rows(cdq,[cell,"TC"])
        /*
        This is necessary because sensor tree is defined as those sensors withe the
        same cell, station, offset and model
         */
        def sensorToTreeMap = [:]
        cdr.each{
          sensorToTreeMap.put(it.SENSOR_ID.intValue(), [it.SEQ.intValue(), it.TREE.intValue()])
        }
        def today = new Date()
        def sq
/*
SELECT * FROM (
  SELECT
   CASE WHEN TYPE='HmaCell' THEN 1
    WHEN TYPE='PccCell' THEN 2
    WHEN TYPE='AggCell' THEN 3
    WHEN TYPE='CompositeCell' THEN 4
   END PAVEMENT_TYPE
  ,CASE WHEN LOCATION='Mainline' THEN 1
    WHEN LOCATION='Low Volume Road' THEN 2
    WHEN LOCATION='Farm Road' THEN 3
    When Location='MnRoad Parking Lot' THEN 4
    WHEN LOCATION='MnRoad Sidewalk' THEN 5
    WHEN LOCATION='Public Road' THEN 6
   END FACILITY
  ,X.DEPTH_GROUP
  ,X.SENSOR_ID
  ,X.SEQ
  ,X.DATE_INSTALLED SENSOR_INSTALLED_DATE
  ,X.DATE_REMOVED SENSOR_REMOVED_DATE
  ,X.DEPTH
  ,X.STATION
  ,X.OFFSET
  ,X.SENSOR_IDX
  ,X.CELL
  ,X.CELL_FROM_DATE
  ,CASE
    WHEN X.CELL_TO_DATE < X.CELL_FROM_DATE OR X.CELL_TO_DATE IS NULL THEN SYSDATE
    ELSE X.CELL_TO_DATE
   END CELL_TO_DATE
  ,X.MAT
  ,X.THICKNESS
  ,X.MODEL
  FROM MNR.CELL_SENSOR_PROPS X
  )
  WHERE CELL=? AND MODEL=?
  ORDER BY CELL, SEQ
         */
        sq = """
SELECT * FROM (
  SELECT
   CASE WHEN TYPE='HmaCell' THEN 1
    WHEN TYPE='PccCell' THEN 2
    WHEN TYPE='AggCell' THEN 3
    WHEN TYPE='CompositeCell' THEN 4
   END PAVEMENT_TYPE
  ,CASE WHEN LOCATION='Mainline' THEN 1
    WHEN LOCATION='Low Volume Road' THEN 2
    WHEN LOCATION='Farm Road' THEN 3
    When Location='MnRoad Parking Lot' THEN 4
    WHEN LOCATION='MnRoad Sidewalk' THEN 5
    WHEN LOCATION='Public Road' THEN 6
   END FACILITY
  ,X.DEPTH_GROUP
  ,X.SENSOR_ID
  ,X.SEQ
  ,X.DATE_INSTALLED SENSOR_INSTALLED_DATE
  ,X.DATE_REMOVED SENSOR_REMOVED_DATE
  ,X.DEPTH
  ,X.STATION
  ,X.OFFSET
  ,X.SENSOR_IDX
  ,X.CELL
  ,X.CELL_FROM_DATE
  ,CASE
    WHEN X.CELL_TO_DATE < X.CELL_FROM_DATE OR X.CELL_TO_DATE IS NULL THEN SYSDATE
    ELSE X.CELL_TO_DATE
   END CELL_TO_DATE
  ,X.MAT
  ,X.THICKNESS
  ,X.MODEL
  FROM
  --MNR.CELL_SENSOR_PROPS
  (
SELECT
T.CELL, T.TYPE, T.LOCATION , CASE WHEN T.CELL_END_DATE IS NOT NULL
AND T.DATE_INSTALLED > T.CELL_END_DATE THEN T.CELL_END_DATE+1 ELSE T.FIRST_LAYER_DATE END CELL_FROM_DATE ,
NVL(T.CELL_END_DATE,SYSDATE) CELL_TO_DATE ,
T.MAT ,
T.THICKNESS ,
T.MODEL ,
T.SENSOR_ID ,
T.SEQ ,
T.DATE_INSTALLED,
T.DATE_REMOVED,
ROW_NUMBER() OVER (PARTITION BY T.CELL ORDER BY T.STATION,T.OFFSET,T.DEPTH) SENSOR_IDX ,
T.STATION ,
T.OFFSET ,
T.DEPTH ,
T.DEPTH_GROUP
FROM
(
   SELECT
   C.CELL_NUMBER CELL ,
   SUBSTR(C.CLASS,24) TYPE ,
   F.NAME LOCATION ,
   DEPTH_GROUP ,
   DESCR DEPTH_GROUP_DESCR ,
   M.DESCRIPTION MAT ,
   LY.THICKNESS/25.4 THICKNESS ,
   CD.CONSTRUCTION_ENDED_DATE FIRST_LAYER_DATE ,
   CD.TO_DATE CELL_END_DATE ,
   SM.MODEL ,
   S.ID SENSOR_ID ,
   S.SEQ ,
   ROUND((S.SENSOR_DEPTH_IN/12),4) DEPTH ,
   S.DATE_INSTALLED ,
   S.DATE_REMOVED ,
   S.STATION_FT STATION ,
   S.OFFSET_FT OFFSET
   FROM MNR.CELL C JOIN MNR.LANE LN ON LN.CELL_ID=C.ID JOIN MNR.LAYER LY ON LY.LANE_ID=LN.ID JOIN MNR.SENSOR S ON S.LAYER_ID=LY.ID JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID=SM.ID JOIN MNR.MATERIAL M ON LY.MATERIAL_ID=M.ID JOIN MNR.ROAD_SECTION R ON C.ROAD_SECTION_ID=R.ID JOIN MNR.FACILITY F ON R.FACILITY_ID=F.ID
   JOIN
   -- MNR.CELL_SUMMARY
   (SELECT
ID ,
CELL,
FIRST_LAYER_DATE,
LAST_LAYER_DATE,
CONSTRUCTION_ENDED_DATE,
CELL_END_DATE TO_DATE,
TYPE,
DESIGN_NUMBER
FROM
(
   SELECT
   ID ,
   CELL,
   CELL_TYPE TYPE,
   FIRST_LAYER_DATE,
   LAST_LAYER_DATE,
   CONSTRUCTION_ENDED_DATE,
   NVL(DEMOLISHED_DATE,LEAD(FIRST_LAYER_DATE-1,1,NULL) OVER (PARTITION BY CELL ORDER BY FIRST_LAYER_DATE)) CELL_END_DATE,
   DESIGN_NUMBER
   FROM
   (
      SELECT
      ID ,
      CELL_NUMBER CELL ,
      SUBSTR(CLASS,24) CELL_TYPE ,
      (SELECT MIN(SL.CONSTRUCT_START_DATE) FROM MNR.CELL SC JOIN MNR.LANE SLN ON SLN.CELL_ID=SC.ID JOIN MNR.LAYER SL ON SL.LANE_ID=SLN.ID WHERE SC.ID=C.ID) FIRST_LAYER_DATE ,
      (SELECT MAX(SL.CONSTRUCT_START_DATE) FROM MNR.CELL SC JOIN MNR.LANE SLN ON SLN.CELL_ID=SC.ID JOIN MNR.LAYER SL ON SL.LANE_ID=SLN.ID WHERE SC.ID=C.ID) LAST_LAYER_DATE ,
      CONSTRUCTION_ENDED_DATE ,
      DEMOLISHED_DATE ,
      CASE WHEN LEAD
      (
         ID,1,NULL
      )
      OVER
      (
         PARTITION BY CELL_NUMBER
         ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
      )
      IS NULL THEN 1 WHEN LEAD
      (
         ID,2,NULL
      )
      OVER
      (
         PARTITION BY CELL_NUMBER
         ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
      )
      IS NULL THEN 2 WHEN LEAD
      (
         ID,3,NULL
      )
      OVER
      (
         PARTITION BY CELL_NUMBER
         ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
      )
      IS NULL THEN 3 WHEN LEAD
      (
         ID,4,NULL
      )
      OVER
      (
         PARTITION BY CELL_NUMBER
         ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
      )
      IS NULL THEN 4 WHEN LEAD
      (
         ID,5,NULL
      )
      OVER
      (
         PARTITION BY CELL_NUMBER
         ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
      )
      IS NULL THEN 5 ELSE 1 END DESIGN_NUMBER
      FROM MNR.CELL C
   )
)
)
   CD ON CD.ID=C.ID
   JOIN
   (SELECT
DEPTH_GROUP,DESCR,RNG_START,RNG_STOP
FROM
   --MNR.DEPTH_RANGE
(   SELECT
   1 DEPTH_GROUP,'0.000 - 0.110' DESCR, 0.0 RNG_START, 0.110 RNG_STOP
   FROM DUAL
   UNION
   (SELECT 2 DEPTH_GROUP,'0.111 - 0.190' DESCR, 0.111, 0.190 FROM DUAL)
   UNION
   (SELECT 3 DEPTH_GROUP,'0.191 - 0.350' DESCR, 0.191, 0.350 FROM DUAL)
   UNION
   (SELECT 4 DEPTH_GROUP,'0.351 - 0.450' DESCR, 0.351, 0.450 FROM DUAL)
   UNION
   (SELECT 5 DEPTH_GROUP,'0.451 - 0.550' DESCR, 0.451, 0.550 FROM DUAL)
   UNION
   (SELECT 6 DEPTH_GROUP,'0.551 - 0.850' DESCR, 0.551, 0.850 FROM DUAL)
   UNION
   (SELECT 7 DEPTH_GROUP,'0.851 - 1.250' DESCR, 0.851, 1.250 FROM DUAL)
   UNION
   (SELECT 8 DEPTH_GROUP,'1.251 - 1.650' DESCR, 1.251, 1.650 FROM DUAL)
   UNION
   (SELECT 9 DEPTH_GROUP,'1.651 - 2.480' DESCR, 1.651, 2.480 FROM DUAL)
   UNION
   (SELECT 10 DEPTH_GROUP,'2.481 - 3.590' DESCR, 2.481, 3.590 FROM DUAL)
   UNION
   (SELECT 11 DEPTH_GROUP,'3.591 - 4.590' DESCR, 3.591, 4.590 FROM DUAL)
   UNION
   (SELECT 12 DEPTH_GROUP,'4.591 - 5.700' DESCR, 4.591, 5.700 FROM DUAL)
   UNION
   (SELECT 13 DEPTH_GROUP,'5.701+' DESCR, 5.701, NULL FROM DUAL)
)
)
   R ON ROUND
   (
      (S.SENSOR_DEPTH_IN/12),4
   )
   BETWEEN NVL(R.RNG_START, ROUND((S.SENSOR_DEPTH_IN/12),4))
   AND NVL(R.RNG_STOP, ROUND((S.SENSOR_DEPTH_IN/12),4))
)
T JOIN
--MNR.SENSOR_SUM_BY_SEQ
(SELECT
C.CELL_NUMBER CELL ,
SM.MODEL ,
MIN(SEQ) SEQ_MIN ,
MAX(SEQ) SEQ_MAX ,
S.STATION_FT STATION ,
S.OFFSET_FT OFFSET ,
COUNT(*) NUM_SENSORS
FROM MNR.CELL C JOIN MNR.LANE LN ON LN.CELL_ID=C.ID JOIN MNR.LAYER LY ON LY.LANE_ID=LN.ID JOIN MNR.SENSOR S ON S.LAYER_ID=LY.ID JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID=SM.ID
GROUP BY C.CELL_NUMBER,SM.MODEL,S.STATION_FT, S.OFFSET_FT
ORDER BY C.CELL_NUMBER,MIN(SEQ)
)
D ON D.CELL=T.CELL
AND D.STATION=T.STATION
AND D.OFFSET=T.OFFSET
AND D.MODEL=T.MODEL
  )
  --MNR.CELL_SENSOR_PROPS
  X
  )
WHERE CELL=? AND MODEL=?
        """
        def l = sql.rows(sq,[cell,"TC"])
        l.each {
          def rd = it.SENSOR_REMOVED_DATE
          def ctd = it.CELL_TO_DATE
          int design = sensorToDesignMap.get(it.SENSOR_ID.intValue()).get(1)
          int tree =   sensorToTreeMap.get(it.SENSOR_ID.intValue()).get(1)

          def sp = new SensorProp (it.PAVEMENT_TYPE.intValue()
          ,it.FACILITY.intValue()
          ,it.CELL.intValue()
          ,design
          ,tree
          ,it.SEQ.intValue()
          ,new Date(it.SENSOR_INSTALLED_DATE.getTime())
          ,new Date(rd?rd.getTime():today.getTime())
          ,it.DEPTH.doubleValue()
          ,it.DEPTH_GROUP.intValue()
          ,it.STATION.doubleValue()
          ,it.OFFSET.doubleValue()
          ,it.SENSOR_IDX.intValue()
          ,new Date(it.CELL_FROM_DATE.getTime())
          ,new Date(ctd?ctd.getTime():today.getTime())
          ,it.MAT
          ,it.THICKNESS.doubleValue() )
          sensorProps << sp
          }
        } catch(SQLException sqle) {
          log.info sqle.message
        }
      return sensorProps
      }

  Map yearDates(def fromDate, def toDate) {
    def rc = [:]
    int fyear = fromDate.year<1900?fromDate.year+1900:fromDate.year
    int tyear = toDate.year<1900?toDate.year+1900:toDate.year
    if (tyear < 1993)
      return rc
    if (fyear==tyear) {
      rc.put(fyear,[fromDate,toDate])
    } else {
      rc.put(fyear,[fromDate, newSqlDate(Calendar.DECEMBER,31,fyear)])
      (fyear+1..tyear).each { yr ->
        if (yr == tyear)
          rc.put(yr,[newSqlDate(Calendar.JANUARY,1,yr), toDate])
        else
          rc.put(yr,[newSqlDate(Calendar.JANUARY,1,yr), newSqlDate(Calendar.DECEMBER,31,yr)])
      }
    }
    return rc
  }

  java.sql.Date newSqlDate(int month, int day, int year) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DATE, day);
    // normalize the object
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    return new java.sql.Date(calendar.getTime().getTime());

  }

  static public boolean includeTcDataQualityFlag = false

  Map queryMap(def sql, int cell, def from_date, def to_date, def tableName) {
    def rc = [:]
    def ans
    def queryError = ""
    def whereClause = "WHERE CELL = ? AND DAY BETWEEN ? AND ?"
    def psq = "SELECT UNIQUE SEQ FROM MNR.${tableName} ${whereClause} ORDER BY SEQ"
    ArrayList columnNames = new ArrayList()
    ArrayList fixedColumnNames = new ArrayList()
    try {
      ans = sql.rows(psq.toString(), [cell,from_date,to_date])
    } catch (SQLException sqle) {
      queryError = "Unable to determine sequence numbers in MNR.${tableName} for cell ${cell} DAY BETWEEN ${from_date} AND ${to_date}: ${sqle.message}"
      log.error  queryError
    }
    if (queryError)
      rc.put (SEQ_QUERY_ERROR,queryError.toString())

    def q = ""
    if (ans) {
      def q1 = ""
      def q2 = ""
      def q3 = ""
      fixedColumnNames.add("Cell")
      fixedColumnNames.add("Day")
      fixedColumnNames.add("Hour")
      fixedColumnNames.add("Qhr")
      fixedColumnNames.add("Minute")
      fixedColumnNames.add("QualityFlag")
      if (includeTcDataQualityFlag) {
        q1 = "SELECT CELL Cell, DAY Day, HOUR Hour, QHR Qhr, MINUTE Minute, QUALITY_FLAG QualityFlag,"
        q2 = " FROM (SELECT CELL,DAY,HOUR,QHR,MINUTE,QUALITY_FLAG,"
        q3 = " FROM MNR.${tableName} ${whereClause} GROUP BY CELL,DAY,HOUR,QHR,MINUTE,QUALITY_FLAG) ORDER BY CELL,DAY,HOUR,QHR,MINUTE".toString()
      } else {
        q1 = "SELECT CELL Cell, DAY Day, HOUR Hour, QHR Qhr, MINUTE Minute,"
        q2 = " FROM (SELECT CELL,DAY,HOUR,QHR,MINUTE,"
        q3 = " FROM MNR.${tableName} ${whereClause} GROUP BY CELL,DAY,HOUR,QHR,MINUTE) ORDER BY CELL,DAY,HOUR,QHR,MINUTE".toString()
      }

      def a2 = []
      ans.each {row ->
        columnNames.add("s_${row.SEQ}".toString())
        a2 << "MIN(DECODE(SEQ,${row.SEQ},VALUE)) AS s_${row.SEQ}"
      }
      q = "${q1}${columnNames.join(',')}${q2}${a2.join(',')}${q3}"
    }
    if (q) {
      def args=[new Integer(cell),from_date,to_date]
      rc.put (DATA_QUERY,q.toString())
      rc.put (VALUE_COLUMN_NAMES, columnNames)
      rc.put (FIXED_COLUMN_NAMES, fixedColumnNames)
      rc.put (ARGLIST, args)
      log.info "${args} ${q}"
    }
    return rc
  }

  /*
  Use annual tables for the large volume sensor types (TC, VW, WM, XV).
  These are created in the first half of the year when all of the data for the
  previous year is certain to be in the current year table.
  If name is one of these
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

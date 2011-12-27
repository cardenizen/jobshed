package us.mn.state.dot.mnroad

import org.codehaus.groovy.grails.commons.GrailsApplication

import grails.util.GrailsUtil
import java.text.SimpleDateFormat
import org.apache.log4j.Logger
import wjw.cron.ex.CronExpressionEx
import wjw.cron.ex.DateFormatUtil
import java.text.ParseException
import groovy.sql.Sql
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: carr1den
 * Date: Jul 30, 2009
 * Time: 7:55:36 AM
 * To change this template use File | Settings | File Templates.
 */

public class MrUtils {
  static Logger log = Logger.getLogger(MrUtils.class)

  static String grailsEnvironment() {
    String env = "envUnknown"
//check if production
    if ( GrailsUtil.getEnvironment().equals(GrailsApplication.ENV_PRODUCTION))
      {env="production"}
//check if development
    else if ( GrailsUtil.getEnvironment().equals(GrailsApplication.ENV_DEVELOPMENT))
      {env="development"}
//check if testing
    else if ( GrailsUtil.getEnvironment().equals(GrailsApplication.ENV_TEST) )
      {env="test"}
    return env
  }

/*
  * Given a date string in yyyy,mm,dd,hh format
  * return an SqlDate
   */
  static java.util.Date getDate(String ds)  {
    def dts = []
    dts = ds.trim().split(",")
    def yr = Integer.parseInt(dts[0])-1900
    def mo = Integer.parseInt(dts[1])-1
    def dy = Integer.parseInt(dts[2])
    def hr = Integer.parseInt(dts[3])-1
    return new Date(yr,mo,dy,hr,0,0)
  }

  static def getFormattedDate(String dateTimeString, String format) {
    def sdf = new SimpleDateFormat(format)
    Date dt
    if (!dateTimeString)
      return null
    try {
      dt = sdf.parse(dateTimeString)
    } catch (Exception iae) {
      def msg = "Unable to parse ${dateTimeString} as a Date: ${iae.message}"
      log.warn(msg)
      println msg
    }
    return dt
  }

  static String formatDate(java.util.Date dateTime, String format) {
    def sdf = new SimpleDateFormat(format)
    String dt = ""
    if (!dateTime)
      return dt
    try {

      dt = sdf.format(dateTime)
    } catch (Exception iae) {
      def msg = "Unable to format ${dateTime} of type ${dateTime.class.name} as a Date: ${iae.message}"
      log.warn(msg)
      println msg
    }
    return dt
  }

  static def formatTimestamp(java.sql.Timestamp dateTime, String format) {
    return formatDate(dateTime, format)
  }

  static Long toLong(String s) {
    Long rc = 0
    try {
      rc = Long.parseLong(s)
    } catch (NumberFormatException nfe) {
      log.warn("${nfe.message} - ${s}")
    }
    return rc
  }

  static Double roundTwo(Double d) {
    return round(d, 2)
  }

  static Double round(Double d, Integer scale) {
    if (!d)
      return 0.0
    def tenp = Math.pow(10.0, scale)
    return ((long)Math.round(d * tenp))/ tenp;
  }

  static String fqfn(base, dir) {
    (!base.endsWith(File.separator))?(base + File.separator + dir):(base + dir)
  }


  static String mkdir(String base,String dir) {
    String rc = ""
    def nds = (!base.endsWith(File.separator) && !dir.startsWith(File.separator))?(base + File.separator + dir):(base + dir)
    def ndf = new File(nds)
    if (ndf.exists()) {
      rc = nds
    } else {
      //println "Creating: '${nds}'."
      if (ndf.mkdir()) {
        //println "Created: '${nds}'."
        rc = nds
      } else  {
        //println "Unable to create: '${nds}'."
        log.info "Unable to create: '${nds}'."
        rc = base
      }
    }
    return rc
  }

  static String mkBranch(String base, String branch) {
    def dir = base
    def fns = branch.split("\\\\")
    if (fns && fns.length > 0) {
       for (fn in fns) {
          dir = MrUtils.mkdir(dir, fn)
       }
    }
    return dir.toString()
  }

  static quotedString(String val) {
    def nvl = val
    if (val == null)
      return ""
    if (val.contains("\'")) {
      def s = val
      nvl = s.replace("\'","\'\'")
    }
    if (val.contains("\"")) {
      def nv = (val.toString().indexOf('"')>-1)?val.replace('"','""'):"$val"
      nvl = ((nv.toString().indexOf('\r\n')>-1))?nv.replace("\r\n"," "):"$nv"
    }
    return '"' + nvl +'"'
  }

  static String formatSqlValueForCsv(Object val, String typ) {
    String rc = ""
    if (typ.equals("DATE") || (typ.startsWith("TIMESTAMP"))) {
      if (val == null)
        rc = ""
      else if ("${val}".endsWith("00:00:00.0")){
        rc="${val}".split(" ")[0]
      }
      else if (typ == "TIMESTAMP(6)") {
          def ov = val.stringValue()
          try {
          rc = ov[0..ov.lastIndexOf('.')+3] // truncate to thousandths of a second
          } catch (StringIndexOutOfBoundsException siob) {
            rc = ov //println "${ov} ${siob.message}"
          }
      }
      else
        rc = "${val}"
    }
    else if (typ=="CHAR" || typ=="VARCHAR2" ) {
      if (val == null)
        return "\"\""
      rc = "\"${val.trim()}\""
      if (val.contains("\'")) {
        def s = val
        def n = s.replace("\'","\'\'")
        rc = "\"$n\""
      }
      if (val.contains("\"'")) {
        def nv = (val.toString().indexOf('"')>-1)?val.replace('"','""'):"$val"
        def nv1 = ((nv.toString().indexOf('\r\n')>-1))?nv.replace("\r\n"," "):"$nv"
        rc =  '"' + nv1 +'"'
      }
    }
    else if (typ.equals("NUMBER")) {
      rc = val == null?"":val
    }
    else {
        rc = val?.toString()
        println "Unknown type: $typ"
    }
    return rc
  }

  static String nextScheduledTime(String ce) {
    if (!ce)
      return "";
    StringBuffer sb = new StringBuffer();
    CronExpressionEx exp = new CronExpressionEx(ce);
    java.util.Date dd = new java.util.Date();
    sb.append(DateFormatUtil.format("yyyy-MM-dd HH:mm:ss", exp.getNextValidTimeAfter(dd)))
    return sb.toString();
  }

  static String cronSchedule(String ce) {
    if (!ce)
      return "";
    StringBuffer sb = new StringBuffer();
    CronExpressionEx exp = null;
    try {
       exp = new CronExpressionEx(ce);
      java.util.Date dd = new java.util.Date();
//       sb.append(DateFormatUtil.format("yyyy-MM-dd HH:mm:ss", dd)).append("<br>");
       for (int i = 1; i <= 8; i++) {
         dd = exp.getNextValidTimeAfter(dd);
         sb.append(Integer.toString(i))
                 .append(": ")
                 .append(DateFormatUtil.format("yyyy-MM-dd HH:mm:ss", dd))
                 .append("<br>");
         dd = new java.util.Date(dd.getTime() + 1000);
       }
    } catch (ParseException e) {
       sb.append("Error parsing the cron expression: <br>")
         .append("\t").append(ce);
    }
    return sb.toString();
  }

  static java.sql.Date toSqlDate(int mm, int dd, int yy) {
      java.sql.Date javaSqlDate = new java.sql.Date(toDate(mm,dd,yy).getTime());
      return javaSqlDate;
  }

  static Date toDate(int mm, int dd, int yy) {
      // get a calendar using the default time zone and locale.
      Calendar calendar = Calendar.getInstance();

      // set Date portion to January 1, 1970
      calendar.set(Calendar.YEAR, yy);
      calendar.set(Calendar.MONTH, mm);
      calendar.set(Calendar.DATE, dd);

      // normalize the object
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      Date date = new Date(calendar.getTime().getTime());

      return date;
  }

  static java.sql.Timestamp endOfYearSqlTimestamp (targetYear) {
    return new java.sql.Timestamp(endOfYearSqlDate(targetYear).getTime())
  }

  static java.sql.Date endOfYearSqlDate (targetYear) {
    def cal = Calendar.instance
    java.sql.Date rc = new java.sql.Date(cal.getTime().getTime())
    def yr = cal.get(Calendar.YEAR)
    try {
      if (targetYear) {
        if (targetYear instanceof java.lang.String) {
          cal.set(Integer.parseInt(targetYear), Calendar.DECEMBER, 31,0,0,0)
          cal.set(Calendar.MILLISECOND, 0);
        }
        else if (targetYear instanceof java.lang.Integer) {
          cal.set(targetYear, Calendar.DECEMBER, 31,0,0,0)
          cal.set(Calendar.MILLISECOND, 0);
        }
      }
      rc = new java.sql.Date(cal.getTime().getTime())
    }
    catch (NumberFormatException nfe) {
      log.info("Unable to parse 'config.throughYear'.  Setting to current year: '${yr}'.")
    }
    return rc
  }

  static java.sql.Date startOfYearSqlDate (targetYear) {
    def cal = Calendar.instance
    java.sql.Date rc = new java.sql.Date(cal.getTime().getTime())
    def yr = cal.get(Calendar.YEAR)
    try {
      if (targetYear) {
        if (targetYear instanceof java.lang.String) {
          cal.set(Integer.parseInt(targetYear), Calendar.JANUARY, 1,0,0,0)
          cal.set(Calendar.MILLISECOND, 0);
        }
        else if (targetYear instanceof java.lang.Integer) {
          cal.set(targetYear, Calendar.JANUARY, 1,0,0,0)
          cal.set(Calendar.MILLISECOND, 0);
        }
      }
      rc = new java.sql.Date(cal.getTime().getTime())
    }
    catch (NumberFormatException nfe) {
      log.info("Unable to parse 'config.throughYear'.  Setting to current year: '${yr}'.")
    }
    return rc
  }

  static final def fls = ["false","f","no","n","0"]
  static final def tru = ["true","t","yes","y","1"]
  static boolean yesno (def a) {
    boolean yn = false
    if (a instanceof Boolean) {
      yn = a
    }
    if (a instanceof Integer) {
      yn = !a
    }
    if (a instanceof String) {
      if (fls.contains(a.toLowerCase()))
        yn = false
      if (tru.contains(a.toLowerCase()))
        yn = true
    }
    return yn
  }

/*
  Returns a List of Maps.
  Each map has a keySet of ID,CELL,DESIGN,CELL_TYPE,FROM_DATE,TO_DATE,START_STATION,END_STATION
  For current Cells TO_DATE is 12/31/${throughYear}
   to do create CELL_DESIGN in production
*/
  static List cellAttrs(Sql sql, String cellType, def throughYear) {
      def ra = []
      def cellsQuery = """
SELECT D.ID, D.CELL, D.DESIGN, D.CELL_TYPE, D.CONSTRUCTION_BEGAN_DATE FROM_DATE
  , NVL(D.CELL_END_DATE,(SELECT UNIQUE FROM_DATE_OVER-1 TO_DATE FROM MNR.CELL_ON_CELL CC WHERE CC.ID_UNDER=D.ID)) TO_DATE
  , START_STATION,END_STATION FROM
      (
      SELECT ID, CELL, DESIGN, CELL_TYPE
        , CONSTRUCTION_BEGAN_DATE,CONSTRUCTION_ENDED_DATE
        , CELL_END_DATE
        , START_STATION,END_STATION FROM
--        MNR.CELL_DESIGN
-- begin CELL_DESIGN
( SELECT
ID,
CELL,
DESIGN_NUMBER DESIGN,
CELL_TYPE ,
CONSTRUCTION_BEGAN_DATE ,
CONSTRUCTION_ENDED_DATE ,
NVL(DEMOLISHED_DATE,LEAD(CONSTRUCTION_BEGAN_DATE-1,1,NULL) OVER (PARTITION BY CELL ORDER BY CONSTRUCTION_BEGAN_DATE)) CELL_END_DATE ,
START_STATION ,
END_STATION
FROM
(
   SELECT
   ID ,
   CELL_NUMBER CELL ,
   SUBSTR(CLASS,24) CELL_TYPE ,
   (SELECT FIRST_LAYER_DATE FROM MNR.CELLS WHERE ID=C.ID) CONSTRUCTION_BEGAN_DATE ,
   CONSTRUCTION_ENDED_DATE ,
   DEMOLISHED_DATE ,
   CASE WHEN LEAD (ID,1,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 1 WHEN LEAD (ID,2,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 2 WHEN LEAD (ID,3,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 3 WHEN LEAD (ID,4,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 4 WHEN LEAD (ID,5,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 5 ELSE 1 END DESIGN_NUMBER ,
   C.START_STATION ,
   C.END_STATION
   FROM MNR.CELL C
   ORDER BY CELL,DESIGN_NUMBER
)
)
-- end CELL_DESIGN
      ) D
"""
      def q = cellsQuery
      def args = []
      if (cellType || throughYear) {
        q += " WHERE"
        if (cellType) {
          args << cellType
          q += " CELL_TYPE=? "
        }
        if (throughYear) {
          if (cellType) {
            q += " AND"
          }
          int yy = todaysYear()
          try {
          yy = Integer.parseInt(throughYear)
          } catch (NumberFormatException) {
            log.error  "Invalid 'throughYear' - using ${(new Date()).year} instead."
          }
          args << toSqlDate(11,31,yy)
          q += " CONSTRUCTION_ENDED_DATE < ?"
        }
      }
      q += " ORDER BY CELL, CONSTRUCTION_BEGAN_DATE"
    writeQuery(q,args,"c:\\MnROAD\\Data Release 2010\\cellsQuery.txt", false)
    def resultSet = sql.rows(q.toString(),args)
      // This block fills in any null END_DATE with Dec. 31 of year config.throughYear
      resultSet.each { row ->
        def m = [:]
        row.keySet().each { key ->
          def o = row.get(key)
          if(o) {
            if ( (o.class.name == "java.sql.TimeStamp") || (o.class.name == "java.sql.Date")){
              def val = new java.sql.Date(o.time)
              m.put(key, val)
            } else {
              m.put(key,o)
            }
          } else { // The value is null (normally only the TO_DATE should be null but check anyway.
            if (key == "TO_DATE"){
              def cal = new GregorianCalendar()
              cal.setTimeInMillis(MrUtils.endOfYearSqlDate(throughYear).getTime())
              java.sql.Timestamp rc = new java.sql.Timestamp(cal.getTime().getTime())
              m.put(key, rc)
            }
          }
       }
        ra << m
      }
    return ra
  }

  static def writeQuery(def query, def args, def fqfn, def append) {
    File iddf = new File(fqfn)
    FileWriter fw = new FileWriter(iddf, append)
    fw.append("${query}\n")
    fw.append("${args}\n")
    fw.flush()
    fw.close()
  }

  static String[] toArrayOfStrings(ArrayList al) {
    String [] sa = new String[al.size()];
    int i = 0;
    Iterator iter = al.iterator();
    while (iter.hasNext()) {
      String v = (String) iter.next()
        sa[i++] = v == null ? "" : v;
    }
    return sa
  }

  static int todaysYear() {
    Calendar.instance.get(Calendar.YEAR).intValue()
  }

  /*
  Use annual tables for the large volume sensor types (TC, VW, WM, XV).
  These are created in the first half of the year when all of the data for the
  previous year is certain to be in the current year table.
  If name is one of these
   */
  static String tableName(Sql sql, def name, def fr_year) {
    String rc = name
//    if (name.size > 2) {
//      if (grailsApplication.config.largeTables.contains(name.substring(0,2))) {
//        def tname = rc
        int currentYear = Calendar.getInstance().get(Calendar.YEAR)
        if (fr_year < currentYear-1) {
          rc = "${name}_${fr_year}"
        } else { /// Check to see if table "${model}_VALUES_${currentYear-1}" exists
          def result=false
          try {
            result = sql.firstRow("select count(*) from MNR.${name}_${currentYear-1}")
            rc = "${name}_${currentYear-1}"
          } catch (SQLException sqle) {
            log.info "Table MNR.${name}_${currentYear-1} not found."
          }
        }
//      }
//    }
    return rc
  }

}
// Groovy Code here

// Implicit variables include:
//     ctx: the Spring application context
//     grailsApplication: the Grails application
//     config: the Grails configuration
//     request: the HTTP request
//     session: the HTTP session
import groovy.sql.Sql
def dataSource = ctx.dataSource
def sql = Sql.newInstance(dataSource)

static String DATA_QUERY = "dataQuery"
static String COUNT_QUERY = "countQuery"

  List valueColumns(def tableName) {
    def rc = []
    def t = ctx.grailsApplication.config.valueColumnNames.get(tableName)
    if (t) {
      rc = t.split(",")
      }
      else
      rc << "VALUE"
    return rc
  }

  Map queryMap(def sql, int cell, String tableName, ArrayList valueColumnNames, java.sql.Date from_date, java.sql.Date to_date) {
    def rc = [:]
    def tnbase = "TC_VALUES"
    def tn = tableName
    if (ctx.grailsApplication.config.useLargetables || tableName.startsWith(tnbase))
      tn = tnbase + "_ALL"
    def psq = "select unique seq from MNR.SENSOR_COUNTS where cell=? and table_name=? and ((from_day between ? and ?) or (to_day between ? and ?)) order by seq"
    def q = ""
    def ans = sql.rows(psq, [cell, tn, from_date,to_date, from_date,to_date])
    if (ans) {
      def q1 = 'select cell \"Cell\", to_char(day,\'yyyy-mm-dd\') \"Day\", hour \"Hour\", qhr \"Qhr\", minute \"Minute\",'
      def q2 = ' from (select cell,day,hour,qhr,minute,'
      def q3 = " FROM mnr.$tableName WHERE cell = ? AND DAY BETWEEN ? and ? GROUP BY cell,day,hour,qhr,minute) order by cell,day,hour,qhr,minute".toString()
      def a1 = []
      def a2 = []
      ans.each {row ->
        if (valueColumnNames.size()==1) {
          a1 << "\"s_${row.SEQ}\""
          a2 << "min(decode(seq,${row.SEQ},value)) as \"s_${row.SEQ}\""
        }
        else {
          valueColumnNames.each { cn ->
            a1 << "${cn}_${row.SEQ}"
            a2 << "min(decode(seq,${row.SEQ},${cn})) as ${cn}_${row.SEQ}"
          }
        }
      }
      q = "${q1}${a1.join(',')}${q2}${a2.join(',')}${q3}"
    }
    if (q) {
      rc.put (DATA_QUERY,q.toString())
      rc.put (COUNT_QUERY,q[0..6] + "count(*) NUMROWS " + q.substring(q.indexOf("from")))
    }
    return rc
  }
  

def arglist = []
//substr(table_name,1,2) model
def q = """
select cell
,table_name
,seq
, to_char(from_day,'yyyy-mm-dd') fr_date
, to_char(to_day,'yyyy-mm-dd') to_date
from MNR.SENSOR_COUNTS
order by table_name,cell,seq
"""
def ans = sql.rows(q, arglist)
ans.eachWithIndex { row,i ->
  if (i < 100) {
     def qm = queryMap(sql, row.CELL.intValue(), row.table_name, valueColumns(row.table_name), java.sql.Date.valueOf(row.fr_date), java.sql.Date.valueOf(row.to_date))
     println qm[DATA_QUERY]
     println qm[COUNT_QUERY]
     }
}

select cell "Cell", to_char(day,'yyyy-mm-dd') "Day", hour "Hour", qhr "Qhr", minute "Minute","s_1","s_2","s_3","s_4","s_5","s_6","s_7","s_8" from (select cell,day,hour,qhr,minute,min(decode(seq,1,value)) as "s_1",min(decode(seq,2,value)) as "s_2",min(decode(seq,3,value)) as "s_3",min(decode(seq,4,value)) as "s_4",min(decode(seq,5,value)) as "s_5",min(decode(seq,6,value)) as "s_6",min(decode(seq,7,value)) as "s_7",min(decode(seq,8,value)) as "s_8" FROM mnr.CR_VALUES WHERE cell = ? AND DAY BETWEEN ? and ? GROUP BY cell,day,hour,qhr,minute) order by cell,day,hour,qhr,minute

select count(*) NUMROWS from (select cell,day,hour,qhr,minute,min(decode(seq,1,value)) as "s_1",min(decode(seq,2,value)) as "s_2",min(decode(seq,3,value)) as "s_3",min(decode(seq,4,value)) as "s_4",min(decode(seq,5,value)) as "s_5",min(decode(seq,6,value)) as "s_6",min(decode(seq,7,value)) as "s_7",min(decode(seq,8,value)) as "s_8" FROM mnr.CR_VALUES WHERE cell = ? AND DAY BETWEEN ? and ? GROUP BY cell,day,hour,qhr,minute) order by cell,day,hour,qhr,minute


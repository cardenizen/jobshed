package us.mn.state.dot.mnroad
import groovy.sql.Sql
import org.quartz.JobExecutionContext
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.apache.log4j.Logger

class RefreshReadingCountsJob extends JobBase {
  def timeout = 5000l // execute job once in 5 seconds
  static String defaultCron = '0/30 * * * * ?'
  static String trigger = 'refreshReadingCountsJobCronTrigger'
  static String group = 'mnroad'
  static int initialized = 0

  def dataSource
  def grailsApplication

  static triggers = {
    cron name:trigger, startDelay:100, cronExpression: defaultCron, group:group
  }

  String jobName() {
    return this.class.name
  }

  def execute(JobExecutionContext context) {
    def sd = new Date()
    if (context
      && context.scheduler
      && !initialized
      && ConfigurationHolder.config.pauseJobsAtStartup
      ) {
      initialized = 1
      context.scheduler.pauseTrigger(trigger, group)
      log.info "Trigger [${trigger},${group}] paused. Job will not run until trigged!\n"
      return
    }
    if (!context.mergedJobDataMap || context.mergedJobDataMap.size() < 2) {
      log.info "Too few parameters: ${context.mergedJobDataMap}"
      return
    }
    def res = "Initialized"
    def sds = new java.sql.Date(sd.getTime())
    def jr = new JobResult()
    jr.jobName = jobName()
    jr.startTime = sds
    jr.result = res
    jr.duration = "Unknown"
    jr.parameterMap = "${context.mergedJobDataMap}"
    if (!jr.hasErrors() && jr.save(flush:true)) {
      println "Initialized JobResult (id:${jr.id}) was saved."
    }
    try {
      def jd = context.getJobDetail()
      if (jd) {
        jd.addJobListener("MyJobListener")
      }
      res = run(context.mergedJobDataMap)
    }
    catch (Exception e) {
      println "RefreshReadingCountsJob.execute exception: ${e.message}"
      res = "Abend: ${e.message}"
      if (jr.id) {
        jr.result = res
        jr.save(flush:true)
      } else {
        println "Unable to store result for job RefreshReadingCountsJob"
      }
    }
    def ed = new Date()
    if (!jr.id) {
      res = "No Job Result ID"
    }
    else if (!res.startsWith("Abend")) {
      jr.result = res
      def sjr = JobResult.get(jr.id)
      sjr.duration = getDuration(sd,ed)
      def svjr = jr.save(flush:true)
      if (svjr)
        res = "Job Result ${svjr.id} saved. ${svjr.result}."
      else
        res = "Unable to save result for Job: ${jr}"
    }
    context.setResult(res)
  }

  def run(Map runParams) {
      // execute task
      def rr = []
      boolean tf = false
      def table
      if (dataSource) {
        Sql sql = new Sql(dataSource)
        def tnfm = tableNamesFromModels(grailsApplication.config)
        tnfm.each { tableName ->
          table = tableName.split(":")[0]
          def qdel = "DELETE FROM MNR.SENSOR_COUNTS WHERE TABLE_NAME = ?"
          tf = sql.execute(qdel.toString(),[table])
          def q = "insert into MNR.sensor_counts select distinct '${table}' table_name,cell,seq,min(day) from_day,max(day) to_day,count(*) num_readings,sysdate as_of from MNR.${table} group by cell,seq,to_number(to_char(day,'yyyy')) order by TABLE_NAME,CELL,SEQ,min(day)"
          tf = sql.execute(q.toString())
          if (!tf) {
            println "${table}->${sql.updateCount}"
            rr << "${table}->${sql.updateCount}"
          }
        }
      }
    return rr.toString()
  }
  /*
   *
   * This method returns a list on table names in which sensor reading values are stored.  Most tables contain
   * only a single value column named 'VALUE'.  Those that do not follow this convention are identified as a member
   * of the "valueColumnNames" map in Config.groovy.  The map key is table name and the value is a comma separated
   * list of the names of columns which hold sensor reading values.
   *
   */
  def tableNamesFromModels(def config) {
    def cabinetTempModel='CT'
    def skipModels = ["CT"]
    def rc = []
    def q = "SELECT substr(table_name,1,2) model_from_table_names FROM all_tables where owner = 'MNR' and table_name like '%_VALUES' and substr(table_name,3,2)='_V' order by table_name"

    Sql sql = new Sql(dataSource)
    def ans = sql.rows(q.toString())
    ans.each { row ->
      if (!skipModels.contains(row.MODEL_FROM_TABLE_NAMES)) // != cabinetTempModel)
        rc << row.MODEL_FROM_TABLE_NAMES
    }

    // Adjust names for the large table model types to use Views
    def tableNames = rc.collect {
      if (config.largeTables.contains(it)) {
          it+'_VALUES_ALL'  // or use te View
      }
      else {
        it+'_VALUES'
      }
    }
    rc = tableNames.collect {
      if (config.valueColumnNames.keySet().contains(it)) {
        it+":${config.valueColumnNames.get(it)}"
      }
      else
        it
    }
    return rc
  }
}

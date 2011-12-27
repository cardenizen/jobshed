package us.mn.state.dot.mnroad

import org.quartz.JobExecutionContext
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.apache.log4j.Logger

class ExportSensorsJob extends JobBase {
    static String trigger = 'exportSensorsJobCronTrigger'
    static String group = 'mnroad'
    static Logger log = Logger.getLogger(ExportSensorsJob.class)

    static triggers = {
      cron name:trigger, startDelay:100, cronExpression: defaultCron, group:group
    }

    String jobName() {
      return this.class.name
    }

    static int initialized = 0
    def exportDataService

    def execute(JobExecutionContext context) {
      def sd = new Date()
      if (context
        && context.scheduler
        && !initialized
        && ConfigurationHolder.config.pauseJobsAtStartup
        ) {
        initialized = 1
        context.scheduler.pauseTrigger(trigger, group)
        log.info "Trigger [${trigger},${group}] paused. Job will not run until triggered!\n"
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
        res = exportDataService.dpSensors(context.mergedJobDataMap)
      }
      catch (Exception e) {
        println "ExportSensorJob.execute exception: ${e.message}"
        res = "Abend: ${e.message}"
        if (jr.id) {
          jr.result = res
          jr.save(flush:true)
        } else {
          println "Unable to store result for job ExportSensor"
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
          res = "Unable to save Job Result: ${jr}"
      }
      context.setResult(res)
    }
}

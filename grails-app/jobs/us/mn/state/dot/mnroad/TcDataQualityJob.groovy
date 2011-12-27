package us.mn.state.dot.mnroad

import org.quartz.JobExecutionContext
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * Created by IntelliJ IDEA.
 * User: Carr1Den
 * Date: Jun 2, 2011
 * Time: 12:31:47 PM
 * To change this template use File | Settings | File Templates.
 */
class TcDataQualityJob extends JobBase {
  def timeout = 5000l // execute job once in 5 seconds
  static String defaultCron = '0/30 * * * * ?'
  static String trigger = 'tcDataQualityJobCronTrigger'
  static String group = 'mnroad'
  static int initialized = 0

  def dataSource
  def grailsApplication
  def tcDataQualityService

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
    }  else {
      log.error "${jr.errors}"  
    }
    try {
      def jd = context.getJobDetail()
      if (jd) {
        jd.addJobListener("MyJobListener")
      }
      res = run(context.mergedJobDataMap)
    }
    catch (Exception e) {
      println "TcDataQualityJob.execute exception: ${e.message}"
      res = "Abend: ${e.message}"
      if (jr.id) {
        jr.result = res
        jr.save(flush:true)
      } else {
        println "Unable to store result for job TcDataQualityJob"
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
    def rr = tcDataQualityService.tcQueryAll(runParams)
    return rr.toString()
  }
}

package us.mn.state.dot.mnroad

import org.quartz.CronTrigger
//import us.mn.state.dot.mnroad.data.ExportLayersJobParams
//import us.mn.state.dot.mnroad.data.ExportFwdJobParams
//import com.google.gson.Gson
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONException

class JobSetupController extends ControllerBase {

  static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

  def index = {
    //println "index: ${params}, redirecting to list."
    redirect(action: "list", params: params)
  }

  def list = {
    params.max = Math.min(params.max ? params.int('max') : 10, 100)
    def list = JobSetup.list(params)
    def schedulerStatusMap = [:]
    def mostRecentResultMap = [:]
    def jobsRunningMap = [:]

    list.each { job ->
      CronTrigger trigger = getTrigger(job.toString())
      schedulerStatusMap.put(job.id, triggerState(trigger))
      jobsRunningMap.put(job.id, jobIsRunning(job.jobName)?"Running":"Idle")
      def mrj = JobResult.findByJobName(job.toString(), [max:1,sort:"startTime",order:"desc"])
      mostRecentResultMap.put(job.id, mrj?"${mrj.startTime},&nbsp;&nbsp;${mrj.duration},&nbsp;&nbsp;${mrj.result}":"None")
    }

    [jobSetupInstanceList: list
            , jobSetupInstanceTotal: JobSetup.count()
            , jobsRunningMap : jobsRunningMap
            , schedulerStatusMap : schedulerStatusMap
            , jrm : mostRecentResultMap
    ]
  }

  def create = {
    //println "create: ${params}."
    def jobSetupInstance = new JobSetup()
    jobSetupInstance.properties = params
    jobSetupInstance.parameterMap = "{}"
    return [jobSetupInstance: jobSetupInstance]
  }

  def save = {
    def jobSetupInstance = new JobSetup(params)
    jobSetupInstance.createdBy = getUserName()
    jobSetupInstance.lastUpdatedBy = jobSetupInstance.createdBy
    if (!jobSetupInstance.hasErrors() && jobSetupInstance.save(flush: true)) {
      flash.message = "${message(code: 'default.created.message', args: [message(code: 'jobSetup.label', default: 'JobSetup'), jobSetupInstance.id])}"
      redirect(action: "show", id: jobSetupInstance.id)
    } else {
      render(view: "create", model: [jobSetupInstance: jobSetupInstance])
    }
  }

  def show = {
    def jobSetupInstance = JobSetup.get(params.id)
    if (!jobSetupInstance) {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'jobSetup.label', default: 'JobSetup'), params.id])}"
      redirect(action: "list")
    } else {
      def json = ""
      def jobj
      try {
        jobj = new JSONObject(jobSetupInstance.parameterMap)
        jobj.keySet().each {
          json += "\n${it} -> ${jobj.get(it)}\n"
        }
      } catch (JSONException je) {
        flash.message = je.message
      }
      [jobSetupInstance: jobSetupInstance
              , pm:json
              , schedule:MrUtils.cronSchedule(jobSetupInstance.cronExpr)]
    }
  }

  def edit = {
    def jobSetupInstance = JobSetup.get(params.id)
    if (!jobSetupInstance) {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'jobSetup.label', default: 'JobSetup'), params.id])}"
      redirect(action: "list")
    } else {
      return [jobSetupInstance: jobSetupInstance]
    }
  }

  def update = {
    def jobSetupInstance = JobSetup.get(params.id)
    if (jobSetupInstance) {
      if (params.version) {
        def version = params.version.toLong()
        if (jobSetupInstance.version > version) {
          jobSetupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'jobSetup.label', default: 'JobSetup')] as Object[], "Another user has updated this JobSetup while you were editing")
          render(view: "edit", model: [jobSetupInstance: jobSetupInstance])
          return
        }
      }
      jobSetupInstance.properties = params
      jobSetupInstance.parameterMap = jobSetupInstance.prettyToJson(params.prettyParameterMap)
      if (!jobSetupInstance.hasErrors() && jobSetupInstance.save(flush: true)) {
        def cls
        try {
          cls = grailsApplication.classLoader.loadClass(jobSetupInstance.jobName)
        } catch (ClassNotFoundException cnfe) {
          flash.message = "ClassNotFoundException - The Job Name must be an existing class."
          render(view: "edit", model: [jobSetupInstance: jobSetupInstance])
        }
        def jobcls = getJob(jobSetupInstance.jobName)
        if (jobSetupInstance.cronExpr && jobcls && cls) {
          jobcls.triggers[cls.trigger].expandoProperties.triggerAttributes.remove("cronExpression")
          jobcls.find{jobSetupInstance.jobName}.triggers[cls.trigger].expandoProperties.triggerAttributes.put("cronExpression",jobSetupInstance.cronExpr)
        }
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'jobSetup.label', default: 'JobSetup'), jobSetupInstance.id])}"
        redirect(action: "show", id: jobSetupInstance.id)
      }
      else {
        render(view: "edit", model: [jobSetupInstance: jobSetupInstance])
      }
    } else {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'jobSetup.label', default: 'JobSetup'), params.id])}"
      redirect(action: "list")
    }
  }

  def getJob = { name ->
    def rc
    for (job in grailsApplication.taskClasses) {
      if (name == job.fullName) {
        rc = job
        break
      }
    }
    return rc
  }

  def delete = {
    def jobSetupInstance = JobSetup.get(params.id)
    if (jobSetupInstance) {
      try {
        jobSetupInstance.cronExpr = 'dummy'
        jobSetupInstance.parameterMap='dummy'
        jobSetupInstance.save(flush:true)
        jobSetupInstance.delete(flush: true)
        flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'jobSetup.label', default: 'JobSetup'), params.id])}"
        redirect(action: "list")
      } catch (org.springframework.dao.DataIntegrityViolationException e) {
        flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'jobSetup.label', default: 'JobSetup'), params.id])}"
        redirect(action: "show", id: params.id)
      }
    } else {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'jobSetup.label', default: 'JobSetup'), params.id])}"
      redirect(action: "list")
    }
  }

  def editCronExpr = {
    //println "editCronExpr: $params"
    [theExpression:params.theExpression,jobid:params.jobid]
  }

  def updateCronExpr = {
    //println "updateCronExpr: ${params}"
    if (params.jobid) {   // editing an existing job
      def ji = JobSetup.get(getId(params.jobid))
      ji.cronExpr = params.theExpression
      if (!ji.hasErrors() && ji.save(flush: true)) {
          flash.message = "${message(code: 'default.updated.message', args: [message(code: 'jobSetup.label', default: 'JobSetup'), ji])}"
          redirect(action: "show", id: getId(params.jobid))
      } else {
        flash.message = "Cron expression, '${params.theExpression}', could not be saved. "
        redirect(action: "list")
      }
    } else {
      // editing an existing job
      redirect(action: "create", params:[cronExpr: params.theExpression, schedule:MrUtils.cronSchedule(params.theExpression)])
    }

  }

}

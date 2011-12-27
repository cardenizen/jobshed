package us.mn.state.dot.mnroad

import java.text.ParseException
import org.quartz.CronTrigger
import org.quartz.JobDataMap
import groovy.sql.Sql

class ConfigController extends ControllerBase {

  def index = {
    quartzScheduler.addJobListener(new MyJobListener())
    def jsi = JobSetup.get(getId(params.jobid))
    def jn = params.jobName?:(jsi?jsi.jobName:"")
    def mostRecentJobResult
    if (jn) {
      mostRecentJobResult = JobResult.findByJobName(jn, [max:1,sort:"startTime",order:"desc"])
    }
    CronTrigger trigger = getTrigger(jn.toString())
    def status = " "
    if (trigger && trigger.nextFireTime)
      status = "Trigger ${trigger.getName()} in group ${trigger.getGroup()} scheduled to fire at ${trigger.nextFireTime}."
    if (trigger && params.size() == 2) {
      if (triggerState(trigger) == 'Paused') {
        quartzScheduler.resumeTrigger(trigger.getName(), trigger.getGroup())
      }
      status = "${trigger.getName()}.${trigger.getGroup()} is ${triggerState(trigger)}"
    }
    flash.message=status + " Trigger Status: ${triggerState(trigger)}"
    def shortName=(jn && jn.lastIndexOf(".") > 1)?jn[jn.lastIndexOf(".")+1..-1]:""
    def js = JobSetup.get(params.jobid)
    def runningJobs = quartzScheduler.getCurrentlyExecutingJobs()
    [
        pauseResume: (triggerState(trigger) == 'Paused')?"Resume":"Pause"
        , runningJobs: runningJobs
        , jobName:jn
        , jobTitle:shortName
        , cronExpr:params.theExpression?:(js?js.cronExpr:"")
        , jsi:jsi
        , jobid:params.jobid
        , mrj:mostRecentJobResult
    ]
  }

    def editCronExpr = {
      println "ConfigController:editCronExpr: $params"
      [theExpression:params.theExpression
        , jobid:params.jobid
      ]
    }

    def pause = {
      def status = "Trigger Status: Unknown."
      def trigger
      def mostRecentJobResult
      if (params.jobid) {
        def jsi = JobSetup.get(getId(params.jobid))
        if (jsi) {
          trigger = getTrigger(jsi.jobName)
          if (jsi.jobName) {
            mostRecentJobResult = JobResult.findByJobName(jsi.jobName, [max:1,sort:"startTime",order:"desc"])
          }
        }
      }
      if (trigger) {
        quartzScheduler.pauseTrigger(trigger.getName(), trigger.getGroup())
        status = "${trigger.getName()}.${trigger.getGroup()} is ${triggerState(trigger)}"
      }
      flash.message=status
      render (view:'index'
        , model:[
          pauseResume: (triggerState(trigger) == 'Paused')?"Resume":"Pause"
        , runningJobs: quartzScheduler.getCurrentlyExecutingJobs()
        , cronExpr: params.cronExpr
        , jobid:params.jobid
        , mrj:mostRecentJobResult
        ])
    }

    def interrupt = {
      def status = "Interrupt Status: Unknown."
      if (params.jobDetailName && params.jobDetailGroup) {
        quartzScheduler.interrupt(params.jobDetailName, params.jobDetailGroup)
        status = "Job ${params.jobDetailName}, ${params.jobDetailGroup} sent interrupt signal."
      }
      flash.message=status
      render (view:'index'
        , model:[
          pauseResume: (triggerState(trigger) == 'Paused')?"Resume":"Pause"
        , runningJobs: quartzScheduler.getCurrentlyExecutingJobs()
        , cronExpr: params.cronExpr
        , jobid:params.jobid
        ])
    }

  def resume = {
    def status = "Trigger Status: Unknown."
    def trigger
    def mostRecentJobResult
    def jsi
    if (params.jobid) {
      jsi = JobSetup.get(getId(params.jobid))
      if (jsi) {
        trigger = getTrigger(jsi.jobName)
        if (jsi.jobName) {
          mostRecentJobResult = JobResult.findByJobName(jsi.jobName, [max:1,sort:"startTime",order:"desc"])
        }
      }
    }
    if (trigger) {
      if (params.cronExpr) {
        try {
          trigger.setCronExpression params.cronExpr
          if (jsi) {
            trigger.setJobDataMap(new JobDataMap(jsi.toMap()))
          }
          quartzScheduler.rescheduleJob (trigger.getName(), trigger.getGroup(), trigger)
          status = "Trigger ${trigger.getName()} in group ${trigger.getGroup()} scheduled to fire at ${trigger.nextFireTime}."
        } catch (ParseException pe) {
          status = "Unable to parse new cron expression: ${params.newCronExpression}.  Job not rescheduled."
          log.info status
        }
      } else {
        quartzScheduler.resumeTrigger(trigger.getName(), trigger.getGroup())
        status = " Trigger Status: ${triggerState(trigger)}"
      }
     
    }
    flash.message=status
    render (view:'index'
      , model:[
        pauseResume: (triggerState(trigger) == 'Paused')?"Resume":"Pause"
      , runningJobs: quartzScheduler.getCurrentlyExecutingJobs()
      , cronExpr: params.cronExpr
      , jobid:params.jobid
      , mrj:mostRecentJobResult
      ])
  }

  def reschedule = {
    def status = "Trigger Status: Unknown."
    def trigger
    def mostRecentJobResult
    def jsi
    if (params.jobid) {
      jsi = JobSetup.get(getId(params.jobid))
      if (jsi) {
        trigger = getTrigger(jsi.jobName)
        if (jsi.jobName) {
          mostRecentJobResult = JobResult.findByJobName(jsi.jobName, [max:1,sort:"startTime",order:"desc"])
        }
      }
    }
    if (trigger) {
      if (params.cronExpr) {
        try {
          trigger.setCronExpression params.cronExpr
          if (jsi) {
            trigger.setJobDataMap(new JobDataMap(jsi.toMap()))
          }
          quartzScheduler.rescheduleJob (trigger.getName(), trigger.getGroup(), trigger)
          status = "Trigger ${trigger.getName()} in group ${trigger.getGroup()} scheduled to fire at ${trigger.nextFireTime}."
        } catch (ParseException pe) {
          status = "Unable to parse new cron expression: ${params.newCronExpression}.  Job not rescheduled."
          log.info status
        }
      } else {
        status = "A new cron expression is required."
      }
    }
    flash.message=status
    render (view:'index'
      ,model:[
        pauseResume: (triggerState(trigger) == 'Paused')?"Resume":"Pause"
      , runningJobs: quartzScheduler.getCurrentlyExecutingJobs()
      , cronExpr: params.cronExpr
      , jobid:params.jobid
      , mrj:mostRecentJobResult
      ])
  }

  def runNow = {
    CronTrigger trigger
    def mostRecentJobResult
    if (params.jobid) {
      def jsi = JobSetup.get(getId(params.jobid))
      if (jsi) {
        if (jsi.jobName) {
          mostRecentJobResult = JobResult.findByJobName(jsi.jobName, [max:1,sort:"startTime",order:"desc"])
        }
        trigger = getTrigger(jsi.jobName)
        def cls
        try {
          cls = grailsApplication.classLoader.loadClass(jsi.jobName)
        } catch (ClassNotFoundException cnfe) {
          log.error("The class for job name ${jsi.jobName} was not found.")
        }
        if (!cls) {
          try {
            log.info "Trying this.classloader ..."
            cls = this.classLoader.loadClass(jsi.jobName)
          } catch (ClassNotFoundException cnfe) {
            log.error("The class for job name ${jsi.jobName} was not found.")
          }
        }
        if (cls) {
          def cejl = quartzScheduler.getCurrentlyExecutingJobs()
          def isExecuting = false
          for (job in cejl) {
            if (job.jobDetail.name == cls.name) {
              isExecuting = true
              break
            }
          }
          if (isExecuting){
            flash.message = "Job '${cls.name}' is alreading running."
          } else {
            def jdm = new JobDataMap(jsi.toMap())
            quartzScheduler.triggerJob(cls.name, cls.group, jdm)
            flash.message="${cls.name} was triggered."
          }
        }
      } else {
        flash.message = "JobSetup for id ${params.jobid} not found in DB."
      }
    } else {
      flash.message="Job ID, jobid, not in params map."
    }
//    render (view:'index'
//      ,model:[
//        runningJobs: quartzScheduler.getCurrentlyExecutingJobs()
//       , pauseResume: (triggerState(trigger)=="Paused")?"Resume":"Pause"
//       , cronExpr: params.cronExpr
//       , jobid:params.jobid
//       , mrj:mostRecentJobResult
//      ])
    redirect(action: "list", controller: "jobSetup")
  }

  def getTriggers =  {
    def triggers = []
    quartzScheduler.getJobGroupNames().each { groupName ->
      quartzScheduler.getJobNames(groupName).each { jobName ->
        triggers << "${groupName}.${jobName}"
      }
    }
  }

  def testNow = {
    if (params.jobid) {
      def jsi = JobSetup.get(getId(params.jobid))
      flash.message = test(jsi.toMap())
    }
    redirect(controller:"jobSetup", action: "show", id: getId(params.jobid))
  }

  def exportDataService
    def test(def config) {
//      import groovy.sql.Sql
//      import us.mn.state.dot.mnroad.JobSetup
//      def dataSource = ctx.dataSource
      def rc = "\"test\" does nothing for now."
//      def sql = Sql.newInstance(dataSource)

      return rc
    }
/*
select "cell" "Cell", 'TC' "Model", "station" "Station","offset" "Offset","seq" "Seq",DEPTH_GROUP "Location_group",DEPTH "Depth_ft"
,"material" "Material","thickness"/12 "Thickness","tree" "Tree", TYPE "Type"
FROM (
SELECT
X.CELL "cell"
,CASE WHEN X.TYPE='HmaCell' THEN 1 WHEN X.TYPE='PccCell' THEN 2 WHEN X.TYPE='AggCell' THEN 3  WHEN X.TYPE='CompositeCell' THEN 4 END
TYPE
,CASE WHEN X.LOCATION='Mainline' THEN 1 WHEN X.LOCATION='Low Volume Road' THEN 2 WHEN X.LOCATION='Farm Road' THEN 3 WHEN X.LOCATION='MnRoad Parking Lot' THEN 4 WHEN X.LOCATION='MnRoad Sidewalk' THEN 5 WHEN X.LOCATION='Public Road' THEN 6  END
LOCATION
,X.SENSOR_IDX
,X.STATION "station"
,X.OFFSET "offset"
,X.DEPTH
,X.SEQ "seq"
,X.MAT "material"
,X.THICKNESS "thickness"
,X.DEPTH_GROUP
,X.TREE "tree"
 FROM (SELECT
T.CELL
,T.TYPE
,T.LOCATION
,ROW_NUMBER() OVER (PARTITION BY T.CELL ORDER BY T.STATION,T.OFFSET,T.DEPTH) SENSOR_IDX
,T.STATION
,T.OFFSET
,T.DEPTH
,T.SEQ
,T.MAT
,T.THICKNESS
,T.DEPTH_GROUP
,D.TREE TREE
FROM (
SELECT
C.CELL_NUMBER CELL, S.SEQ, SUBSTR(C.CLASS,24) TYPE, F.NAME LOCATION
, S.STATION_FT STATION, S.OFFSET_FT OFFSET, S.SENSOR_DEPTH_IN/12 DEPTH
, DEPTH_GROUP, DESCR DEPTH_GROUP_DESCR, M.DESCRIPTION MAT, LY.THICKNESS/25.4 THICKNESS
FROM MNR.CELL C
JOIN MNR.LANE LN ON LN.CELL_ID=C.ID
JOIN MNR.LAYER LY ON LY.LANE_ID=LN.ID
JOIN MNR.SENSOR S ON S.LAYER_ID=LY.ID
JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID=SM.ID
JOIN MNR.MATERIAL M ON LY.MATERIAL_ID=M.ID
JOIN MNR.ROAD_SECTION R ON C.ROAD_SECTION_ID=R.ID
JOIN MNR.FACILITY F ON R.FACILITY_ID=F.ID
INNER JOIN
--MNR.TC_DEPTH_RANGES R
(
SELECT
--DISTINCT DEPTH_GROUP, DESCR, R.RNG_START, R.RNG_STOP, COUNT(*) OVER (PARTITION BY DEPTH_GROUP,DESCR) N
DEPTH_GROUP,DESCR,RNG_START,RNG_STOP FROM (
        SELECT 1 DEPTH_GROUP,'0.000 - 0.110' DESCR, 0.0 RNG_START, 0.110 RNG_STOP FROM DUAL
UNION (SELECT  2 DEPTH_GROUP,'0.111 - 0.190' DESCR, 0.111, 0.190 FROM DUAL)
UNION (SELECT  3 DEPTH_GROUP,'0.191 - 0.350' DESCR, 0.191, 0.350 FROM DUAL)
UNION (SELECT  4 DEPTH_GROUP,'0.351 - 0.450' DESCR, 0.351, 0.450 FROM DUAL)
UNION (SELECT  5 DEPTH_GROUP,'0.451 - 0.550' DESCR, 0.451, 0.550 FROM DUAL)
UNION (SELECT  6 DEPTH_GROUP,'0.551 - 0.850' DESCR, 0.551, 0.850 FROM DUAL)
UNION (SELECT  7 DEPTH_GROUP,'0.851 - 1.250' DESCR, 0.851, 1.250 FROM DUAL)
UNION (SELECT  8 DEPTH_GROUP,'1.251 - 1.650' DESCR, 1.251, 1.650 FROM DUAL)
UNION (SELECT  9 DEPTH_GROUP,'1.651 - 2.480' DESCR, 1.651, 2.480 FROM DUAL)
UNION (SELECT 10 DEPTH_GROUP,'2.481 - 3.590' DESCR, 2.481, 3.590 FROM DUAL)
UNION (SELECT 11 DEPTH_GROUP,'3.591 - 4.590' DESCR, 3.591, 4.590 FROM DUAL)
UNION (SELECT 12 DEPTH_GROUP,'4.591 - 5.700' DESCR, 4.591, 5.700 FROM DUAL)
UNION (SELECT 13 DEPTH_GROUP,'5.701+'        DESCR, 5.701, NULL  FROM DUAL)
)
) R
ON S.SENSOR_DEPTH_IN/12 BETWEEN NVL(R.RNG_START, S.SENSOR_DEPTH_IN/12) AND NVL(R.RNG_STOP, S.SENSOR_DEPTH_IN/12)
WHERE SM.MODEL='TC'
ORDER BY CELL, SEQ
) T
JOIN
(
SELECT CELL, STATION, OFFSET, SEQ_MIN, SEQ_MAX, NUM_SENSORS, TREE
FROM
--MNR.TC_TREE
(SELECT B.CELL_NUMBER CELL,B.STATION_FT STATION,B.OFFSET_FT OFFSET,B.SEQ_MIN,B.SEQ_MAX,B.NUM_SENSORS
, RANK() OVER (PARTITION BY CELL_NUMBER ORDER BY STATION_FT) TREE
FROM (
SELECT C.CELL_NUMBER
,S.STATION_FT, S.OFFSET_FT, MIN(SEQ) SEQ_MIN, MAX(SEQ) SEQ_MAX, COUNT(*) NUM_SENSORS
FROM MNR.CELL C
JOIN MNR.LANE LN ON LN.CELL_ID=C.ID
JOIN MNR.LAYER LY ON LY.LANE_ID=LN.ID
JOIN MNR.SENSOR S ON S.LAYER_ID=LY.ID
JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID=SM.ID
WHERE SM.MODEL='TC' AND S.DATE_INSTALLED BETWEEN ? AND ?
GROUP BY C.CELL_NUMBER,S.STATION_FT, S.OFFSET_FT
ORDER BY C.CELL_NUMBER,S.STATION_FT, S.OFFSET_FT
) B)
) D
ON D.CELL=T.CELL AND D.STATION=T.STATION AND D.OFFSET=T.OFFSET
) X
WHERE X.DEPTH_GROUP IS NOT NULL
)
WHERE "cell"=?
ORDER BY "cell","seq"

SELECT
X.CELL
,CASE WHEN X.TYPE='HmaCell' THEN 1 WHEN X.TYPE='PccCell' THEN 2 WHEN X.TYPE='AggCell' THEN 3  WHEN X.TYPE='CompositeCell' THEN 4 END
TYPE
,CASE WHEN X.LOCATION='Mainline' THEN 1 WHEN X.LOCATION='Low Volume Road' THEN 2 WHEN X.LOCATION='Farm Road' THEN 3 WHEN X.LOCATION='MnRoad Parking Lot' THEN 4 WHEN X.LOCATION='MnRoad Sidewalk' THEN 5 WHEN X.LOCATION='Public Road' THEN 6  END
LOCATION
,X.SENSOR_IDX
,X.STATION
,X.OFFSET
,X.DEPTH
,X.SEQ
,X.MAT
,X.THICKNESS
,X.DEPTH_GROUP
,X.TREE
 FROM (SELECT
T.CELL
,T.TYPE
,T.LOCATION
,ROW_NUMBER() OVER (PARTITION BY T.CELL ORDER BY T.STATION,T.OFFSET,T.DEPTH) SENSOR_IDX
,T.STATION
,T.OFFSET
,T.DEPTH
,T.SEQ
,T.MAT
,T.THICKNESS
,T.DEPTH_GROUP
,D.TREE TREE
FROM (
SELECT
C.CELL_NUMBER CELL, S.SEQ, SUBSTR(C.CLASS,24) TYPE, F.NAME LOCATION
, S.STATION_FT STATION, S.OFFSET_FT OFFSET, S.SENSOR_DEPTH_IN/12 DEPTH
, DEPTH_GROUP, DESCR DEPTH_GROUP_DESCR, M.DESCRIPTION MAT, LY.THICKNESS/25.4 THICKNESS
FROM MNR.CELL C
JOIN MNR.LANE LN ON LN.CELL_ID=C.ID
JOIN MNR.LAYER LY ON LY.LANE_ID=LN.ID
JOIN MNR.SENSOR S ON S.LAYER_ID=LY.ID
JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID=SM.ID
JOIN MNR.MATERIAL M ON LY.MATERIAL_ID=M.ID
JOIN MNR.ROAD_SECTION R ON C.ROAD_SECTION_ID=R.ID
JOIN MNR.FACILITY F ON R.FACILITY_ID=F.ID
INNER JOIN
--MNR.TC_DEPTH_RANGES R
(
SELECT
DEPTH_GROUP,DESCR,RNG_START,RNG_STOP FROM (
        SELECT 1 DEPTH_GROUP,'0.000 - 0.110' DESCR, 0.0 RNG_START, 0.110 RNG_STOP FROM DUAL
UNION (SELECT  2 DEPTH_GROUP,'0.111 - 0.190' DESCR, 0.111, 0.190 FROM DUAL)
UNION (SELECT  3 DEPTH_GROUP,'0.191 - 0.350' DESCR, 0.191, 0.350 FROM DUAL)
UNION (SELECT  4 DEPTH_GROUP,'0.351 - 0.450' DESCR, 0.351, 0.450 FROM DUAL)
UNION (SELECT  5 DEPTH_GROUP,'0.451 - 0.550' DESCR, 0.451, 0.550 FROM DUAL)
UNION (SELECT  6 DEPTH_GROUP,'0.551 - 0.850' DESCR, 0.551, 0.850 FROM DUAL)
UNION (SELECT  7 DEPTH_GROUP,'0.851 - 1.250' DESCR, 0.851, 1.250 FROM DUAL)
UNION (SELECT  8 DEPTH_GROUP,'1.251 - 1.650' DESCR, 1.251, 1.650 FROM DUAL)
UNION (SELECT  9 DEPTH_GROUP,'1.651 - 2.480' DESCR, 1.651, 2.480 FROM DUAL)
UNION (SELECT 10 DEPTH_GROUP,'2.481 - 3.590' DESCR, 2.481, 3.590 FROM DUAL)
UNION (SELECT 11 DEPTH_GROUP,'3.591 - 4.590' DESCR, 3.591, 4.590 FROM DUAL)
UNION (SELECT 12 DEPTH_GROUP,'4.591 - 5.700' DESCR, 4.591, 5.700 FROM DUAL)
UNION (SELECT 13 DEPTH_GROUP,'5.701+'        DESCR, 5.701, NULL  FROM DUAL)
)
) R
ON S.SENSOR_DEPTH_IN/12 BETWEEN NVL(R.RNG_START, S.SENSOR_DEPTH_IN/12) AND NVL(R.RNG_STOP, S.SENSOR_DEPTH_IN/12)
WHERE SM.MODEL='TC'
ORDER BY CELL, SEQ
) T
JOIN
(
SELECT CELL, STATION, OFFSET, SEQ_MIN, SEQ_MAX, NUM_SENSORS, TREE
FROM
--MNR.TC_TREE
(SELECT B.CELL_NUMBER CELL,B.STATION_FT STATION,B.OFFSET_FT OFFSET,B.SEQ_MIN,B.SEQ_MAX,B.NUM_SENSORS
, RANK() OVER (PARTITION BY CELL_NUMBER ORDER BY STATION_FT) TREE
FROM (
SELECT C.CELL_NUMBER
,S.STATION_FT, S.OFFSET_FT, MIN(SEQ) SEQ_MIN, MAX(SEQ) SEQ_MAX, COUNT(*) NUM_SENSORS
FROM MNR.CELL C
JOIN MNR.LANE LN ON LN.CELL_ID=C.ID
JOIN MNR.LAYER LY ON LY.LANE_ID=LN.ID
JOIN MNR.SENSOR S ON S.LAYER_ID=LY.ID
JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID=SM.ID
WHERE SM.MODEL='TC'
 AND S.DATE_INSTALLED BETWEEN ? AND ? AND C.CELL_NUMBER=?
GROUP BY C.CELL_NUMBER,S.STATION_FT, S.OFFSET_FT
ORDER BY C.CELL_NUMBER,S.STATION_FT, S.OFFSET_FT
) B)
) D
ON D.CELL=T.CELL AND D.STATION=T.STATION AND D.OFFSET=T.OFFSET
) X
WHERE X.DEPTH_GROUP IS NOT NULL
 */
}

package us.mn.state.dot.mnroad

class JobResult {

  Long id
  String jobName
  Date startTime
  String duration
  String result
  String parameterMap

  static constraints = {
  }

  static mapping = {
    id (generator:'sequence', params:[sequence:'JOBSEQ'])
    version false
    jobName         (nullable:false)
    startTime       (nullable:false)
    duration        (nullable:true)
    result          (nullable:true)
    parameterMap    (nullable:true, type:'text')
  }

  String toString() {
    "${id} ${jobName} ${startTime} ${duration} ${result}"// ${parameterMap} "
  }
}

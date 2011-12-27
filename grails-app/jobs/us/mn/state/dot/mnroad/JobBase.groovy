package us.mn.state.dot.mnroad

import org.joda.time.Period
import org.joda.time.PeriodType
import org.quartz.InterruptableJob
import org.quartz.Scheduler
import org.quartz.JobExecutionException
import org.quartz.UnableToInterruptJobException
import org.apache.commons.logging.Log

abstract class JobBase { //implements InterruptableJob {

  static String defaultCron = '0/30 * * * * ?'
  abstract String jobName()
//  // logging services
//  private static Log _log = LogFactory.getLog(DataProductJob.class);
//
//  // has the job been interrupted?
//  private boolean _interrupted = false;
//
//  // job name
//  private String _jobName = "";
//
//  /**
//   * <p>
//   * Called by the <code>{@link Scheduler}</code> when a user
//   * interrupts the <code>Job</code>.
//   * </p>
//   *
//   * @return void (nothing) if job interrupt is successful.
//   * @throws JobExecutionException
//   *           if there is an exception while interrupting the job.
//   */
//  public void interrupt() throws UnableToInterruptJobException {
//      _log.info("---" + "  -- INTERRUPTING --");
//      _interrupted = true;
//  }

    def getDuration = { sd, ed ->
      Period p = new Period(sd.getTime(), ed.getTime(), PeriodType.time());
      def ds = []
      if (p.hours)
        ds << "${p.hours} hours"
      if (p.minutes)
        ds << "${p.minutes} minutes"
      if (p.millis || p.seconds)
        ds << "${p.seconds}.${String.format("%02d", p.millis)} seconds"
      return ds.join(", ") + "."
    }
}

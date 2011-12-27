package us.mn.state.dot.mnroad

import org.quartz.listeners.JobListenerSupport
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.apache.log4j.Logger

/**
 * Created by IntelliJ IDEA.
 * User: carr1den
 * Date: Apr 5, 2011
 * Time: 7:27:48 AM
 * To change this template use File | Settings | File Templates.
 */

public class MyJobListener extends JobListenerSupport {

    public String getName() {
      return "MyJobListener"
    }

    public void jobWasExecuted(JobExecutionContext context,
                               JobExecutionException jobException) {
      def msg = context.getResult()
      if (msg) {
        //msg.grom()
        log.info msg
      }
    }
}

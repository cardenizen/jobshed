package us.mn.state.dot.mnroad;

import org.joda.time.DateTime;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Carr1Den
 * Date: Jul 22, 2011
 * Time: 3:22:30 PM
 */
public class CellDesign {
    Integer  id;
    Integer  cell;
    Integer  design;
    String   cellType;
    DateTime cellFromDate;
    DateTime cellToDate;
    DateTime sensorFromDate;
    DateTime sensorToDate;
    Integer  seqMin;
    Integer  seqMax;
    Integer  numSensors;

   public CellDesign(Integer i, Integer c, Integer d, String cType
           , Date bDate, Date eDate
           , Date fDate, Date tDate
           , Integer sMin
           , Integer sMax
           , Integer nSensors) {
       id = i;
       cell = c;
       design = d;
       cellType = cType;
       cellFromDate = new DateTime(bDate.getTime());
       if (eDate == null) {
         cellToDate = new DateTime();
       }
       else {
         cellToDate = new DateTime(eDate.getTime());
       }
       sensorFromDate = new DateTime(fDate.getTime());
       if (eDate == null) {
         sensorToDate = new DateTime();
       }
       else {
         sensorToDate = new DateTime(tDate.getTime());
       }
       seqMin = sMin;
       seqMax = sMax;
       numSensors = nSensors;
   }

   public String toString() {
       return String.format("Cell %d, Design %d, %s, %s - %s, %s - %s, %s - %s, %s"
               , cell, design, cellType
               , cellFromDate.toString("E MM/dd/yyyy")
               , cellToDate.toString("E MM/dd/yyyy")
               , sensorFromDate.toString("E MM/dd/yyyy")
               , sensorToDate.toString("E MM/dd/yyyy")
               , seqMin
               , seqMax
               , numSensors
       );
   }
}

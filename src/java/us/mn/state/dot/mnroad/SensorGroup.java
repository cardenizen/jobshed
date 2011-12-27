package us.mn.state.dot.mnroad;

import java.util.ArrayList;
import java.util.Date;


/**
 * Created by IntelliJ IDEA.
 * User: Carr1Den
 * Date: Jul 21, 2011
 * Time: 4:07:05 PM
 * 
 */
public class SensorGroup {
    Integer id;
    Integer cell;
    String cellType;
    Date fromDate;
    Date toDate;
    Integer design;
    Double station;
    Double offset;
    Integer seqMin;
    Integer seqMax;
    Integer numSensors;
    Integer tree;
    ArrayList sensorProps;
}


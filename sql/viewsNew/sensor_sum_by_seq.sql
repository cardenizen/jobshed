-- Depends on:
-- tables MNR.CELL, MNR.LANE, MNR.LAYER, MNR.SENSOR, MNR.SENSOR_MODEL
CREATE OR REPLACE VIEW SENSOR_SUM_BY_SEQ AS
SELECT
C.CELL_NUMBER CELL ,
SM.MODEL ,
MIN(SEQ) SEQ_MIN ,
MAX(SEQ) SEQ_MAX ,
S.STATION_FT STATION ,
S.OFFSET_FT OFFSET ,
COUNT(*) NUM_SENSORS
FROM MNR.CELL C JOIN MNR.LANE LN ON LN.CELL_ID=C.ID JOIN MNR.LAYER LY ON LY.LANE_ID=LN.ID JOIN MNR.SENSOR S ON S.LAYER_ID=LY.ID JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID=SM.ID
GROUP BY C.CELL_NUMBER,SM.MODEL,S.STATION_FT, S.OFFSET_FT
ORDER BY C.CELL_NUMBER,MIN(SEQ)
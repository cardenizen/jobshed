CREATE OR REPLACE VIEW SENSOR_LOCATION AS
SELECT
C.CELL_NUMBER CELL,
SUBSTR (C.CLASS, 24) CLASS,
SM.MODEL,
S.STATION_FT STATION,
S.OFFSET_FT OFFSET,
S.SEQ,
M.BASIC_MATERIAL,
S.SENSOR_DEPTH_IN DEPTH,
S.DATE_REMOVED,
S.ORIENTATION
FROM MNR.SENSOR S JOIN MNR.SENSOR_MODEL SM ON S.SENSOR_MODEL_ID = SM.ID JOIN MNR.LAYER L ON S.LAYER_ID = L.ID JOIN MNR.LANE LN ON L.LANE_ID = LN.ID JOIN MNR.CELL C ON LN.CELL_ID = C.ID JOIN MNR.MATERIAL M ON M.ID = L.MATERIAL_ID
ORDER BY CELL, MODEL, SEQ
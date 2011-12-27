SELECT A.CELL, TO_CHAR(A.FROM_DATE,'dd-Mon-yyyy') CELL_FROM, TO_CHAR(A.TO_DATE,'dd-Mon-yyyy') CELL_TO, DA.DATA_TABLE, TO_CHAR(DA.FROM_DATE,'dd-Mon-yyyy') FROM_DAY, TO_CHAR(DA.TO_DATE,'dd-Mon-yyyy') TO_DAY 
FROM (
SELECT CELL, MIN(CD.FROM_DATE) FROM_DATE, MAX(NVL(CD.TO_DATE,'31-Dec-2010')) TO_DATE FROM (
SELECT D.ID, D.CELL, D.DESIGN, D.CELL_TYPE, D.CONSTRUCTION_BEGAN_DATE FROM_DATE
  , NVL(D.CELL_END_DATE,(SELECT UNIQUE FROM_DATE_OVER-1 TO_DATE FROM MNR.CELL_ON_CELL CC WHERE CC.ID_UNDER=D.ID)) TO_DATE
  , START_STATION,END_STATION FROM
      (SELECT ID, CELL, DESIGN, CELL_TYPE
        , CONSTRUCTION_BEGAN_DATE,CONSTRUCTION_ENDED_DATE
        , NVL(DEMOLISHED_DATE,LEAD(CONSTRUCTION_BEGAN_DATE-1,1,NULL) OVER (PARTITION BY CELL ORDER BY CONSTRUCTION_BEGAN_DATE)) CELL_END_DATE
        , START_STATION,END_STATION FROM
        (SELECT C.ID
	        , C.CELL_NUMBER CELL
	        , CASE WHEN LEAD(ID,1,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 1 
	          WHEN LEAD(ID,2,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 2 
	          WHEN LEAD(ID,3,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 3 
	          WHEN LEAD(ID,4,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 4 
	          WHEN LEAD(ID,5,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 5 
	          ELSE 1 
	          END DESIGN 
	        , SUBSTR(C.CLASS,24) CELL_TYPE
	        , (SELECT FIRST_LAYER_DATE+1 FROM MNR.CELLS WHERE ID=C.ID) CONSTRUCTION_BEGAN_DATE
		        , C.CONSTRUCTION_ENDED_DATE
		        , C.DEMOLISHED_DATE
		        , C.START_STATION
		        , C.END_STATION
        FROM MNR.CELL C
        )
      ) D
) CD
GROUP BY CD.CELL
) A 
JOIN 
(
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE,'DISTRESS_AC' DATA_TABLE
FROM MNR.DISTRESS_AC
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_AGG_SURVEY_SEMI' DATA_TABLE
FROM MNR.DISTRESS_AGG_SURVEY_SEMI
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_ALPS_RESULTS_RUT' DATA_TABLE
FROM MNR.DISTRESS_ALPS_RESULTS_RUT
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_CIRCULR_TEXTR_METER' DATA_TABLE
FROM MNR.DISTRESS_CIRCULR_TEXTR_METER
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE,'DISTRESS_CUPPING' DATA_TABLE
FROM MNR.DISTRESS_CUPPING
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_FRICTION_DFT' DATA_TABLE
FROM MNR.DISTRESS_FRICTION_DFT
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL,  MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_FRICTION_TRAILER' DATA_TABLE
FROM MNR.DISTRESS_FRICTION_TRAILER
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE,'DISTRESS_JPCC' DATA_TABLE
FROM MNR.DISTRESS_JPCC
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_LANE_SHOULDER_DROPOFF' DATA_TABLE
FROM MNR.DISTRESS_LANE_SHOULDER_DROPOFF
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_LIGHTWEIGHT_DEFLECT' DATA_TABLE
FROM MNR.DISTRESS_LIGHTWEIGHT_DEFLECT
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_NUCLEAR_DENSITY' DATA_TABLE
FROM MNR.DISTRESS_NUCLEAR_DENSITY
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_OBSI_DATA' DATA_TABLE
FROM MNR.DISTRESS_OBSI_DATA
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_OBSI_SUMMARY' DATA_TABLE
FROM MNR.DISTRESS_OBSI_SUMMARY
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_PCC_FAULTS' DATA_TABLE
FROM MNR.DISTRESS_PCC_FAULTS
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION
ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_PERMEABILITY_DIRECT' DATA_TABLE
FROM MNR.DISTRESS_PERMEABILITY_DIRECT
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_RIDE_LISA' DATA_TABLE
FROM MNR.DISTRESS_RIDE_LISA
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_RIDE_PATHWAYS' DATA_TABLE
FROM MNR.DISTRESS_RIDE_PATHWAYS
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_RIDE_PAVETECH' DATA_TABLE
FROM MNR.DISTRESS_RIDE_PAVETECH
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_RUTTING_DIPSTICK' DATA_TABLE
FROM MNR.DISTRESS_RUTTING_DIPSTICK
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_RUTTING_STRAIGHT_EDGE' DATA_TABLE
FROM MNR.DISTRESS_RUTTING_STRAIGHT_EDGE
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_SAND_PATCH' DATA_TABLE
FROM MNR.DISTRESS_SAND_PATCH
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_SCHMIDT_HAMMER' DATA_TABLE
FROM MNR.DISTRESS_SCHMIDT_HAMMER
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_SOUND_ABSORPTION' DATA_TABLE
FROM MNR.DISTRESS_SOUND_ABSORPTION
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
UNION ALL
SELECT UNIQUE CELL, MIN(DAY) FROM_DATE, MAX(DAY) TO_DATE, 'DISTRESS_WATER_PERMEABILITY' DATA_TABLE
FROM MNR.DISTRESS_WATER_PERMEABILITY
WHERE DAY <= '31-Dec-2010'
GROUP BY CELL
) DA ON DA.CELL=A.CELL AND (DA.TO_DATE > A.TO_DATE OR DA.FROM_DATE < A.FROM_DATE)

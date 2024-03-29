CREATE OR REPLACE VIEW CELL_DESIGN AS
SELECT
ID,
CELL,
CELL_TYPE,
CONSTRUCTION_BEGAN_DATE FIRST_LAYER_DATE,
CONSTRUCTION_ENDED_DATE,
CELL_END_DATE TO_DATE,
DESIGN_NUMBER
FROM
(
   SELECT
   ID,
   CELL,
   CELL_TYPE,
   CONSTRUCTION_BEGAN_DATE,
   CONSTRUCTION_ENDED_DATE,
   NVL ( DEMOLISHED_DATE, LEAD ( CONSTRUCTION_BEGAN_DATE - 1, 1, NULL) OVER (PARTITION BY CELL ORDER BY CONSTRUCTION_BEGAN_DATE)) CELL_END_DATE,
   DESIGN_NUMBER
   FROM
   (
      SELECT
      ID,
      CELL_NUMBER CELL,
      SUBSTR (CLASS, 24) CELL_TYPE,
      (SELECT FIRST_LAYER_DATE FROM MNR.CELLS WHERE ID = C.ID) CONSTRUCTION_BEGAN_DATE,
      CONSTRUCTION_ENDED_DATE,
      DEMOLISHED_DATE,
      CASE WHEN LEAD
      (
         ID, 1, NULL
      )
      OVER
      (
         PARTITION BY CELL_NUMBER
         ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
      )
      IS NULL THEN 1 WHEN LEAD
      (
         ID, 2, NULL
      )
      OVER
      (
         PARTITION BY CELL_NUMBER
         ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
      )
      IS NULL THEN 2 WHEN LEAD
      (
         ID, 3, NULL
      )
      OVER
      (
         PARTITION BY CELL_NUMBER
         ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
      )
      IS NULL THEN 3 WHEN LEAD
      (
         ID, 4, NULL
      )
      OVER
      (
         PARTITION BY CELL_NUMBER
         ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
      )
      IS NULL THEN 4 WHEN LEAD
      (
         ID, 5, NULL
      )
      OVER
      (
         PARTITION BY CELL_NUMBER
         ORDER BY C.CONSTRUCTION_ENDED_DATE DESC
      )
      IS NULL THEN 5 ELSE 1 END DESIGN_NUMBER
      FROM MNR.CELL C
   )
)

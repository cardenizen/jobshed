SELECT  
--cell, count(*)
L.DCP_ID,L.PROJECT_DESC,L.CELL,L.STATION,L.OFFSET_FT,L.DAY,L.LAYER,L.COMMENTS,L.MOISTURE_PERCENT,L.DENSITY_PCF,L.DEPTH_BELOW_GRADE_IN,V.BLOW_COUNT,V.PROBE_DEPTH_IN 
FROM MNR.DCP_LOCATION L JOIN MNR.DCP_VALUES V ON V.DCP_ID=L.DCP_ID

group by cell

SELECT ID, CELL, TYPE, FROM_DATE, CASE WHEN CS.TO_DATE IS NOT NULL THEN CS.TO_DATE ELSE LEAD(FIRST_LAYER_DATE,1,SYSDATE) OVER (PARTITION BY CELL ORDER BY CELL,FROM_DATE) END TO_DATE FROM MNR.CELLS CS 
order by type, cell, from_date

SELECT layer,count(layer) FROM  MNR.DCP_LOCATION L 
group by layer

L.DCP_ID,L.PROJECT_DESC,L.CELL,L.STATION,L.OFFSET_FT,L.DAY,L.LAYER,L.COMMENTS,L.MOISTURE_PERCENT,L.DENSITY_PCF,L.DEPTH_BELOW_GRADE_IN,V.BLOW_COUNT,V.PROBE_DEPTH_IN 
FROM MNR.DCP_LOCATION L JOIN MNR.DCP_VALUES V ON V.DCP_ID=L.DCP_ID

select L.DCP_ID,L.PROJECT_DESC,L.CELL,L.STATION,L.OFFSET_FT,L.DAY,L.LAYER
FROM MNR.DCP_LOCATION L

select L.DCP_ID,L.PROJECT_DESC,L.CELL,s.start_station,L.STATION,s.end_station,L.OFFSET_FT,s.from_date,L.DAY,s.to_date,L.LAYER from MNR.DCP_LOCATION L join 
(SELECT cs.cell, cs.type, c.start_station,c.end_station,cs.from_date,cs.to_date FROM mnr.cells cs join mnr.cell c on cs.id = c.id 
--where type='AggCell'
) S 
on s.cell=l.cell and l.station between s.start_station and s.end_station
where l.station > 0 


SELECT cs.cell, cs.type, c.start_station,c.end_station,cs.from_date,cs.to_date FROM mnr.cells cs join mnr.cell c on cs.id = c.id 
where c.construction_ended_date < '01-JAN-1994'

SELECT cs.cell, cs.type, c.start_station,c.end_station,cs.from_date,cs.to_date FROM mnr.cells cs join mnr.cell c on cs.id = c.id 
where c.construction_ended_date > '01-JAN-1994'

select L.DCP_ID,L.PROJECT_DESC,L.CELL,s.start_station,L.STATION,s.end_station,L.OFFSET_FT,s.from_date,L.DAY,s.to_date,L.LAYER from MNR.DCP_LOCATION L join 
(
SELECT cs.cell, cs.type, c.start_station,c.end_station,cs.from_date,cs.to_date FROM mnr.cells cs join mnr.cell c on cs.id = c.id 
where type='AggCell' and c.construction_ended_date < '01-JAN-1994'
) S 
on s.cell=l.cell and l.station between s.start_station and s.end_station and l.day < s.from_date
where l.station > 0 

select L.DCP_ID,L.PROJECT_DESC,L.CELL,s.start_station,L.STATION,s.end_station,L.OFFSET_FT,s.from_date,L.DAY,s.to_date,L.LAYER from MNR.DCP_LOCATION L join 
(
SELECT cs.cell, cs.type, c.start_station,c.end_station,cs.from_date,cs.to_date FROM mnr.cells cs join mnr.cell c on cs.id = c.id 
where type='AggCell' and c.construction_ended_date > '01-JAN-1994'
) S 
on s.cell=l.cell and l.station between s.start_station and s.end_station and l.day < s.from_date
where l.station > 0 


select L.DCP_ID,L.PROJECT_DESC,L.CELL,L.STATION,L.DAY,L.LAYER from MNR.DCP_LOCATION L
where l.cell in (32,33,35,35)

select unique L.DCP_ID,L.CELL,L.STATION,L.DAY,L.LAYER,c.* from MNR.DCP_LOCATION L 
join mnr.cell c on c.cell_number=l.cell and l.station between c.start_station and c.end_station
where l.cell in (32,33,35,35)

and c.class='us.mn.state.dot.mnroad.AggCell'

minus

select L.DCP_ID,L.CELL,L.STATION,L.DAY,s.from_date,L.LAYER from MNR.DCP_LOCATION L join 
(
SELECT cs.cell, cs.type, c.start_station,c.end_station,cs.from_date,cs.to_date FROM mnr.cells cs join mnr.cell c on cs.id = c.id 
where type='AggCell' and c.construction_ended_date < '01-JAN-1994'
) S 
on s.cell=l.cell and l.station between s.start_station and s.end_station and l.day < s.from_date
where l.station > 0 

and l.cell in (32,33,35,35)




select unique 
L.DCP_ID,L.CELL,L.STATION,L.DAY,c.construction_ended_date,L.LAYER,substr(c.class,24) type 
from MNR.DCP_LOCATION L 
join mnr.cell c on c.cell_number=l.cell and l.station between c.start_station and c.end_station
where substr(c.class,24)='AggCell'

select unique 
L.DCP_ID,L.PROJECT_DESC,L.CELL,L.STATION,L.OFFSET_FT,L.DAY,L.LAYER,L.COMMENTS,L.MOISTURE_PERCENT,L.DENSITY_PCF,L.DEPTH_BELOW_GRADE_IN
,V.BLOW_COUNT,V.PROBE_DEPTH_IN FROM MNR.DCP_LOCATION L JOIN MNR.DCP_VALUES V ON V.DCP_ID=L.DCP_ID
join mnr.cell c on c.cell_number=l.cell and l.station between c.start_station and c.end_station
where substr(c.class,24)='AggCell'

select unique 
L.DCP_ID,L.PROJECT_DESC,L.CELL,L.STATION,L.OFFSET_FT,L.DAY,L.LAYER,L.COMMENTS,L.MOISTURE_PERCENT,L.DENSITY_PCF,L.DEPTH_BELOW_GRADE_IN
,V.BLOW_COUNT,V.PROBE_DEPTH_IN FROM MNR.DCP_LOCATION L JOIN MNR.DCP_VALUES V ON V.DCP_ID=L.DCP_ID
join mnr.cell c on c.cell_number=l.cell and l.station between c.start_station and c.end_station
where substr(c.class,24)='HmaCell'
order by cell,station,offset_ft
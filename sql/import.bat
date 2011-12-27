sqlplus mnr/mnr@localhost/XE as sysdba @drop.sql
imp 'mnr/mnr@localhost/XE as sysdba' tables=(JOB_SETUP,JOB_RESULT) fromuser=mnr touser=mnr file=JobShedData.dmp

imp 'mnr/mnr@localhost/XE as sysdba' tables=(JOB_SETUP) indexfile=index.sql file=JobShedData.dmp
select 'update mnr.job_setup set cron_expr='''|| cron_expr || ''' where job_name='''||job_name||''';' from mnr.job_setup;

sqlplus mnr/dev11mnr@MRL2K3DEV.ad.dot.state.mn.us:1521/dev11.ad.dot.state.mn.us @drop.sql
imp 'mnr/dev11mnr@MRL2K3DEV.ad.dot.state.mn.us:1521/dev11.ad.dot.state.mn.us' tables=(JOB_SETUP,JOB_RESULT) fromuser=mnr touser=mnr file=mnrlt.dmp

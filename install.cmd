net stop tomcat6
call grails prod war
copy /Y target\jobshed-1.0.war "%CATALINA_HOME%\webapps\jobshed.war"
del /Q /S /F "%CATALINA_HOME%\webapps\jobshed\" > nul
del /Q /S /F "%CATALINA_HOME%\logs\*.*" > nul
rmdir /Q /S "%CATALINA_HOME%\webapps\jobshed\"
net start tomcat6


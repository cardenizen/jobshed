// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
grails.views.javascript.library="jquery"
grails.views.javascript.library="jquery"

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }

}
// Copied from www.gnumims.org
/**
 * Log4j configuration.
 * Causing this file to reload (e.g. edit+save) may break the appLog destination
 * and further logs will be written to files or directories like "[:]".
 * For more info see http://logging.apache.org/log4j/1.2/manual.html
 * For log levels see http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Level.html
 * Basic log levels are ALL < TRACE < DEBUG < INFO < WARN < ERROR < FATAL < OFF
 */
// Pickup the Tomcat/Catalina logDirectory else use the current dir.
def catalinaBase = System.properties.getProperty('catalina.base')
def logDirectory = catalinaBase ? "${catalinaBase}/logs" : '.'
log4j = {
    appenders {
        // Use if we want to prevent creation of a stacktrace.log file.
//        'null' name:'stacktrace'

        // Use this if we want to modify the default appender called 'stdout'.
        console name:'stdout', layout:pattern(conversionPattern: '%d{dd-MMM@HH:mm:ss}-%m%n')

        // Custom log file.
        rollingFile name:"appLog",
                        file:"${logDirectory}/${appName}.log".toString(),
                        maxFileSize:'300kB',
                        maxBackupIndex:0,
                        layout:pattern(conversionPattern: '%d{[EEE, dd-MMM-yyyy @ HH:mm:ss.SSS]} [%t] %-5p %c %x - %m%n')
    }

    // Configure the root logger to output to stdout and appLog appenders.
    root {
      error 'stdout','appLog'
    //  info 'stdout'
        additivity = true
    }

    // This is for the builtin stuff and from the default Grails-1.1.1 config.
    error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'org.hibernate'

    warn   'org.mortbay.log' // Jetty

    error "grails.app" // Set the default log level for our app code.
    info "grails.app.bootstrap" // Set the log level per type and per type.class
//    error "grails.app.service.NavigationService"
//    error "grails.app.service.com.zeddware.grails.plugins.filterpane.FilterService"

    // Move anything that should behave differently into this section.
    switch(environment) {
        case 'development':
            debug "grails.app.service"
            debug "grails.app.controller"
            break
        case 'test':
            debug "grails.app.service"
            debug "grails.app.controller"
            info "us.mn.state.dot.mnroad"
            break
        case 'production':
            warn "grails.app.service"
            warn "grails.app.controller"
            info "us.mn.state.dot.mnroad"
            break
    }
}

plugins {
 applet {
   jars {
     '/cronExprBldrApplet.jar' {
       groovy = false; // set to false to skip including groovy-all-* in the jar
       classes = ['wjw.cron.ex.*.class','lib/quartz-1.7.3.jar']
       sign {
         alias = 'default' //your alias in keystore
         storepass = 'default'//alternatively, use a system property with -Dstorepass=password
       }
     }
   }
 }
}

ldap {
  server.url = "ldap://ldapad:389/"
  search.base = "DC=ad,DC=dot,DC=state,DC=mn,DC=us"
  search.user = "ldapbrowse"
  search.pass = "ldapbrowse"
  username.attribute = "sAMAccountName"
  usersearch.filter = "(&(sAMAccountName={0})(objectClass=person))"
  referrals = "follow"
  role.base = "DC=ad,DC=dot,DC=state,DC=mn,DC=us"
  role.search="(&(member={0})(objectClass=group))"
//  allowEmptyPasswords=true
//  skip.authentication = true
//  // skip.credentialsCheck if in development not connected to an LDAP server
//  skip.credentialsCheck  = true
  allowEmptyPasswords=false
  skip.authentication = false
  // skip.credentialsCheck if in development not connected to an LDAP server
  switch(environment) {
      case 'development':
        skip.credentialsCheck  = true
          break
      case 'test':
        skip.credentialsCheck  = false
          break
      case 'production':
          break
  }
}

currentSchema='MNR'

/* Data Product configuration defaults. */
pauseJobsAtStartup = true
defaultCronExpr = "* * 0/12 * * ?"
uncdrive = "\\\\Ad\\Mrl\\SECTIONS\\RESEARCH\\"
shareddrive = "\\\\mrl2k3cadd\\research\$\\"
wrdrive = "R:\\"
cdrive = "C:\\"

includeTcDataQualityFlag = false

//[Thu, 12-May-2011 @ 12:26:27.382] [quartzScheduler_Worker-7] INFO  us.mn.state.dot.mnroad.MyJobListener  - Job Result 94 saved. Cannot find Data Product Folder: R:\MnROAD\Data Product v 1.0
//[Thu, 12-May-2011 @ 12:41:33.210] [quartzScheduler_Worker-10] INFO  us.mn.state.dot.mnroad.MyJobListener  - Job Result 97 saved. Cannot find Data Product Folder: \\ad\mrl\SECTIONS\RESEARCH\MnROAD\Data Product v 1.0
//[Thu, 12-May-2011 @ 12:44:21.679] [quartzScheduler_Worker-1] INFO  us.mn.state.dot.mnroad.MyJobListener  - Job Result 98 saved. Cannot find Data Product Folder: \\mrl2k3cadd\research$\MnROAD\Data Product v 1.0


jointDowelFolder = "${rdrive}${dataProductDataFolder}${ddByType}\\PCC - Concrete\\Concrete Panel Details"
// Added by the Joda-Time plugin:
grails.gorm.default.mapping = {
	"user-type" type: org.joda.time.contrib.hibernate.PersistentDateTime, class: org.joda.time.DateTime
	"user-type" type: org.joda.time.contrib.hibernate.PersistentDuration, class: org.joda.time.Duration
	"user-type" type: org.joda.time.contrib.hibernate.PersistentInstant, class: org.joda.time.Instant
	"user-type" type: org.joda.time.contrib.hibernate.PersistentInterval, class: org.joda.time.Interval
	"user-type" type: org.joda.time.contrib.hibernate.PersistentLocalDate, class: org.joda.time.LocalDate
	"user-type" type: org.joda.time.contrib.hibernate.PersistentLocalTimeAsString, class: org.joda.time.LocalTime
	"user-type" type: org.joda.time.contrib.hibernate.PersistentLocalDateTime, class: org.joda.time.LocalDateTime
	"user-type" type: org.joda.time.contrib.hibernate.PersistentPeriod, class: org.joda.time.Period
}

// Special Handling must be added for certain tables
// large tables hold readings in separate tables for each year
// and a view is used to read them all as one table
// TC_VALUES_ALL, VW_VALUES_ALL, WM_VALUES_ALL, XV_VALUES_ALL
largeTables=['TC','VW','WM','XV']
useLargeTables=false
//largeTables=[] // Used for testing on XE database
// In some tables the reading(s) is stored in a column named other than "VALUE"
valueColumnNames = [
 'TB_VALUES':'NORTH_VALUE,SOUTH_VALUE'
,'TD_VALUES':'VALUE,LA'
,'HD_VALUES':'TEMPDIFF,INITTEMP,TEMPAT39SEC'
]

/*
cellAttrsQuery
 */
cellAttrsQuery="""
SELECT CELL,CELL_TYPE
  ,FIRST_LAYER_DATE FROM_DATE
  ,NVL(TO_DATE,LEAD(FIRST_LAYER_DATE-1,1,NULL) OVER (PARTITION BY CELL ORDER BY FIRST_LAYER_DATE)) TO_DATE
  ,DESIGN_NUMBER FROM MNR.CELL_DESIGN
  WHERE CELL_TYPE=? ORDER BY CELL, FROM_DATE
"""
cellFromToYear = """
SELECT ID,CELL,CONSTRUCTION_BEGAN_DATE FIRST_LAYER_DATE,CONSTRUCTION_ENDED_DATE,CELL_END_DATE  TO_DATE,DESIGN_NUMBER FROM (SELECT ID,CELL,CELL_TYPE,CONSTRUCTION_BEGAN_DATE,CONSTRUCTION_ENDED_DATE,NVL(DEMOLISHED_DATE,LEAD(CONSTRUCTION_BEGAN_DATE-1,1,NULL) OVER (PARTITION BY CELL ORDER BY CONSTRUCTION_BEGAN_DATE)) CELL_END_DATE,DESIGN_NUMBER FROM (SELECT ID,CELL_NUMBER CELL,SUBSTR(CLASS,24) CELL_TYPE,(SELECT FIRST_LAYER_DATE FROM MNR.CELLS WHERE ID=C.ID) CONSTRUCTION_BEGAN_DATE,CONSTRUCTION_ENDED_DATE,DEMOLISHED_DATE,CASE WHEN LEAD(ID,1,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 1 WHEN LEAD(ID,2,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 2 WHEN LEAD(ID,3,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 3 WHEN LEAD(ID,4,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 4 WHEN LEAD(ID,5,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 5 ELSE 1 END DESIGN_NUMBER FROM MNR.CELL C))
  WHERE CELL = ? AND ? BETWEEN TO_NUMBER(TO_CHAR(CONSTRUCTION_BEGAN_DATE,'YYYY')) AND TO_NUMBER(TO_CHAR(CELL_END_DATE,'YYYY')) 
ORDER BY CELL, CONSTRUCTION_BEGAN_DATE
"""

cellFromToCell = """
SELECT ID,CELL,CONSTRUCTION_BEGAN_DATE FIRST_LAYER_DATE,CONSTRUCTION_ENDED_DATE,CELL_END_DATE  TO_DATE,DESIGN_NUMBER FROM (
  SELECT ID,CELL,CELL_TYPE,CONSTRUCTION_BEGAN_DATE,CONSTRUCTION_ENDED_DATE,NVL(DEMOLISHED_DATE,LEAD(CONSTRUCTION_BEGAN_DATE-1,1,NULL) OVER (PARTITION BY CELL ORDER BY CONSTRUCTION_BEGAN_DATE)) CELL_END_DATE,DESIGN_NUMBER FROM (
    SELECT ID,CELL_NUMBER CELL,SUBSTR(CLASS,24) CELL_TYPE,(SELECT FIRST_LAYER_DATE FROM MNR.CELLS WHERE ID=C.ID) CONSTRUCTION_BEGAN_DATE,CONSTRUCTION_ENDED_DATE,DEMOLISHED_DATE,CASE WHEN LEAD(ID,1,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 1 WHEN LEAD(ID,2,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 2 WHEN LEAD(ID,3,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 3 WHEN LEAD(ID,4,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 4 WHEN LEAD(ID,5,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 5 ELSE 1 END DESIGN_NUMBER
      FROM MNR.CELL C
    )
  ) WHERE CELL BETWEEN ? AND ? ORDER BY CELL, CONSTRUCTION_BEGAN_DATE
"""

/*
 <!-- Production MnROAD -->
<Resource name="jdbc/prod_mnr"
    url="jdbc:oracle:thin:@MRL2K3dev.ad.dot.state.mn.us:1521:dev11"
    username="mnr"
    password="dev11mnr"
or
    url="jdbc:oracle:thin:@MRL2K3MRDB.ad.dot.state.mn.us:1521:mnrd"
    username="mnru"
    password="mnru"

    auth="Container"
    type="javax.sql.DataSource"
    driverClassName="oracle.jdbc.OracleDriver"
    hibernate.default_schema = 'MNR'
    maxIdle="3"
    maxActive="10"
    removeAbandoned="true"
    removeAbandonedTimeout="60"
    testOnBorrow="true"
    logAbandoned="true"
    factory="org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory"
    validationQuery="select count(*) from dual"
    maxWait="-1"/>
*/
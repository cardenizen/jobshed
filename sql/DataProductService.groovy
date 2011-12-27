package us.mn.state.dot.mnroad

import groovy.sql.Sql
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.listeners.JobListenerSupport

class DataProductService extends JobListenerSupport {

  boolean transactional = true
  def sessionFactory
  def dataSource
  def config = [:]

  def jobResult = ""

  public String getName() {
    return "DataProductService"
  }

  public void jobWasExecuted(JobExecutionContext context,
                             JobExecutionException jobException) {
    jobResult = "${new Date()} - Job Listener; jobWasExecuted.  Result: ${context.getResult()}"
    //jobResult.grom() // notify anyone listening
  }

  def dataProduct(def cfg) {
    //def exportDataService =  ctx.getBean("exportDataService")  // used in the console
    config = cfg
    if ((config.size()>1) && !config.rdrive) {
      log.error "Required parameter 'rdrive' is null" //.grom()
      return
    }

    def status = ""
    if (config["exportLayersFiles"] == "true") {
      def designDetailsByTypeFolders = createFoldersByType(config.ddByType)
      if (designDetailsByTypeFolders)    {
        config.pavementTypes.keySet().each { shortName ->
          def exportTarget = "Layer"
          try {
            status = exportLayers(exportTarget, designDetailsByTypeFolders.get(shortName), shortName)
            log.info "${shortName} exports completed normally to folder ${designDetailsByTypeFolders.get(shortName)}."
          } catch (Exception e) {
            status = "${shortName} ${exportTarget}  Export failed: ${e}"
            log.error status
          }
          println status
        }
      }
    }

    if (config["exportSensorFiles"] == "true") {
      def sensorDetailsByTypeFolders = createFoldersByType(config.sensorByType)
      if (sensorDetailsByTypeFolders)    {
        config.pavementTypes.keySet().each { shortName ->
          def exportTarget = "Sensor"
          try {
            status = exportSensors(exportTarget, sensorDetailsByTypeFolders.get(shortName), shortName)
            log.info "${shortName} exports completed normally to folder ${sensorDetailsByTypeFolders.get(shortName)}."
          } catch (Exception e) {
            status = "${shortName} ${exportTarget}  Export failed: ${e}"
            log.error status
            e.printStackTrace()
          }
          println status
        }
      }
    }

    if (config["exportTransverseJointsFiles"] == "true") {
      status = exportTransverseJoints()
      println status
    }

    if (config["exportMatSamplesFiles"] == "true") {
      def materialSamplesDataFolder = createFoldersByType(config.materialSamplesDataFolder)
      if (materialSamplesDataFolder)    {
        try {
          status = exportMatSamples(materialSamplesDataFolder)
          log.info "Material Samples Export completed normally."
        } catch (Exception e) {
          log.error "Material Samples Export failed: ${e}"
        }
      }
    }

    if (config["exportFwdFiles"] == "true"
      || config["exportDistressFiles"] == "true"
      || config["exportDcpFiles"] == "true"
      ) {
      def measuredDataFolders = createFoldersByType(config.measuredDataFolder)
      if (measuredDataFolders)    {
        if (config["exportDistressFiles"] == "true") {
          try {
            exportDistress(measuredDataFolders)
            log.info "Distress Export completed normally."
          } catch (Exception e) {
            log.error "Distress Export failed: ${e}"
          }
        }
        if (config["exportDcpFiles"] == "true") {
          try {
            exportDcp(measuredDataFolders)
            log.info "DCP Export completed normally."
          } catch (Exception e) {
            log.error "DCP Export failed: ${e}"
          }
        }
        if (config["exportFwdFiles"] == "true") {
          config.pavementTypes.keySet().each { shortName ->
            def exportTarget = "FWD"
            try {
              println exportFwd(exportTarget, measuredDataFolders.get(shortName), shortName)
              log.info "${shortName} exports completed normally to folder ${measuredDataFolders.get(shortName)}."
            } catch (Exception e) {
              log.error "${shortName} ${exportTarget}  Export failed: ${e}"
              e.printStackTrace()
            }
          }
        }
      }
    }
  }

  def String exportLayers (def target, def typeFolder, def shortName) {
    def status = "${shortName}Query not found"
    def fn = "${shortName} All Cells.csv"
    def q = config["${shortName}Query"]
    if (q) {
      def aq = "${q} WHERE SUBSTR(C.CLASS,24)=? ORDER BY CELL,FROM_DATE".toString()
      def argList = [shortName]
      if (config["overwriteLayersFiles"]) {
        def csvList = queryToCsvList(aq, argList)
        status = writeItems(csvList, "${target} Details", typeFolder, fn)
        def dnMap = designNumMap()
        Sql sql = new groovy.sql.Sql(dataSource)
        def cq = config["CellsQuery"].toString()
        def nCells = 0
        sql.eachRow(cq, [shortName]) { cellidrow ->
          def lq = "${q} WHERE SUBSTR(C.CLASS,24)=? AND CELL_ID=? ORDER BY CELL,FROM_DATE".toString()
          def dn = dnMap.get(cellidrow.ID)
          fn = "${shortName} ${cellidrow.CELL} Design ${dn}.csv"
          csvList = queryToCsvList(lq, [shortName, new BigDecimal(cellidrow.ID)])
          status = writeItems(csvList, "${target} Details", typeFolder, fn)
          log.info "${status} from  cell ${cellidrow.CELL}."
          nCells++
        }
        status = "${nCells} ${shortName} cells written."
      } else {
        status = "${shortName} layer export suppressed by config.overwriteLayerFiles: ${config.overwriteLayerFiles}"
        log.info status
      }
    } else {
      log.info status
    }
    return status
  }

  def String exportSensors (def target, def typeFolder, def shortName) {
    def status = "sensorQuery not found"
    def fn = "${shortName} All Sensors.csv"
    def q = config["sensorQuery"]
    if (q) {
      def aq = "${q} WHERE SUBSTR(C.CLASS,24)=? ORDER BY CELL,MODEL,SEQ".toString()
      def argList = [shortName]
      if (config["overwriteSensorFiles"]) {
        def csvList = queryToCsvList(aq, argList)
        status = writeItems(csvList, "${target} Details", typeFolder, fn)
        def dnMap = designNumMap()
        Sql sql = new groovy.sql.Sql(dataSource)
        def cq = config["CellsQuery"].toString()
        def nCells = 0
        sql.eachRow(cq, [shortName]) { cellidrow ->
          def lq = "${q} WHERE SUBSTR(C.CLASS,24)=? AND CELL_ID=? ORDER BY CELL,MODEL,SEQ".toString()
          def dn = dnMap.get(cellidrow.ID.longValue())
          fn = "${shortName} ${cellidrow.CELL} Design ${dn} Sensors.csv"
          csvList = queryToCsvList(lq, [shortName, new BigDecimal(cellidrow.ID)])
          status = writeItems(csvList, "${target} Details", typeFolder, fn)
          log.info "${status} from  cell ${cellidrow.CELL}."
          nCells++
        }
        status = "${nCells} ${shortName} cells written."
      } else {
        status = "${shortName} sensors suppressed by config[overwriteSensorFiles]: ${config["overwriteSensorFiles"]}"
        log.info status
      }
    } else {
      log.info status
    }
  return status
  }

  def String exportTransverseJoints () {
    def status = "trjQuery not found"
    def folder = config["jointDowelFolder"]
    def fqfn = "${folder}\\transverse joints.csv"
    def q = config["trjQuery"]
    if (q) {
      def aq = "${q} ORDER BY CELL_ID, LANE_ID, LAYER_ID".toString()
      if (config["overwriteTransverseJointsFiles"]) {
        def csvList = queryToCsvList(aq,[])
        status = writeItems(csvList, fqfn)
      } else {
        status = "${shortName} transverse joints suppressed by config.overwriteTransverseJointsFiles: ${config["overwriteTransverseJointsFiles"]}"
        log.info status
      }
    }
    return status
  }

  def String exportFwd(def target,def typeFolder, def shortName) {
    def status = "fwdQuery not found"
    def q = config["fwdQuery"]
    def dnMap = designNumMap()
    Sql sql = new groovy.sql.Sql(dataSource)
    def cq = config["CellsQuery"].toString()
    try {
      def nCells = 0
      sql.eachRow(cq, [shortName]) { cellidrow ->
        def lq = "${q}  WHERE MC.TYPE=? AND MC.CELL=? AND S.SESSION_DATE BETWEEN ? AND ? ORDER BY CELL,SESSION_DATE".toString()
        def dn = dnMap.get(cellidrow.ID.longValue())
        def fileName = "${shortName} ${cellidrow.CELL} Design ${dn} Fwd Drops.csv".toString()
        def sqlDateFrom = new java.sql.Date(cellidrow.from_date.getTime())
        def sqlDateTo = new java.sql.Date(cellidrow.to_date.getTime())
        def ddf = MrUtils.mkBranch(typeFolder, config.loadFolders)
        def fqfn = MrUtils.fqfn(ddf,fileName)
        // queryToCsvFile streams data to a file.  Used for large data exports.
        def rowsExported = queryToCsvFile(lq, [shortName, new BigDecimal(cellidrow.CELL), sqlDateFrom, sqlDateTo], fqfn)
        log.info "${rowsExported} rows exported to ${fileName}."
        nCells++
      }
      status = "${nCells} ${shortName} cells written."
     } catch (java.sql.SQLSyntaxErrorException ses) {
       status = "${shortName} FWD failsed: ${ses.message}"
     }
  return status
  }

  def exportMatSamples(def typeFolders) {
    Sql sql = new groovy.sql.Sql(dataSource)
    Map columnTypeMap = [:]
    Map dataMap = [:]

    def writeRows={ row ->
      def rr = row.toRowResult()
      def r = ""
      def a = []
      def nl = rr.keySet().toList()
      rr.eachWithIndex { it,i ->
        if (nl[i]!="ID") {
          Object o = rr[i]
          def typ = columnTypeMap.get(i+1)
          a << SqlDataFormat.formatSqlValue(o, typ, false)
        }
      }
      dataMap["rows"] << "${a.join(',')}"
      dataMap["nrows"]++
    }

    def writeHeader = { meta ->
      def cn = [:]
      columnTypeMap.clear()
      (1..meta.columnCount).each {
        def colName = meta.getColumnLabel(it)
        if (colName != "ID") {
          cn.put(it,colName)
          columnTypeMap.put(it,meta.getColumnTypeName(it))
          }
        }
      def fileHdr = "${cn.keySet().collect {cn.get(it)}.join(",")}\n"
      dataMap["hdr"] = fileHdr
      dataMap["rows"] = []
      dataMap["nrows"] = 0
     }

    dataMap.put("rowsWritten", 0)
    dataMap.put("nrows", 0)
    dataMap.put("hdr", "")
    dataMap.put("rows", [])

    def drive = config.rdrive
    def dir = MrUtils.mkBranch(drive, config["dataProductDataFolder"])
    dir = MrUtils.mkBranch(dir, config["materialSamplesDataFolder"])
    def fqfn = "${dir}\\MATERIAL_SAMPLES.CSV"
    def q = "SELECT * FROM MNR.MAT_SAMPLES ORDER BY MNROAD_ID"
    def csvList
    try {
      csvList = queryToCsvList(q, [])
    } catch (Exception e) {
      log.info e.message
    }
    if (csvList.size()>0) {
      println "Writing material samples to ${fqfn}."
      def status = writeItems(csvList, fqfn)
    }

    typeFolders.keySet().each { cellType ->
      dir = MrUtils.mkdir(typeFolders.get(cellType),config["matTestsFolder"])
      def fileMap = [:]
      def fileWriterMap = [:]
      def matTableList = config.matTableFolderMap.keySet() as List
      for (tableName in matTableList){
        try {
          q = config["CellsQuery"]
          def rs = sql.rows("${q} ORDER BY CELL,FROM_DATE".toString(),[cellType])
          rs.each { row ->
            q = "${config.matSamplesQuery} JOIN MNR.${tableName} MST ON MST.MNROAD_ID=MS.MNROAD_ID WHERE MS.CELL=? AND SAMPLE_DATE BETWEEN ? AND ? ORDER BY MS.CELL,SAMPLE_DATE"
            def sqlDateFrom = new java.sql.Date(row.from_date.getTime())
            def sqlDateTo = new java.sql.Date(row.to_date.getTime())
            sql.eachRow(q.toString(),[row.cell, sqlDateFrom, sqlDateTo],writeHeader,writeRows)
            if (dataMap["nrows"]) {
              def subdir = config.matTableFolderMap[tableName]
              def folder = subdir?MrUtils.mkBranch(dir,subdir):dir
              def fileName = "${tableName}.csv"
              def fn = "${folder}\\${fileName}"
              FileWriter fw = fileWriterMap.get(fn)
              if (!fw) {
                def f = new File(fn)
                if (!f.exists() || (f.exists()&& config["overwriteDcpFiles"])) {
                  fw = new FileWriter(f, false)
                  fileMap.put(fn,f)
                  fileWriterMap.put(fn,fw)
                  dataMap.put("rowsWritten", 0)
                }
              }
              if (fw && config["overwriteMatSamplesFiles"]) {
                if (dataMap["rowsWritten"]==0) {
                  def f = fileMap.get(fn)
                  if (f.length() > 0) {
                    def aFileWriter = new FileWriter(f, false)
                    fileWriterMap.put(fn,aFileWriter)
                    fw = aFileWriter
                  }
                  fw.append dataMap["hdr"]
                }
                writeFile(dataMap["rows"],fw)
                dataMap["rowsWritten"]=dataMap["rows"].size()
                log.info "${cellType}: ${tableName} rows from cell ${row.cell}: ${dataMap["rowsWritten"]}"
              }
            }
          }
        } catch (Exception e) {
          log.error "${e.message} retrieving ${q}."
        }
      }
      if (fileWriterMap) {
        fileWriterMap.keySet().each { aFileName ->
         //println "Closing ${aFileName}."
         fileWriterMap.get(aFileName).close()
        }
        fileWriterMap.clear()
      }
    }
  }

  def exportDcp(def typeFolders) {
    Sql sql = new groovy.sql.Sql(dataSource)
    Map columnTypeMap = [:]
    Map dataMap = [:]

    def writeRows={ row ->
      def rr = row.toRowResult()
      def r = ""
      def a = []
      def nl = rr.keySet().toList()
      rr.eachWithIndex { it,i ->
        if (nl[i]!="ID") {
          Object o = rr[i]
          def typ = columnTypeMap.get(i+1)
          a << SqlDataFormat.formatSqlValue(o, typ, false)
        }
      }
      dataMap["rows"] << "${a.join(',')}"
      dataMap["nrows"]++
    }

    def writeHeader = { meta ->
      def cn = [:]
      columnTypeMap.clear()
      (1..meta.columnCount).each {
        def colName = meta.getColumnLabel(it)
        if (colName != "ID") {
          cn.put(it,colName)
          columnTypeMap.put(it,meta.getColumnTypeName(it))
          }
        }
      def fileHdr = "${cn.keySet().collect {cn.get(it)}.join(",")}\n"
      dataMap["hdr"] = fileHdr
      dataMap["rows"] = []
      dataMap["nrows"] = 0
     }

    dataMap.put("rowsWritten", 0)
    dataMap.put("nrows", 0)
    dataMap.put("hdr", "")
    dataMap.put("rows", [])
    typeFolders.keySet().each { cellType ->
      def dir = typeFolders.get(cellType)
      dir = MrUtils.mkdir(dir,config.physMeasureFolder)
      def q = ""
      def fileMap = [:]
      def fileWriterMap = [:]
      try {
        q = config["CellsQuery"]
        def rs = sql.rows("${q} ORDER BY CELL,FROM_DATE".toString(),[cellType])
        rs.each { row ->
          q = "${config.dcpQuery} where cell=? and day between ? and ? order by cell,day"
          def sqlDateFrom = new java.sql.Date(row.from_date.getTime())
          def sqlDateTo = new java.sql.Date(row.to_date.getTime())
          sql.eachRow(q.toString(),[row.cell, sqlDateFrom, sqlDateTo],writeHeader,writeRows)
          if (dataMap["nrows"]) {
            def subdir = config.distressTableFolderMap["DCP_LOCATION"]
            def dcpFolder = MrUtils.mkBranch(dir,subdir)
            def dcpFileName = "DCP.csv"
            def fn = "${dcpFolder}\\${dcpFileName}"
            FileWriter fw = fileWriterMap.get(fn)
            if (!fw) {
              def f = new File(fn)
              if (!f.exists() || (f.exists()&& config.overwriteDcpFiles)) {
                fw = new FileWriter(f, false)
                fileMap.put(fn,f)
                fileWriterMap.put(fn,fw)
                dataMap.put("rowsWritten", 0)
              }
            }
            if (fw && config.overwriteDcpFiles) {
              if (dataMap["rowsWritten"]==0) {
                def f = fileMap.get(fn)
                if (f.length() > 0) {
                  // If file exists but no rows have been written it must be an old file.  So overwrite it.
                  def aFileWriter = new FileWriter(f, false)
                  fileWriterMap.put(fn,aFileWriter)
                  fw = aFileWriter
                }
                fw.append dataMap["hdr"]
              }
              writeFile(dataMap["rows"],fw)
              dataMap["rowsWritten"]=dataMap["rows"].size()
              log.info "${cellType}: DCP rows from cell ${row.cell}: ${dataMap["rowsWritten"]}"
            }
          }
        }
      } catch (Exception e) {
        log.error "${e.message} retrieving ${q}."
      }
      if (fileWriterMap) {
        fileWriterMap.keySet().each { aFileName ->
         //println "Closing ${aFileName}."
         fileWriterMap.get(aFileName).close()
        }
        fileWriterMap.clear()
      }
    }
  }

  def exportDistress(def typeFolders) {
    Sql sql = new groovy.sql.Sql(dataSource)
    Map columnTypeMap = [:]
    Map dataMap = [:]

    def writeRows={ row ->
      def rr = row.toRowResult()
      def r = ""
      def a = []
      def nl = rr.keySet().toList()
      rr.eachWithIndex { it,i ->
        if (nl[i]!="ID") {
          Object o = rr[i]
          def typ = columnTypeMap.get(i+1)
          a << SqlDataFormat.formatSqlValue(o, typ, false)
        }
      }
      dataMap["rows"] << "${a.join(',')}"
      dataMap["nrows"]++
    }

    def writeHeader = { meta ->
      def cn = [:]
      columnTypeMap.clear()
      (1..meta.columnCount).each {
        def colName = meta.getColumnLabel(it)
        if (colName != "ID") {
          cn.put(it,colName)
          columnTypeMap.put(it,meta.getColumnTypeName(it))
          }
        }
      def fileHdr = "${cn.keySet().collect {cn.get(it)}.join(",")}\n"
      dataMap["hdr"] = fileHdr
      dataMap["rows"] = []
      dataMap["nrows"] = 0
     }

    dataMap.put("rowsWritten", 0)
    dataMap.put("nrows", 0)
    dataMap.put("hdr", "")
    dataMap.put("rows", [])

    typeFolders.keySet().each { cellType ->
      def dir = typeFolders.get(cellType)
      dir = MrUtils.mkdir(dir,config.physMeasureFolder)
      def q = ""
      def fileMap = [:]
      def fileWriterMap = [:]
      try {
        q = "SELECT * FROM MNR.DISTRESS_CELL WHERE TYPE=?"
        def rs = sql.rows(q.toString(),[cellType])
        rs.each { row ->
          q = "select * from ${config.currentSchema}.${row.data_table} where cell=? and day between ? and ? order by cell,day"//,lane,day"
          def sqlDateFrom = new java.sql.Date(row.from_date.getTime())
          def sqlDateTo = new java.sql.Date(row.to_date.getTime())
          sql.eachRow(q.toString(),[row.cell, sqlDateFrom, sqlDateTo],writeHeader,writeRows)
          if (dataMap["nrows"]) {
            def subdir = config.distressTableFolderMap[row.data_table]
            def distressFolder = MrUtils.mkBranch(dir,subdir)
            def distressFileName = "${row.data_table.toString().substring(9)}.csv"
            if (config.distressFileNameMap[row.data_table])
              distressFileName = "${config.distressFileNameMap[row.data_table]}.csv"
            def fn = "${distressFolder}\\${distressFileName}"
            FileWriter fw = fileWriterMap.get(fn)
            if (!fw) {
              def f = new File(fn)
              if (!f.exists() || (f.exists()&& config.overwriteDistressFiles)) {
                fw = new FileWriter(f, false)
                fileMap.put(fn,f)
                fileWriterMap.put(fn,fw)
                dataMap.put("rowsWritten", 0)
              }
            }
            if (fw && config.overwriteDistressFiles) {
              if (dataMap["rowsWritten"]==0) {
                def f = fileMap.get(fn)
                if (f.length() > 0) {
                  // If file exists but no rows have been written it must be an old file.  So overwrite it.
                  def aFileWriter = new FileWriter(f, false)
                  fileWriterMap.put(fn,aFileWriter)
                  fw = aFileWriter
                }
                fw.append dataMap["hdr"]
              }
              writeFile(dataMap["rows"],fw)
              dataMap["rowsWritten"]=dataMap["rows"].size()
              log.info "${cellType}: ${row.data_table} rows from cell ${row.cell}: ${dataMap["rowsWritten"]}"
            }
          }
        }
      } catch (Exception e) {
        log.error "${e.message} retrieving ${q}."
      }
      if (fileWriterMap) {
        fileWriterMap.keySet().each { aFileName ->
         //println "Closing ${aFileName}."
         fileWriterMap.get(aFileName).close()
        }
        fileWriterMap.clear()
      }
    }
  }

  def writeFile = {ol, ddfw ->
    ol.each { arow ->
      ddfw.append("${arow}\n")
    }
  }
  
  def String writeItems(def csvList, def exportTarget, def typeFolder, def fileName) {
    def ddf = MrUtils.mkdir(typeFolder, "${exportTarget}")
    def fqfn = MrUtils.fqfn(ddf,fileName)
    return writeItems(csvList, fqfn)
  }

  def String writeItems(def csvList, def fqfn) {
    File iddf = new File(fqfn)
    def all = []
    all << csvList["hdr"]
    csvList["lines"].each{ row ->
      def cvals = []
      row.keySet().each { colName ->
        cvals << row.get(colName)
      }
      all << cvals.join(",")
    }
    if (all.size()>1) {
      FileWriter iddfw = new FileWriter(iddf, false)
      writeFile(all, iddfw)
      iddfw.close()
    }
    int nExported = all.size()?all.size()-1:0
    "${nExported}\t\t items exported to file '${iddf.getName()}'"
  }

  def createFoldersByType(def baseFolder) {
    def rc = [:]
    try {
      rc = makeTypeFolders(baseFolder)
    } catch (Exception e) {
      println e.toString()
      log.error "createTypeFolders: ${e.message}"
    }
    return rc
  }

  Map makeTypeFolders(def branch) {
    def rc = [:]
    def drive = config.rdrive
    def dir = MrUtils.mkBranch(drive, config.dataProductDataFolder)
    dir = MrUtils.mkBranch(dir, branch)
    config.pavementTypes.keySet().each { type ->
      rc.put(type, MrUtils.mkdir(dir,config.pavementTypes.get(type)))
    }
  return rc
  }

  def queryToCsvList(String q, List arglist) {
    def dm = [:]
    def sql = Sql.newInstance(dataSource)
    def typeMap = [:]
    def rows = []
    def onRow = {
       rows << it.toRowResult()
       }
    def onFirstRow = {meta ->
       (1..meta.columnCount).each {
         typeMap.put(meta.getColumnLabel(it),meta.getColumnTypeName(it))
       }
    }
    sql.eachRow(q,arglist,onFirstRow,onRow)
    def colNames = typeMap.keySet().toList()
    def lines = []
    rows.each { row ->
       def vals = [:]
       row.keySet().eachWithIndex { key, i ->
         vals.put(key,MrUtils.formatSqlValueForCsv(row[key], typeMap[key]))
       }
      lines << vals
    }
    dm.put("hdr",colNames.join(","))
    dm.put("lines",lines)
    return dm
  }

  def queryToCsvFile(String q, List arglist, String fqfn) {
    File iddf = new File(fqfn)
    if (iddf.exists()) {
      log.info "${fqfn} exists.  "
      return
    }
    FileWriter iddfw = new FileWriter(iddf, false)
    BufferedWriter bw = new BufferedWriter(iddfw)
    def sql = Sql.newInstance(dataSource)
    def rowCnt = 0
    def typeMap = [:]
    def onRow = {
      def vals = []
      it.toRowResult().keySet().eachWithIndex { key, i ->
        vals  << MrUtils.formatSqlValueForCsv(it.toRowResult()[key], typeMap[key])
      }
      bw.writeLine(vals.join(",").toString())
      rowCnt++
      if ((rowCnt%100) == 0) {
        bw.flush()
      }
    }
    def onFirstRow = {meta ->
      def colNames = []
      (1..meta.columnCount).each {
        colNames << meta.getColumnLabel(it)
        typeMap.put(meta.getColumnLabel(it),meta.getColumnTypeName(it))
      }
      bw.writeLine(colNames.join(",").toString())
    }
    sql.eachRow(q,arglist,onFirstRow,onRow)
      bw.flush()
    if (rowCnt) {
      bw.close()
    }
    else {
      bw.close()
      iddf.delete()
    }
    return rowCnt
  }

  Map designNumMap () {
    def rc = [:]
    def q = "SELECT ID, CASE WHEN LEAD(ID,1,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 1 WHEN LEAD(ID,2,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 2 WHEN LEAD(ID,3,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 3 WHEN LEAD(ID,4,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 4 WHEN LEAD(ID,5,NULL) OVER (PARTITION BY CELL_NUMBER ORDER BY C.CONSTRUCTION_ENDED_DATE DESC) IS NULL THEN 5 ELSE 1 END DESIGN_NUMBER FROM MNR.CELL C ORDER BY CELL_NUMBER,CONSTRUCTION_ENDED_DATE" 
    Sql sql = new groovy.sql.Sql(dataSource)
    sql.eachRow(q) { row ->
      rc.put(row.ID,row.DESIGN_NUMBER)
    }
    return rc
  }

}

package us.mn.state.dot.mnroad
import groovy.sql.Sql
import java.sql.SQLException
import org.apache.log4j.Logger

class ExportDataService {

    static Logger log = Logger.getLogger(ExportDataService.class)
    static transactional = true
    def dataSource

    def dpLayers(def config) {
      if ((config.size()>1) && !config.rdrive) {
        log.error "Required parameter 'rdrive' is null" //.grom()
        return "Layers"
      }
      def jobSummary = []
      if (
        config.pavementTypes
        && config.ddByType
        ) {
        def dpFolder = "${config.rdrive}\\${config.dataProductDataFolder}"
        File dpf = new File(dpFolder)
        if (!dpf.exists()) {
          return "Abend - Cannot find Data Product Folder: ${dpFolder}."
        }
        def designDetailsByTypeFolders = createFoldersByType(dpFolder, config.ddByType, config.pavementTypes)
        if (designDetailsByTypeFolders)    {
          if (!designDetailsByTypeFolders.get("exception"))  {
            config.pavementTypes.keySet().each { shortName ->
              def exportTarget = "Layer"
              def status = ""
              try {
                status = exportLayers(config, exportTarget, designDetailsByTypeFolders.get(shortName), shortName)
                log.info "${shortName} exports completed normally to folder ${designDetailsByTypeFolders.get(shortName)}."
              } catch (Exception e) {
                status = "${shortName} ${exportTarget}  Export failed: ${e}"
                log.error status
              }
            jobSummary << status
            }
          } else {
            def msg = "${designDetailsByTypeFolders.get("exception")}"
            log.error msg
            return msg
          }
        }
      } else {
      def mc = []
      if (!config.keySet().contains("pavementTypes"))
        mc << "pavementTypes"
      if (!config.keySet().contains("ddByType"))
        mc << "ddByType"
      jobSummary << "Missing configuration: ${mc}"
      }
      return "Export Layers ${jobSummary?jobSummary.join(", "):"- no job summary returned."}"
    }

    def String exportLayers (def config, def target, def typeFolder, def cellType) {
      def status = "${cellType}Query not found"
      def fn = "All ${cellType}s.csv"
      def q = config["${cellType}Query"]
      if (q) {
        def aq = "${q} WHERE SUBSTR(C.CLASS,24)=? ORDER BY CELL,FROM_DATE".toString()
        def argList = [cellType]
        if (MrUtils.yesno(config.overwriteLayersFiles)) {
          log.info  "Layer export -> Args: ${argList}; Query: ${aq}"
          MrUtils.writeQuery(aq,argList,"${typeFolder}\\layerSql${argList}.txt", false)
          def csvList = queryToCsvList(aq, argList)
          status = writeItems(csvList, "${target} Details", typeFolder, fn)
          Sql sql = new groovy.sql.Sql(dataSource)
          def ra = MrUtils.cellAttrs(sql, cellType, config.throughYear)
          def nCells = 0
          ra.each { cellidrow ->
            def lq = "${q} WHERE SUBSTR(C.CLASS,24)=? AND CELL_ID=? ORDER BY CELL,FROM_DATE".toString()
            def sargList = [cellType, new BigDecimal(cellidrow.ID)]
            log.info  "Layer export -> Args: ${sargList}; Query: ${lq}"
            MrUtils.writeQuery(lq,sargList,"${typeFolder}\\layerSql${sargList}.txt", true)
            def l = queryToCsvList(lq, sargList)
            def fileName = "${cellType} ${cellidrow.CELL} Design ${cellidrow.DESIGN}.csv"
            status = writeItems(l, "${target} Details", typeFolder, fileName)
            nCells++
          }
          status = "${nCells} ${cellType} cells written."
        } else {
          status = "${cellType} layer export suppressed."
          log.info status
        }
      } else {
        log.info status
      }
      return status
    }

    def dpSensors(def config) {
      if ((config.size()>1) && !config.rdrive) {
        log.error "Required parameter 'rdrive' is null" //.grom()
        return "Sensors"
      }
      def jobSummary = []
      def sensorByType = "${config.sensorInfo}\\${config.sensorByType}"
      if (
        config.pavementTypes
        && sensorByType
        ) {
          def dpFolder = "${config.rdrive}\\${config.dataProductDataFolder}"
          File dpf = new File(dpFolder)
          if (!dpf.exists()) {
            return "Cannot find Data Product Folder: '${dpFolder}'."
          }
          def sensorDetailsByTypeFolders = createFoldersByType(dpFolder, sensorByType, config.pavementTypes)
          if (sensorDetailsByTypeFolders)    {
            config.pavementTypes.keySet().each { shortName ->
              def exportTarget = "Sensor"
              def status = ""
              try {
                status = exportSensors(config, exportTarget, sensorDetailsByTypeFolders.get(shortName), shortName)
                log.info "${shortName} exports completed normally to folder ${sensorDetailsByTypeFolders.get(shortName)}."
              } catch (Exception e) {
                status = "${shortName} ${exportTarget}  Export failed: ${e}"
                log.error status
              }
              jobSummary << status
            }
          }
        } else {
        def mc = []
        if (!config.keySet().contains("pavementTypes"))
          mc << "pavementTypes"
        if (!config.keySet().contains("sensorByType"))
          mc << "sensorByType"
        jobSummary << "Missing configuration: ${mc}"
        }

        def byTypeFiles = []
        String start = "${config.rdrive}\\${config.dataProductDataFolder}".toString()
        new File(start).eachFileRecurse {
          if (it.toString().endsWith("All Sensors.csv")) {
            byTypeFiles << it.toString()
          }
        }
      def si = "${start}${config.sensorInfo}/All Sensors.csv"
        PrintWriter pw = new PrintWriter(new FileOutputStream(si));
        int nrows = 0;
        byTypeFiles.each { name ->
          int i = 0
          BufferedReader br = new BufferedReader(new FileReader(name));
          String line = br.readLine();
          while (line != null) {
            if (!nrows || i)
              pw.println(line);
            line = br.readLine();
            i++
            nrows++
          }
          br.close();
        }
        pw.close();

        return "Export Sensors ${jobSummary?jobSummary.join(", "):"- no job summary returned."}"
      }

    def String exportSensors (def config, def target, def typeFolder, def cellType) {
      def status = "sensorQuery not found"
      def fn = "${cellType} All Sensors.csv"
      def q = config["sensorQuery"]
      if (q) {
        def aq = "${q} WHERE SUBSTR(C.CLASS,24)=? ORDER BY CELL,MODEL,SEQ".toString()
        def argList = [cellType]
        if (MrUtils.yesno(config.overwriteSensorFiles)) {
          log.info  "Sensor export -> Args: ${argList}; Query: ${aq}"
          def csvList = queryToCsvList(aq, argList)
          status = writeItems(csvList, "${typeFolder}\\${fn}")
          Sql sql = new groovy.sql.Sql(dataSource)
          def ra = MrUtils.cellAttrs(sql, cellType, config.throughYear)
          def nCells = 0
          ra.each { cellidrow ->
            def lq = "${q} WHERE SUBSTR(C.CLASS,24)=? AND CELL_ID=? ORDER BY CELL,MODEL,SEQ".toString()
            def sargList = [cellType, new BigDecimal(cellidrow.ID)]
            log.info  "Sensor export -> Args: ${sargList}; Query: ${lq}"
            def l = queryToCsvList(lq, sargList)
            def fileName = "${cellType} ${cellidrow.CELL} Design ${cellidrow.DESIGN} Sensors.csv"
            status = writeItems(l, "${target} Details", typeFolder, fileName)
            nCells++
          }
          status = "${nCells} ${cellType} cells written."
        } else {
          status = "${cellType} sensor export suppressed."
          log.info status
        }
      } else {
        log.info status
      }
    return status
    }

    def dpDistress(def config) {
      def dpFolder = "${config.rdrive}\\${config.dataProductDataFolder}"
      File dpf = new File(dpFolder)
      if (!dpf.exists()) {
        return "Cannot find Data Product Folder: ${dpFolder}."
      }
      def measuredDataFolders = createFoldersByType(dpFolder, config.measuredDataFolder, config.pavementTypes)
      def jobSummary = ""
      if (measuredDataFolders)    {
        jobSummary = exportDistress(config, measuredDataFolders)
      }
      return "Export Distress ${jobSummary?:"- no job summary returned."}"
    }

    def exportDistress(def config, def typeFolders) {
      Sql sql = new groovy.sql.Sql(dataSource)
      Map columnTypeMap = [:]
      Map scaleMap = [:]
      Map dataMap = [:]
      def jobSummary = []

      def writeRows={ row ->
        def rr = row.toRowResult()
        def r = ""
        def a = []
        def nl = rr.keySet().toList()
        rr.eachWithIndex { it,i ->
          if (nl[i]!="ID") {
            Object o = rr[i]
            def typ = columnTypeMap.get(i+1)
            def scale = scaleMap.get(i+1)
            a << us.mn.state.dot.mnroad.SqlDataFormat.formatSqlValue(o, typ, scale, false)
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
            scaleMap.put(it, meta.getScale(it))
            }
          }
        def fileHdr = "${cn.keySet().collect {cn.get(it)}.join(",")}\n"
        dataMap["hdr"] = fileHdr
        dataMap["rows"] = []
        dataMap["nrows"] = 0
       }

      dataMap.put("rowsWritten", [:])
      dataMap.put("nrows", 0)
      dataMap.put("hdr", "")
      dataMap.put("rows", [])

      def totalRows = 0
      def typeSummary = []
      if (MrUtils.yesno(config.overwriteDistressFiles)) {
        typeFolders.keySet().each { cellType ->
          def dir = MrUtils.mkdir(typeFolders.get(cellType),config.physMeasureFolder)
          def q = ""
          typeSummary.clear()
          try {
            def ra = MrUtils.cellAttrs(sql, cellType, config.throughYear)
            ra.each { row ->
              config.distressTableFolderMap.keySet().each { data_table ->
                def columnList = "t.*"
                def columnAliasMap = config["columnAliasMap"]
                if (columnAliasMap && columnAliasMap["${data_table}"] ) {
                  columnList = columnAliasMap["${data_table}"]
                }
                q = "SELECT ${columnList},TO_NUMBER(${row.DESIGN}) DESIGN from MNR.${data_table} t where cell=? and day between ? and ? order by cell,day"
                def argList = [row.CELL, new java.sql.Date(row.FROM_DATE.time), new java.sql.Date(row.TO_DATE.time)]
                log.info  "Distress Export -> Args: ${argList}; Query: ${q}"
                MrUtils.writeQuery(q,argList,"${dir}\\distressSql${argList}.txt", true)
                sql.eachRow(q.toString(),argList,writeHeader,writeRows)
                if (dataMap["nrows"]) {
                  def fn = config.distressFileNameMap[data_table]
                  def distressFileName = "${data_table.toString().substring(9)}.csv".toLowerCase()
                  if (fn)
                    distressFileName = "${fn}.csv".toLowerCase()
                  def subdir = config.distressTableFolderMap[data_table]
                  def distressFolder = MrUtils.mkBranch(dir,subdir)
                  def fqfn = "${distressFolder}\\${distressFileName}"
                  def f = new File(fqfn)
                  def firstRows = dataMap["rowsWritten"].get(fqfn)
                  FileWriter fw = new FileWriter(f, !firstRows?false:true)
                  if (!firstRows) {
                    fw.append dataMap["hdr"]
                  }
                  dataMap.rows.each { arow ->
                    fw.append("${arow}\n")
                  }
                  fw.flush()
                  fw.close()
                  if (!firstRows) {
                    dataMap["rowsWritten"].put(fqfn, dataMap["rows"].size())
                  }
                  else {
                    int sz = firstRows + dataMap["rows"].size()
                    dataMap["rowsWritten"].remove(fqfn)
                    dataMap["rowsWritten"].put(fqfn, sz)
                  }
                  log.info "${cellType}: ${data_table} rows from cell ${row.CELL}: ${dataMap["rows"].size()} to ${distressFileName}"
                  totalRows += dataMap["rowsWritten"].get(fqfn)
                }
              }
            }
          } catch (Exception e) {
            log.error "${e.message} retrieving ${q}."
            e.printStackTrace()
          }
        if (typeSummary.size()>1) {
          def ts = typeSummary.join(", ")
          jobSummary << ts
        }
        jobSummary << "Total number of ${cellType} distress rows: ${totalRows} "
      }
    } else {
        jobSummary << "Overwrite files is false."
      }
    jobSummary.join(", ")
    }

  def dpDcp(def config) {
    def dpFolder = "${config.rdrive}\\${config.dataProductDataFolder}"
    File dpf = new File(dpFolder)
    if (!dpf.exists()) {
      return "Cannot find Data Product Folder: ${dpFolder}."
    }
    def measuredDataFolders = createFoldersByType(dpFolder, config.measuredDataFolder, config.pavementTypes)
    def jobSummary = ""
    if (measuredDataFolders)    {
      jobSummary = exportDcp(config, measuredDataFolders)
    }
    def rc = "Export Dcp ${jobSummary?:"- no job summary returned."}"
    println "Dcp Done - ${rc}"
    return rc
  }

  def exportDcp(def config, def typeFolders) {
    Sql sql = new groovy.sql.Sql(dataSource)
    Map columnTypeMap = [:]
    Map scaleMap = [:]
    Map dataMap = [:]
    def jobSummary = []

    def writeRows={ row ->
      def rr = row.toRowResult()
      def r = ""
      def a = []
      def nl = rr.keySet().toList()
      rr.eachWithIndex { it,i ->
        if (nl[i]!="ID") {
          Object o = rr[i]
          def typ = columnTypeMap.get(i+1)
          def scale = scaleMap.get(i+1)
          a << us.mn.state.dot.mnroad.SqlDataFormat.formatSqlValue(o, typ, scale, false)
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
          scaleMap.put(it, meta.getScale(it))
          }
        }
      def fileHdr = "${cn.keySet().collect {cn.get(it)}.join(",")}\n"
      dataMap["hdr"] = fileHdr
      dataMap["rows"] = []
      dataMap["nrows"] = 0
     }

    def queries = []
    def dcpQuery = [:]
    dcpQuery.put("name","${config["dcpQuery"]}")
    dcpQuery.put("folder","${config["dcp_folder"]}")
    dcpQuery.put("fileName","Dcp.csv")
    queries << dcpQuery
    def ubmQuery = [:]
    ubmQuery.put("name","${config["unbound_field_moistureQuery"]}")
    ubmQuery.put("folder","${config["moisture_content_folder"]}")
    ubmQuery.put("fileName","Unbound field moisture.csv")
    queries << ubmQuery
    def overwriteDcp = MrUtils.yesno(config.overwriteDcpFiles)
    def typeSummary = []
    typeFolders.keySet().each { cellType ->
      def sqlDateTo = MrUtils.endOfYearSqlDate(config.throughYear)
      typeSummary.clear()
      typeSummary << "${cellType}:"
      queries.each { aquery ->
        dataMap.clear()
        dataMap.put("nrows", 0)
        dataMap.put("hdr", "")
        dataMap.put("rows", [])
        def q = ""
        try {
          q = aquery.get("name")
          def argList = [cellType,sqlDateTo]
          log.info  "DCP Export -> Args: ${argList}; Query: ${q}"
          MrUtils.writeQuery(q,argList,"${typeFolders.get(cellType)}\\dcpSql${argList}.txt", true)
          sql.eachRow(q.toString(),argList,writeHeader,writeRows)
          if (dataMap["nrows"]) {
            def dir = MrUtils.mkdir(typeFolders.get(cellType),config.physMeasureFolder)
            def folder = aquery.get("folder")
            def dcpFolder = MrUtils.mkBranch(dir,folder)
            def dcpFileName = aquery.get("fileName").toLowerCase()
            def fqfn = "${dcpFolder}\\${dcpFileName}"
            def f = new File(fqfn)
            if (dataMap.rows.size() && (!f.exists() || (overwriteDcp))) {
              FileWriter fw = new FileWriter(f, false)
              fw.append dataMap["hdr"]
              dataMap.rows.each { arow ->
                fw.append("${arow}\n")
              }
              fw.flush()
              fw.close()
            }
            def info = "${cellType}/${dcpFileName}: ${dataMap["nrows"]}"
            log.info info
            typeSummary << info

          }
      } catch (Exception e) {
        log.error "${e.message} retrieving ${q}."
      }

      if (typeSummary.size()>1)
        jobSummary << typeSummary.join(", ")
      }
    }
    jobSummary.join(", ")
  }

  def dpFwd(def config) {
    def jobSummary = []
    def dpFolder = "${config.rdrive}\\${config.dataProductDataFolder}"
    File dpf = new File(dpFolder)
    if (!dpf.exists()) {
      return "Cannot find Data Product Folder: ${dpFolder}."
    }
    def measuredDataFolders = createFoldersByType(dpFolder, config.measuredDataFolder, config.pavementTypes)
    config.pavementTypes.keySet().each { cellType ->
      def exportTarget = "FWD"
      try {
        jobSummary << exportFwd(config, exportTarget, measuredDataFolders.get(cellType), cellType)
        log.info "${cellType} exports completed normally to folder ${measuredDataFolders.get(cellType)}."
      } catch (Exception e) {
        log.error "${cellType} ${exportTarget}  Fwd Export failed: ${e}"
        e.printStackTrace()
      }
    }
    return "Export Fwd ${jobSummary?jobSummary.join(", "):"- no job summary returned."}"
  }

  def String exportFwd(def config, def target,def typeFolder, def cellType) {
    def status = "fwdQuery not found"
    Sql sql = new groovy.sql.Sql(dataSource)
    def nrows = 0
    def overwriteFiles = MrUtils.yesno(config.overwriteFwdFiles)
    status = "cells not found"
    def nCells = 0
    // cellAttrs must return CELL, FROM_DATE, TO_DATE, and DESIGN
    def ra = MrUtils.cellAttrs(sql, cellType, config.throughYear)
    ra.each { cellidrow ->
      def fileName = "${cellType} ${cellidrow.CELL} Design ${cellidrow.DESIGN} Fwd Drops.csv".toString()
      def ddf = MrUtils.mkBranch(typeFolder, config.loadFolders)
      def fqfn = MrUtils.fqfn(ddf,fileName)
      def fileExists = (new File(fqfn)).exists()
      if (fileExists && !overwriteFiles) {
        println "File ${fileName} not overwritten."
        return
      }
      def argList = [cellType, new BigDecimal(cellidrow.CELL), new java.sql.Date(cellidrow.FROM_DATE.time), new java.sql.Date(cellidrow.TO_DATE.time)]
      def rowsExported = 0
      def q = "${config.fwdQuery} ${config.queryCriteria} ${config.queryOrder}"
      log.info  "FWD Export -> Args: ${argList}; Query: ${q}"
      MrUtils.writeQuery(q,argList,"${ddf}\\fwdSql${argList}.txt", true)
      rowsExported += batch("${config.countQuery} ${config.queryCriteria}"
              ,q
              , argList, fqfn, config.batchSize)
      status = "${nrows} rows -> ${fileName}"
      log.info "${rowsExported} rows exported to ${fileName}."
      nCells++
      }
    status = "${nCells} ${cellType} cells written."
    return status
  }

  def dpMaterials(def config) {
    if ((config.size()>1) && !config.rdrive) {
      log.error "Required parameter 'rdrive' is null" //.grom()
      return "Layers"
    }
    def jobSummary = []
    def materialsFolder = MrUtils.mkBranch("${config.rdrive}\\${config.dataProductDataFolder}",config.materialSamplesFolder)
    if (materialsFolder && (new File(materialsFolder)).exists())    {
      def status = ""
      status = exportMatSamples(config, materialsFolder)
      jobSummary << status
      if (
        config.asphalt // Asphalt
        || config.concrete // Concrete
        || config.aggregateSoil // Aggregate & Soil
        ) {
        def exportTarget = "Materials"
        status = ""
        if (config["asphalt"]) {
          status = exportMaterialTests(config, materialsFolder, "asphalt")
          jobSummary << status
          println status
        }
        if (config.concrete) {
          status = exportMaterialTests(config, materialsFolder, "concrete")
          jobSummary << status
          println status
        }
        if (config.aggregateSoil) {
          status = exportMaterialTests(config, materialsFolder, "aggregateSoil")
          jobSummary << status
          println status
        }
        jobSummary << "End ${exportTarget} exports."
        }
      } else {
      jobSummary << "Data Product folder, '${config.rdrive}\\${config.dataProductDataFolder}', missing."
    }
    return "Export Materials ${jobSummary?jobSummary.join(", "):"- no job summary returned."}"
  }

  def exportMatSamples(def config, def dir) {
    def status = ""
    def fqfn = "${dir}\\${config.fileName}"
    def nrows = 0
    if (!MrUtils.yesno(config.overwriteMatSamplesFiles)) {
      return "${nrows} written because overwriteMatSamplesFiles: ${config.overwriteMatSamplesFiles}."
    }
    def tableName = config.tableName
    def dq = "${config.selectPhrase} FROM ${tableName} ${config.querySuffix?:""} ${config.wherePhrase?:""}"

    log.info  "MatSamples Export -> Args: none Query: ${dq}"
    MrUtils.writeQuery(dq,[],"${dir}\\matSamplesSql.txt", true)
    nrows = batch("${config.countQuery}", dq, [], fqfn, config.batchSize)
    status = "${nrows} rows -> ${config.fileName}"

    return status
  }

//  @Typed(TypePolicy.MIXED)
  def exportMaterialTests(def config, def mf, def type) {
    def status = ""
    def rowsOfType = 0
    def dir = MrUtils.mkBranch(mf,config["${type}Folder"])
    config[type].keySet().each { tableName ->
      // names of columns to be extracted are stored in a map of lists
      //  with the table name (lower case) as the key
      def tbl = tableName.trim()//.toLowerCase()
      def fileName = "${config.materialTestsFileMap.get(tbl)}.csv"
      def fqfn = "${dir}\\${fileName}"
      def nrows = 0
      if (!MrUtils.yesno(config.overwriteMatSamplesFiles)) {
        return "${nrows} written because overwriteMatSamplesFiles: ${config.overwriteMatSamplesFiles}."
      }
      def wherePhrase = "${config.wherePhrase}"
      def adwp = config[type].get(tbl)
      if (adwp && adwp != "\"\"")
        wherePhrase = adwp
      def columnsKey = tbl.startsWith("MNR.")?tbl.substring(4):tbl
      def columnList = config[columnsKey]
      if (!columnList) {
        status = "Columns for table ${tableName} were not found in the supplied config map."
        log.error status
      } else
//      if (columnsKey == "mat_soil_tests")
      {
        def selectPhrase = config.mat_samples_columns.collect(){it}.join(',') + ", " + columnList.collect(){'MST.'+it}.join(',')
        def fromPhrase = "MNR.MAT_SAMPLES MS JOIN ${tableName} MST ON MST.MNROAD_ID=MS.MNROAD_ID"
        def orderbyPhrase = "${config.testQueryOrder}"
        def countQuery = "${config.countQuery} MS JOIN ${tbl} MST ON MST.MNROAD_ID=MS.MNROAD_ID ${wherePhrase}"
        def selectQuery  = "SELECT ${selectPhrase} FROM ${fromPhrase} ${wherePhrase} ORDER BY ${orderbyPhrase}"
        log.info  "MatSample Tests Export -> Args: none Query: ${selectQuery}"
        MrUtils.writeQuery(selectQuery,[],"${dir}\\matTestsSql.txt", true)                
        nrows = batch(countQuery, selectQuery, [], fqfn, config.batchSize)
        rowsOfType += nrows
      }
    }
    return "${type} -> ${rowsOfType} rows."
  }

    def dpWeather(def config) {
      def jobSummary = []
      def dpFolder = "${config.rdrive}\\${config.dataProductDataFolder}"
      File dpf = new File(dpFolder)
      if (!dpf.exists()) {
        return "Cannot find Data Product Folder: ${dpFolder}."
      }
      def measuredDataFolders = createFoldersByType(dpFolder, config.measuredDataFolder, config.pavementTypes)
      def exportTarget = "Weather"
      try {
        //jobSummary << exportWeather(config, exportTarget, measuredDataFolders.get(cellType), cellType)
        log.info "${cellType} exports completed normally to folder ${measuredDataFolders.get(cellType)}."
      } catch (Exception e) {
        log.error "${cellType} ${exportTarget}  Fwd Export failed: ${e}"
        e.printStackTrace()
      }
      return "Export Weather ${jobSummary?jobSummary.join(", "):"- no job summary returned."}"
    }

    def String exportWeather(def config, def target,def typeFolder, def cellType) {
      def status = "weatherQuery not found"
      Sql sql = new groovy.sql.Sql(dataSource)
      def nrows = 0
      def overwriteFiles = MrUtils.yesno(config.overwriteFwdFiles)
      status = "cells not found"
      def nCells = 0
      // cellAttrs must return CELL, FROM_DATE, TO_DATE, and DESIGN
      def ra = MrUtils.cellAttrs(sql, cellType, config.throughYear)
      ra.each { cellidrow ->
        def fileName = "${cellType} ${cellidrow.CELL} Design ${cellidrow.DESIGN} Fwd Drops.csv".toString()
        def ddf = MrUtils.mkBranch(typeFolder, config.loadFolders)
        def fqfn = MrUtils.fqfn(ddf,fileName)
        def fileExists = (new File(fqfn)).exists()
        if (fileExists && !overwriteFiles) {
          println "File ${fileName} not overwritten."
          return
        }
        def argList = [cellType, new BigDecimal(cellidrow.CELL), new java.sql.Date(cellidrow.FROM_DATE.time), new java.sql.Date(cellidrow.TO_DATE.time)]
        def rowsExported = 0
        def q = "${config.weatherQuery}"
        log.info  "FWD Export -> Args: ${argList}; Query: ${q}"
        MrUtils.writeQuery(q,argList,"${ddf}\\fwdSql${argList}.txt", true)
        rowsExported += batch("${config.countQuery} ${config.queryCriteria}"
                ,q
                , argList, fqfn, config.batchSize)
        status = "${nrows} rows -> ${fileName}"
        log.info "${rowsExported} rows exported to ${fileName}."
        nCells++
        }
      status = "${nCells} ${cellType} cells written."
      return status
    }

    long rowCount(def q, def arglist, Sql sql) {
      long rc = 0L
      try {
        def row = sql.firstRow(q,arglist)
        if (row?.ROW_COUNT) {
          rc = row.ROW_COUNT
        }
      } catch (SQLException sqle) {
        def msg = q?"Qry: ${q}\n":""
        println "Unable to get row count - ${msg}Msg: ${sqle.getMessage()}"
      }
      return rc
    }

    int batch (countQuery, dataQuery, arglist, fqfn, argBatchsize) {
      def rowcnt = 0
      int numrows = 0
      def iddf = new File(fqfn)
      FileWriter iddfw = new FileWriter(iddf, false)
      BufferedWriter bw = new BufferedWriter(iddfw)

      try {
        Sql sql = new groovy.sql.Sql(dataSource)
        if (countQuery) {
          rowcnt = rowCount(countQuery,arglist,sql)
          if (!rowcnt)
            return rowcnt
        }
        Map columnTypeMap = [:]
        Map scaleMap = [:]

        def writeRow={ row ->
          def rr = row.toRowResult()
          def a = []
          def nl = rr.keySet().toList()
          rr.eachWithIndex { it,i ->
            if (nl[i]!="RNUM") {
              Object o = rr[i]
              def typ = columnTypeMap.get(i+1)
              def scale = scaleMap.get(i+1)
              a << SqlDataFormat.formatSqlValue(o, typ, scale, false)
            }
          }
          bw.writeLine "${a.join(',')}"
          numrows++
        }

        def writeHeader = { meta ->
          def cn = [:]
          columnTypeMap.clear()
          (1..meta.columnCount).each {
            def colName = meta.getColumnLabel(it)
            if (colName != "RNUM") {
              cn.put(it,colName)
              columnTypeMap.put(it,meta.getColumnTypeName(it))
              scaleMap.put(it, meta.getScale(it))
              }
            }
          bw.writeLine "${cn.keySet().collect {cn.get(it)}.join(",")}"
         }

        def defaultBatchsize = 1000
        if (argBatchsize) {
          if (argBatchsize instanceof String) {
            try {
               defaultBatchsize = Integer.parseInt(argBatchsize)
            } catch (NumberFormatException nfe) {
              println "Ignoring batch size '${argBatchsize}'. Using 1000 instead."
            }
          } else {
            defaultBatchsize = argBatchsize
          }
        }

        def batchSize=defaultBatchsize
        int numBatches = ((int)(rowcnt/batchSize)) + 1
        int batchNum = 0
        int fromOffset = 0
        print "Writing ${rowcnt} rows to ${fqfn} ... "
        while (batchNum < numBatches)  {
          def sqs = "SELECT * FROM ( SELECT C.*, ROWNUM RNUM FROM (${dataQuery} ) C WHERE ROWNUM <= ${fromOffset+batchSize} ) where rnum  >= ${fromOffset+1}"
          if (batchNum == 0) {
            sql.eachRow(sqs.toString(),arglist,writeHeader,writeRow)
          }
          else {
            sql.eachRow(sqs.toString(), arglist) { writeRow(it) }
          }
          bw.flush()
          fromOffset += batchSize
          batchNum++
          if (batchNum && batchNum%5 == 0 ) {//&& batchNum <= numBatches) {
            println  "Processing batch ${batchNum} of ${numBatches}."
          }
        }
        println "Done"
      } catch (Exception e) {
        println "Batch exception writing rows:${e.message}."
      } finally {
        if (bw) {
          bw.flush()
          bw.close()
          if (!rowcnt && iddf) {
            iddf.delete()
          }
        }
      }

    return numrows
    }

/********************************************************************/

    def createFoldersByType(def dpFolder, def branchName, def pavementTypes) {
      def rc = [:]
      try {
        def dir = MrUtils.mkBranch(dpFolder, branchName)
        pavementTypes.keySet().each { type ->
          rc.put(type, MrUtils.mkdir(dir,pavementTypes.get(type)))
        }
      } catch (Exception e) {
        println e.toString()
        log.error "createTypeFolders: ${e.message}"
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

}

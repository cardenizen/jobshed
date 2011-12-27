import org.apache.commons.dbcp.BasicDataSource
import com.leebutts.SwitchableDataSource
import com.leebutts.Environment
import org.springframework.jdbc.datasource.DriverManagerDataSource

beans = {
/*
  dataSourceTest(BasicDataSource) {
      driverClassName = "oracle.jdbc.driver.OracleDriver"
      url = "jdbc:oracle:thin:@MRL2K3dev.ad.dot.state.mn.us:1521:DEV11"
      username = "mnru"
      password = "dev11mnr"
  }
  dataSourceProduction(BasicDataSource) {
      driverClassName = "oracle.jdbc.driver.OracleDriver"
          url = "jdbc:oracle:thin:@MRL2K3MRDB.ad.dot.state.mn.us:1521:mnrd"
      username = "mnru"
      password = "mnru"
  }

    parentDataSource(DriverManagerDataSource) {
        bean -> bean.'abstract' = true;
        driverClassName = 'oracle.jdbc.OracleDriver'
        username = "mnr"
    }

    Environment.list().each { env ->
        "${env.prefix}DataSource"(DriverManagerDataSource) { bean ->
            bean.parent = parentDataSource
            bean.scope = "prototype"

            driverClassName = env.driver

            if (env.dbType?.equalsIgnoreCase("HSQL")) {
                url = env.protocol + env.instance
            }
            if (env.dbType?.equalsIgnoreCase("Oracle")) {
                url = env.protocol + env.username + "/" + env.password + "@" + env.host + ":" + env.port + ":" + env.instance
            }
            if (env.user) {
                username = env.username
            }
            if (env.password) {
                password = env.password
            }
        }
    }

    def dataSources = [:]
    Environment.list().each {env ->
        dataSources[env.id] = ref(env.prefix + 'DataSource')
    }

    dataSource(SwitchableDataSource) {
        targetDataSources = dataSources
    }
*/    
}

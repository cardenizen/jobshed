//dataSource {
//    pooled = true
//    driverClassName = "org.hsqldb.jdbcDriver"
//    username = "sa"
//    password = ""
//}
dataSource {
    pooled = true
    driverClassName = "oracle.jdbc.OracleDriver"
    dialect="org.hibernate.dialect.Oracle10gDialect"
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
//    cache.provider_class='com.opensymphony.oscache.hibernate.OSCacheProvider'
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
    default_schema = 'MNR'
}

// environment specific settings
environments {
    development {
        dataSource {
//          url="jdbc:oracle:thin:@MRL2K3dev.ad.dot.state.mn.us:1521:DEV11"
//          username="mnr"
//          password="dev11mnr"
//          url = "jdbc:oracle:thin:@localhost:1521:XE"
//          username = "mnr"
//          password = "dev11mnr"
          url="jdbc:oracle:thin:@MRL2K3MRDB.ad.dot.state.mn.us:1521:mnrd"
          username="mnru"
          password="mnru"
        }
    }
    test {
        dataSource {
          url = "jdbc:oracle:thin:@localhost:1521:XE"
          username = "mnr"
          password = "dev11mnr"
        }
    }
    production {
        dataSource {
//            url = "jdbc:hsqldb:file:prodDb;shutdown=true"
          url="jdbc:oracle:thin:@MRL2K3MRDB.ad.dot.state.mn.us:1521:mnrd"
          username="mnru"
          password="mnru"
        }
    }
}

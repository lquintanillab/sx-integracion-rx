package sx

import groovy.sql.Sql
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert

class ReplicaService {

    def dataSourceLocatorService

    def importar(entityName){
      def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

      servers.each{ server ->
        importar(entityName,server.server)
      }
    }

    def importar(entityName,serverName){

       // println "***  Importando de Por ReplicaService: ****  "+serverName +"  "+entityName


        def query="Select * from audit_log where name= ${entityName} and date_replicated is null "

        def server=DataSourceReplica.findAllByActivaAndCentralAndServer(true,false,serverName)

        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

        def datasourceCentral=dataSourceLocatorService.dataSourceLocator(central.server)

        def centralSql=new Sql(datasourceCentral)



             //   println "***  Importando de Por ReplicaService: ${server.server} ******* ${server.url}****  "

            def datasourceOrigen=dataSourceLocatorService.dataSourceLocator(server.server)


            def sql=new Sql(datasourceOrigen)


            sql.rows(query).each{audit ->

                def config= EntityConfiguration.findByName(audit.name)

               // println "Importando       " +audit.name+"  ---------  "+audit.persisted_object_id+"   "+config

                if(config){

                    def origenSql="select * from $config.tableName where $config.pk=?"


                    def row=sql.firstRow(origenSql, [audit.persisted_object_id])
                    if(audit.event_name=='DELETE' || row){
                        try {
                            switch (audit.event_name) {

                                case 'INSERT':
                                //    println 'Insertando '+row
                                    SimpleJdbcInsert insert=new SimpleJdbcInsert(datasourceCentral).withTableName(config.tableName)

                                    def res=insert.execute(row)

                                    if(res){
                                //        println '*************** Registros importados: '+res
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                                    }else{
                                 //       println '***************  No se encontraron registros para insertar'
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR",audit.id])
                                    }

                                    break
                                case 'UPDATE':

                                //    println "****************************** Actualizando "+audit.name+"  ******  "+audit.persisted_object_id
                                    int updated=centralSql.executeUpdate(row, config.updateSql)
                                    if(updated){
                                    //    println "Se actualizo el registro se va a crear auditLog"
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ",audit.id])
                                    }else{
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR ",audit.id])
                                    }

                                    break
                                case 'DELETE':

                                    def res=centralSql.firstRow("SELECT *  FROM ${config.tableName} WHERE ${config.pk}=?",[audit.persisted_object_id])

                                    if(res){
                                        def rs= centralSql.execute("DELETE FROM ${config.tableName} WHERE ${config.pk}=?",[audit.persisted_object_id])
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ELIMINADO",audit.id])
                                    }else{
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REGISTRO NO EXISTENTE EN EL TARGET",audit.id])
                                    }

                                    break;
                                default:
                                    break;
                            }

                            afterImport(audit,server)
                            if(config.name == 'SolicitudDeTraslado' || config.name == 'SolicitudDeTrasladoDet' ){
                                afterImportVales( audit, row, centralSql)
                            }
                            if(config.name == 'Traslado' || config.name == 'TrasladoDet' ){

                              //  println "Importando" +config.name
                                afterImportTraslados(audit,row,server,centralSql )
                            }


                        }
                        catch (DuplicateKeyException dk) {
                        //    println dk.getMessage()
                          //  println "Registro duplicado ${audit.id} -- ${audit.persisted_object_id}"
                            sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["Registro duplicado",audit.id])

                        }catch (Exception e){
                            //e.printStackTrace()
                            log.error(e)
                            String err="Error importando a central: "+ExceptionUtils.getRootCauseMessage(e)
                            sql.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=NOW() WHERE ID=? ", [err,audit.id])
                        }


                    }
                    else{

                       // println '***************  No se encontraron registros para insertar'+audit.id

                    }
                }
                else{
                  //  println "No tiene registro de configuracion"
                }
            }


    }
    def importarServer(serverName){

    //  println "***  Importando de Por ReplicaService: ****  "+serverName+"eMBARQUESS"


        def query="Select * from audit_log where name in ('EMBARQUE','ENVIO','ENVIODET') and date_replicated is null "

        def server=DataSourceReplica.findAllByActivaAndCentralAndServer(true,false,serverName)

        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

        def datasourceCentral=dataSourceLocatorService.dataSourceLocator(central.server)

        def centralSql=new Sql(datasourceCentral)



             //   println "***  Importando de Por ReplicaService: ${server.server} ******* ${server.url}****  "

            def datasourceOrigen=dataSourceLocatorService.dataSourceLocator(server.server)


            def sql=new Sql(datasourceOrigen)


            sql.rows(query).each{audit ->

                def config= EntityConfiguration.findByName(audit.name)

          //     println "Importando       " +audit.name+"  ---------  "+audit.persisted_object_id+"   "+config

                if(config){

                    def origenSql="select * from $config.tableName where $config.pk=?"


                    def row=sql.firstRow(origenSql, [audit.persisted_object_id])
                    if(audit.event_name=='DELETE' || row){
                        try {
                            switch (audit.event_name) {

                                case 'INSERT':
                                //    println 'Insertando '+row
                                    SimpleJdbcInsert insert=new SimpleJdbcInsert(datasourceCentral).withTableName(config.tableName)

                                    def res=insert.execute(row)

                                    if(res){
                                //        println '*************** Registros importados: '+res
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                                    }else{
                                 //       println '***************  No se encontraron registros para insertar'
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR",audit.id])
                                    }

                                    break
                                case 'UPDATE':

                                //    println "****************************** Actualizando "+audit.name+"  ******  "+audit.persisted_object_id
                                    int updated=centralSql.executeUpdate(row, config.updateSql)
                                    if(updated){
                                    //    println "Se actualizo el registro se va a crear auditLog"
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ",audit.id])
                                    }else{
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR ",audit.id])
                                    }

                                    break
                                case 'DELETE':

                                    def res=centralSql.firstRow("SELECT *  FROM ${config.tableName} WHERE ${config.pk}=?",[audit.persisted_object_id])

                                    if(res){
                                        def rs= centralSql.execute("DELETE FROM ${config.tableName} WHERE ${config.pk}=?",[audit.persisted_object_id])
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ELIMINADO",audit.id])
                                    }else{
                                        sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REGISTRO NO EXISTENTE EN EL TARGET",audit.id])
                                    }

                                    break;
                                default:
                                    break;
                            }

                            afterImport(audit,server)
                            if(config.name == 'SolicitudDeTraslado' || config.name == 'SolicitudDeTrasladoDet' ){
                                afterImportVales( audit, row, centralSql)
                            }
                            if(config.name == 'Traslado' || config.name == 'TrasladoDet' ){

                              //  println "Importando" +config.name
                                afterImportTraslados(audit,row,server,centralSql )
                            }


                        }
                        catch (DuplicateKeyException dk) {
                        //    println dk.getMessage()
                          //  println "Registro duplicado ${audit.id} -- ${audit.persisted_object_id}"
                            sql.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["Registro duplicado",audit.id])

                        }catch (Exception e){
                            //e.printStackTrace()
                            log.error(e)
                            String err="Error importando a central: "+ExceptionUtils.getRootCauseMessage(e)
                            sql.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=NOW() WHERE ID=? ", [err,audit.id])
                        }


                    }
                    else{

                       // println '***************  No se encontraron registros para insertar'+audit.id

                    }
                }
                else{
                  //  println "No tiene registro de configuracion"
                }
            }


    }

    def afterImport(def auditOrigen,def serverOrigen ){

        switch (auditOrigen.name){
            case 'Existencia':
             //   println("Dispersando ${auditOrigen.name} **** ${serverOrigen.server} ")
                disperseafterImport(auditOrigen,serverOrigen)
                break
            case 'Cliente':
              //  println("Dispersando ${auditOrigen.name} ")
                disperseafterImport(auditOrigen,serverOrigen)
                break
            case 'DireccionDeEntrega':
              //  println("Dispersando ${auditOrigen.name} ")
                disperseafterImport(auditOrigen,serverOrigen)
                break
            case 'ClienteContactos':
              //  println("Dispersando ${auditOrigen.name} ")
                disperseafterImport(auditOrigen,serverOrigen)
                break
            case 'ComunicacionEmpresa':
               // println("Dispersando ${auditOrigen.name} ")
                disperseafterImport(auditOrigen,serverOrigen)
                break

            default:

                break
        }

    }

    def disperseafterImport(def auditOrigen,def serverOrigen ){

       // println("Disperse after import")

        def servers=DataSourceReplica.findAllByCentralAndServerNotEqual(false,serverOrigen.server)

        servers.each{ server ->

          //  println("Generando Auditlog para : ${server.server}")

            def audit=new AuditLog()

            audit.name =auditOrigen.name
            audit.tableName=auditOrigen.table_name
            audit.persistedObjectId = auditOrigen.persisted_object_id
            audit.eventName =auditOrigen.event_name
            audit.source ='CENTRAL'
            audit.target =server.server
            audit.dateCreated = audit.lastUpdated = new Date()

            audit.save(failOnError: true,flush: true)

        }

    }

    def afterImportVales(def auditOrigen,def row,def centralSql){

        def audit=new AuditLog()

        audit.name =auditOrigen.name
        audit.tableName=auditOrigen.table_name
        audit.persistedObjectId = auditOrigen.persisted_object_id
        audit.eventName =auditOrigen.event_name
        audit.source ='CENTRAL'
        audit.dateCreated = audit.lastUpdated = new Date()

        if(auditOrigen.name == 'SolicitudDeTraslado'){
            def sucursal=resolveSucursal(row.sucursal_atiende_id)
            audit.target=sucursal.nombre
        }else{
            def sol=centralSql.firstRow("select * from solicitud_de_traslado where id=?",[row.solicitud_de_traslado_id])
            def sucursal=resolveSucursal(sol.sucursal_atiende_id)
            audit.target=sucursal.nombre
        }


        audit.save(failOnError: true,flush: true)

    }

    def afterImportTraslados(def auditOrigen,def row,def serverOrigen, def centralSql ){

        def sucursal=new Sucursal()

        if(auditOrigen.name== 'Traslado'){
            sucursal=resolveSucursal(row.sucursal_id)
        }else{
            def trd=centralSql.firstRow("select * from traslado where id=?",[row.traslado_id])
            sucursal=resolveSucursal(trd.sucursal_id)
        }


        def audit=new AuditLog()

        audit.name =auditOrigen.name
        audit.tableName=auditOrigen.table_name
        audit.persistedObjectId = auditOrigen.persisted_object_id
        audit.eventName =auditOrigen.event_name
        audit.source ='CENTRAL'
        audit.dateCreated = audit.lastUpdated = new Date()
        audit.target=sucursal.nombre

        audit.save(failOnError: true,flush: true)


    }

    def resolveSucursal(def sucursalId){
        def sucursal=Sucursal.get(sucursalId)
    }

    def exportar(entityName){
      def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)
      servers.each{server ->
        exportar(entityName,server.server)
      }
    }

    def exportar(entityName,serverName){

      def query="Select * from audit_log where name= ${entityName} and date_replicated is null "

    //  println "***  Exportando  Por ReplicaService: ****  "+entityName+"  ************ "+serverName

        def server=DataSourceReplica.findAllByActivaAndCentralAndServer(true,false,serverName)

  //      println "***  Exportando  Por ReplicaService: ****  "+entityName+"  ************ "+server.server

        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

        def datasourceCentral=dataSourceLocatorService.dataSourceLocator(central.server)

        def sqlCen=new Sql(datasourceCentral)

        def dataSourceSuc=dataSourceLocatorService.dataSourceLocator(server.server)

        def sqlSuc=new Sql(dataSourceSuc)
//println "*** ***************************************"
         sqlCen.rows(query+" and target='${serverName}'").each { audit ->
//println "------------------------------------------------"
                def config= EntityConfiguration.findByName(audit.name)

                if(config){
                    def sqlEntity="select * from $config.tableName where $config.pk=?"

                    def row=sqlCen.firstRow(sqlEntity, [audit.persisted_object_id])
                   if(audit.event_name=='DELETE' || row){
                          try{

                            switch (audit.event_name) {

                                case 'INSERT':
                                   //println 'Insertando '+row
                                    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSourceSuc).withTableName(config.tableName)
                                    def res=insert.execute(row)

                                    if(res){
                                    //    println '***************  Actualizando audit log'+audit.id
                                        sqlCen.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                                    }
                                    break

                                case 'UPDATE':
                            //        println 'Actualizando '+row
                                    int updated=sqlSuc.executeUpdate(row, config.updateSql)
                                    if(updated) {
                              //          println '***************  Actualizando audit log'+audit.id
                                        sqlCen.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ", audit.id])
                                    }else{
                              //          println '***************  No se actualizo'+audit.id
                                    }
                                    break
                                case 'DELETE':

                                    break;
                                default:
                                    break;
                            }

                          }
                          catch (DuplicateKeyException dk) {
                    //          println dk.getMessage()
                            //  println "Registro duplicado ${audit.id} -- ${audit.persisted_object_id}"
                              sqlCen.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["Registro duplicado",audit.id])

                          }catch (Exception e){
                              //e.printStackTrace()

                              println "${audit.id} -- ${audit.persisted_object_id}"
                              log.error(e)
                              String err="Error importando a central: "+ExceptionUtils.getRootCauseMessage(e)
                            }
                  }

              }
       }

  }


}

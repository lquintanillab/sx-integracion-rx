package sx.importacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.DataSourceReplica
import sx.EntityConfiguration
import sx.Sucursal
import sx.AuditLog
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert



@Component
class ImportadorDeVales{


  @Autowired
   @Qualifier('dataSourceLocatorService')
  def dataSourceLocatorService
  @Autowired
  @Qualifier('dataSource')
  def dataSource
  @Autowired
  @Qualifier('replicaService')
  def replicaService

  def importar(){

  //  println ("Importando Vales" )

    def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

      def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

      servers.each{server ->

    //    println "***  Importando Vales: ${server.server} ******* ${server.url}****  "
        importarServer(server)
      }

  }

  def importarSucursal(nombreSuc){

    def server=DataSourceReplica.findByServer(nombreSuc)


  //  println "nombre: ${nombreSuc}  URL: ${server.url} "

      importarServer(server)
  }

    def importarServer(server){

      def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)

      def sqlSuc=new Sql(dataSourceSuc)
      def sqlCen=new Sql(dataSource)

      def config= EntityConfiguration.findByName("SolicitudDeTraslado")
      def configDet= EntityConfiguration.findByName("SolicitudDeTrasladoDet")

      def queryAuditLog="Select * from audit_log where date_replicated is null and name='SolicitudDeTraslado'"

      def audits=sqlSuc.rows(queryAuditLog)

      def queryId="select * from solicitud_de_traslado  where id=?"

      audits.each{ audit ->

          def solSuc=sqlSuc.firstRow(queryId,[audit.persisted_object_id])

          if(solSuc || audit.event_name=='DELETE'){

           try{
              switch(audit.event_name) {
                case 'INSERT':
                  def solCen=sqlCen.firstRow(queryId,[solSuc.id])
                  def res=null
                  if(!solCen){
                    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName(config.tableName)
                     res=insert.execute(solSuc)
                  }

                  def partidasSuc=sqlSuc.rows("select * from solicitud_de_traslado_det where solicitud_de_traslado_id=?",[solSuc.id])
                  partidasSuc.each{ detalle ->
                    def partidaCen=sqlCen.firstRow("select * from solicitud_de_traslado_det where id=?",[detalle.id])
                     if(!partidaCen){
                       SimpleJdbcInsert insert1=new SimpleJdbcInsert(dataSource).withTableName(configDet.tableName)
                       insert1.execute(detalle)
                     }

                  }
                    afterImportVales(audit,solSuc,sqlCen)
                  //if(res){
                      sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                  //}


                break
                case 'UPDATE':
                        int updated=sqlCen.executeUpdate(solSuc, config.updateSql)
                    //    println "************************************"
                        def partidasSuc=sqlSuc.rows("select * from solicitud_de_traslado_det where solicitud_de_traslado_id=?",[solSuc.id])
                        partidasSuc.each{ detalle ->
                            sqlCen.executeUpdate(detalle, configDet.updateSql)
                        }

                        afterImportVales(audit,solSuc,sqlCen)

                        if(updated){
                            //println "Se actualizo el registro se va a crear auditLog"
                            sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ",audit.id])
                        }else{
                            sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR ",audit.id])
                        }
                break

                case 'DELETE':

                break

                default:

                break
              }

           }
         catch (DuplicateKeyException dk) {
              //    println dk.getMessage()
              //    println "Registro duplicado ${audit.id} -- ${audit.persisted_object_id}"
                  sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["Registro duplicado",audit.id])

              }catch (Exception e){
                  e.printStackTrace()
                String err="Error importando a central: "

                  sqlSuc.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=null WHERE ID=? ", [err,audit.id])
              }

          }
          else{
            sqlSuc.execute("UPDATE AUDIT_LOG SET MESSAGE='NO EXISTE',DATE_REPLICATED=NOW() WHERE ID=? ", [audit.id])
          }

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

  def resolveSucursal(def sucursalId){
      def sucursal=Sucursal.get(sucursalId)
  }

}

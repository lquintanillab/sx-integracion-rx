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
class ImportadorDeTraslados{


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

  //  println ("Importando Traslados" )

    def servers=DataSourceReplica.findAllByActivaAndCentralAndSucursal(true,false,true)

      def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

      servers.each{server ->
    //        println ("Importando Traslados"+server.server+"*************"+server.url )
        importarServer(server)
      }

  }

  def importarSucursal(nombreSuc){

    def server=DataSourceReplica.findByServer(nombreSuc)
      importarServer(server)
  }

    def importarServer(server){

  //    println ("Importando Traslados"+server.server+"----------------"+server.url )

      def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)

      def sqlSuc=new Sql(dataSourceSuc)
      def sqlCen=new Sql(dataSource)

      def config= EntityConfiguration.findByName("Traslado")
      def configDet= EntityConfiguration.findByName("TrasladoDet")
      def configCfdi= EntityConfiguration.findByName("Cfdi")
      def configInv= EntityConfiguration.findByName("Inventario")

      def queryAuditLog="Select * from audit_log where date_replicated is null and name='Traslado'"

      def audits=sqlSuc.rows(queryAuditLog)

      def queryId="select * from traslado  where id=?"



      audits.each{ audit ->

      //     println ("Importando ************-*-*-**-*-*-*-**-*-*-**-*-*-*-*-*--*-**-* " )
      //     println audit

          def trdSuc=sqlSuc.firstRow(queryId,[audit.persisted_object_id])

          if(trdSuc || audit.event_name=='DELETE'){

           try{
              switch(audit.event_name) {
                case 'INSERT':
                  if(trdSuc.cfdi_id){
                    def queryCfdi="Select * from cfdi where id=?"
                    def cfdiSuc=sqlSuc.firstRow(queryCfdi,[trdSuc.cfdi_id])
                    def cfdiCen=sqlCen.firstRow(queryCfdi,[trdSuc.cfdi_id])
                    if(!cfdiCen){
                        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName(configCfdi.tableName)
                        def res=insert.execute(cfdiSuc)
                    }
                  }
                    def trdCen=sqlCen.firstRow(queryId,[audit.persisted_object_id])
                    if(!trdCen){
                        SimpleJdbcInsert insert1=new SimpleJdbcInsert(dataSource).withTableName(config.tableName)
                        def res=insert1.execute(trdSuc)
                        if(res){
                            sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                        }
                    }
              //        println "-------------------------"+trdSuc

                def partidasSuc=sqlSuc.rows("select * from traslado_det where traslado_id=?",[trdSuc.id])
                partidasSuc.each{ detalle ->
            //      println "***********************"+detalle
                  if(detalle.inventario_id){
                    def queryInv="Select * from Inventario where id=?"
                    def invSuc=sqlSuc.firstRow(queryInv,[detalle.inventario_id])
                    def invCen=sqlCen.firstRow(queryInv,[detalle.inventario_id])
                      if(!invCen){
                          SimpleJdbcInsert insert2=new SimpleJdbcInsert(dataSource).withTableName(configInv.tableName)
                          insert2.execute(invSuc)
                      }
                  }
                  def sqlDetCen="Select * from traslado_det where id=?"
                  def detCen=sqlCen.firstRow(sqlDetCen,[detalle.id])
                  if(!detCen){
                      SimpleJdbcInsert insert3=new SimpleJdbcInsert(dataSource).withTableName(configDet.tableName)
                      insert3.execute(detalle)
                  }
                }
                      afterImportTraslados(audit,trdSuc,sqlCen)

                        sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                break
                case 'UPDATE':

                if(trdSuc.cfdi_id){
                  def queryCfdi="Select * from cfdi where id=?"
                  def cfdiSuc=sqlSuc.firstRow(queryCfdi,[trdSuc.cfdi_id])
                  def cfdiCen=sqlCen.firstRow(queryCfdi,[trdSuc.cfdi_id])
                  if(!cfdiCen){
                      SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName(configCfdi.tableName)
                      def res=insert.execute(cfdiSuc)
                  }
                }
                  def trdCen=sqlCen.firstRow(queryId,[audit.persisted_object_id])
                  if(!trdCen){
                      SimpleJdbcInsert insert1=new SimpleJdbcInsert(dataSource).withTableName(config.tableName)
                      def res=insert1.execute(trdSuc)
                      if(res){
                          sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                      }
                  }else{
                    def res=sqlCen.executeUpdate(trdSuc, config.updateSql)
                     if(res){
                         sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                     }
                  }
              //      println "-------------------------"+trdSuc

              def partidasSuc=sqlSuc.rows("select * from traslado_det where traslado_id=?",[trdSuc.id])
              partidasSuc.each{ detalle ->
          //      println "***********************"+detalle
                if(detalle.inventario_id){
                  def queryInv="Select * from Inventario where id=?"
                  def invSuc=sqlSuc.firstRow(queryInv,[detalle.inventario_id])
                  def invCen=sqlCen.firstRow(queryInv,[detalle.inventario_id])
                    if(!invCen){
                        SimpleJdbcInsert insert2=new SimpleJdbcInsert(dataSource).withTableName(configInv.tableName)
                        insert2.execute(invSuc)
                    }
                }
                def sqlDetCen="Select * from traslado_det where id=?"
                def detCen=sqlCen.firstRow(sqlDetCen,[detalle.id])
                if(!detCen){
                    SimpleJdbcInsert insert3=new SimpleJdbcInsert(dataSource).withTableName(configDet.tableName)
                    insert3.execute(detalle)
                }else{
                   sqlCen.executeUpdate(detalle, configDet.updateSql)
                }
              }
                  sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                    afterImportTraslados(audit,trdSuc,sqlCen)

                break

                case 'DELETE':

                break

                default:

                break
              }

          }
         catch (DuplicateKeyException dk) {
                  println dk.getMessage()
              //    println "Registro duplicado ${audit.id} -- ${audit.persisted_object_id}"
                  sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["Registro duplicado",audit.id])

              }catch (Exception e){
                  e.printStackTrace()
                String err="Error importando a central: "

                  sqlSuc.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=null WHERE ID=? ", [err,audit.id])
              }

          }
          else{
          //  sqlSuc.execute("UPDATE AUDIT_LOG SET MESSAGE='NO EXISTE',DATE_REPLICATED=NOW() WHERE ID=? ", [audit.id])
          }

      }


  }


  def afterImportTraslados(def auditOrigen,def row, def centralSql ){

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

}

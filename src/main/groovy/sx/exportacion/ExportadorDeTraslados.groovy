package sx.exportacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.DataSourceReplica
import sx.EntityConfiguration
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert



@Component
class ExportadorDeTraslados{

  @Autowired
   @Qualifier('dataSourceLocatorService')
  def dataSourceLocatorService
  @Autowired
  @Qualifier('dataSource')
  def dataSource
  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def exportar(){
      println ("Exportando Traslados" )

        def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

        servers.each{server ->

          exportarServer(server)
        }
  }

  def exportarSucursal(nombreSuc){
    def server=DataSourceReplica.findByServer(nombreSuc)
      exportarServer(server)
  }

  def exportarServer(server){
    def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)

    def sqlSuc=new Sql(dataSourceSuc)
    def sqlCen=new Sql(dataSource)

    def config= EntityConfiguration.findByName("Traslado")
    def configDet= EntityConfiguration.findByName("TrasladoDet")

    def queryAuditLog="Select * from audit_log where date_replicated is null and name='Traslado' and target=?"

    def audits=sqlCen.rows(queryAuditLog,[server.server])

    def queryId="select * from traslado  where id=?"

    audits.each{ audit ->

          def trdCen=sqlCen.firstRow(queryId,[audit.persisted_object_id])

          if(trdCen || audit.event_name=='DELETE'){
     try{
  //    println "---------------"+audit.persisted_object_id+"     -----------------    "
  //    println "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+trdCen.id
                switch(audit.event_name) {
                  case 'INSERT':
                  //  println "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++INSERT    "+trdCen.id

                    def trdSuc=sqlSuc.firstRow(queryId,[trdCen.id])

                    if(!trdSuc){
                      SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSourceSuc).withTableName(config.tableName)
                      def res=insert.execute(trdCen)
                    }

                  def partidasCen=sqlCen.rows("select * from traslado_det where traslado_id=?",[trdCen.id])
                  partidasCen.each{ detalle ->

                    def detalleSuc=sqlSuc.firstRow("select * from traslado_det where id=?",[detalle.id])
                    if(!detalleSuc){
                      SimpleJdbcInsert insert1=new SimpleJdbcInsert(dataSourceSuc).withTableName(configDet.tableName)
                      insert1.execute(detalle)
                    }

                  }
                //  if(res){

                      sqlCen.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                //  }
                  break
                  case 'UPDATE':
               //     println "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++UPDATE    "+trdCen.id
                      //int updated=sqlSuc.executeUpdate(trdCen, config.updateSql)
                    //  println "************************************"
                      def partidasCen=sqlCen.rows("select * from traslado_det where traslado_id=?",[trdCen.id])
                      partidasCen.each{ detalle ->
                          //sqlSuc.executeUpdate(detalle, configDet.updateSql)
                      }
                      /*
                      if(updated){
                          println "Se actualizo el registro se va a crear auditLog"
                        //  sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ",audit.id])
                      }else{
                        //  sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR ",audit.id])
                      }
                      */
                      //  println "--------------------------------------------Se actualizo el registro se va a crear auditLog----------------------------------------"
                       sqlCen.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ",audit.id])
                  break
                  case 'DELETE':

                  break
                  default:

                  break
                }

            }
            catch (DuplicateKeyException dk) {
                    //   println dk.getMessage()
                   //    println "Registro duplicado ${audit.id} -- ${audit.persisted_object_id}"
                       sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["Registro duplicado",audit.id])

                   }catch (Exception e){
                       e.printStackTrace()
                     String err="Error importando a central: "

                       sqlSuc.execute("UPDATE AUDIT_LOG SET MESSAGE=?,DATE_REPLICATED=null WHERE ID=? ", [err,audit.id])
                   }

          }

    }


  }

}

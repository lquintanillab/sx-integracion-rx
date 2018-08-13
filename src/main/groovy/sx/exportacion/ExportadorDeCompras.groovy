package sx.exportacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.DataSourceReplica
import sx.EntityConfiguration
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import sx.Sucursal




@Component
class ExportadorDeCompras{


      @Autowired
     @Qualifier('dataSourceLocatorService')
    def dataSourceLocatorService
    @Autowired
    @Qualifier('dataSource')
    def dataSource

    def exportar(){
     println ("Importando Compras" )

      def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

        servers.each(){server ->
          //println "***  Exportando de Por ReplicaService: ${server.server} ******* ${server.url}****  "
          exportarServer(server)
        }

    }

    def exportarServer(server){

        def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
          def sqlSuc=new Sql(dataSourceSuc)
          def sqlCen=new Sql(dataSource)

          def sucursal=Sucursal.findByNombre(server.server)
          def queryAudits="Select * from audit_log where name in ('Compra','CompraDet') and date_replicated is null and target=?"

          def audits=sqlCen.rows(queryAudits,[server.server])

          audits.each{ audit ->
               // println audit.id+"--"+audit.persisted_object_id

                def config= EntityConfiguration.findByName(audit.name)

                def queryEntity="Select * from ${audit.table_name} where id=?"

                def opSuc=sqlSuc.firstRow(queryEntity,[audit.persisted_object_id])
                def opCen=sqlCen.firstRow(queryEntity,[audit.persisted_object_id])

                   // println opSuc
                   // println opCen
                if(!opSuc){
                    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSourceSuc).withTableName(config.tableName)
                    def res=insert.execute(opCen)
                     if(res){
                      //  println '*************** Registros Exportados: '+res
                            sqlCen.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["IMPORTADO",audit.id])
                    }else{
                       //     println '***************  No se encontraron registros para insertar'
                            sqlCen.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR",audit.id])
                    }

                }else{
                     int updated=sqlSuc.executeUpdate(opCen, config.updateSql)
                      if(updated){
                       // println "Se actualizo el registro se va a crear auditLog"
                            sqlCen.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["ACTUALIZADO: ",audit.id])
                        }else{
                            sqlCen.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE ID=? ", ["REVISAR ",audit.id])
                        }
                }
          }

    }

  


}

package sx.importacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.DataSourceReplica
import sx.EntityConfiguration
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert


@Component
class ImportadorDeAplicacionesCOD{


  @Autowired
   @Qualifier('dataSourceLocatorService')
  def dataSourceLocatorService
  @Autowired
  @Qualifier('dataSource')
  def dataSource



      def importar(){
      //  println ("Importando Cobros  " )

        def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

          def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

          servers.each(){server ->
        //    println "***  Importando de Por ReplicaService: ${server.server} ******* ${server.url}****  "
            importarServer(server)
          }
      }

    def importarSucursal(nombreSuc){

        def server=DataSourceReplica.findByServer(nombreSuc)

      //  println "nombre: ${nombreSuc}  URL: ${server} "

      importarServer(server)

      }

      def importarServer(server){

    //    println "Importando Por Server"

        def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
        def sqlSuc=new Sql(dataSourceSuc)
        def sqlCen=new Sql(dataSource)
        def configCobro= EntityConfiguration.findByName("Cobro")

        def queryAplSuc=""" select p.* from audit_log a join aplicacion_de_cobro p on (a.persisted_object_id=p.id)
                            join cuenta_por_cobrar c on (c.id=p.cuenta_por_cobrar_id)
                            where name='AplicacionDeCobro' and a.date_replicated is null and c.tipo='COD'
                             """
       def aplicaciones =sqlSuc.rows(queryAplSuc)

       aplicaciones.each{ row ->

            def queryApl="Select * from aplicacion_de_cobro a where a.id=? "

            def aplSuc=sqlSuc.firstRow(queryApl,[row.id])

            def aplCen= sqlCen.firstRow(queryApl,[aplSuc.id])

            if(aplCen){
              // println "la aplicacion ya existe"
               sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? ", ["Registro ya existente",row.id])
            }else{
              //  println "la aplicacion no existe"

                def queryCobro="Select * from cobro where id=?"
                def cobroCen=sqlCen.firstRow(queryCobro,[aplSuc.cobro_id])
                def cobroSuc=sqlSuc.firstRow(queryCobro,[aplSuc.cobro_id])
                if(cobroCen){

                //   println "El cobro ya existe"
                   sqlCen.executeUpdate(cobroSuc, configCobro.updateSql)
                }else{
                //   println "El cobro NO existe"

                    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cobro")
                     def res=insert.execute(cobroSuc)

                }

                def queryCfdi="Select * from cfdi where id in (Select cfdi_id from cuenta_por_cobrar where id=?)"

                def cfdiCen=sqlCen.firstRow(queryCfdi,aplSuc.cuenta_por_cobrar_id)

                if(cfdiCen){
                //   println "El cfdi ya existe"
                }else{
                //   println "El cfdi no existe"
                   def cfdiSuc=sqlSuc.firstRow(queryCfdi,aplSuc.cuenta_por_cobrar_id)
                     SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cfdi")
                     def res=insert.execute(cfdiSuc)

                }

                def queryCxc= "Select * from cuenta_por_cobrar where id=?"

                def cxcCen=sqlCen.firstRow(queryCxc,aplSuc.cuenta_por_cobrar_id)

                if(cxcCen){
                  // println "El cxc ya existe"
                }else{
                //   println "El cxc no existe"
                   def cxcSuc=sqlSuc.firstRow(queryCxc,aplSuc.cuenta_por_cobrar_id)
                     SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cuenta_por_cobrar")
                     def res=insert.execute(cxcSuc)
                }

                   SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("aplicacion_de_cobro")
                   def res=insert.execute(aplSuc)
                sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? ", ["Registro replicado",row.id])

            }

       }



      }

  }

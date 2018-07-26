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
class ImportadorDeVentas{
  @Autowired
   @Qualifier('dataSourceLocatorService')
  def dataSourceLocatorService
  @Autowired
  @Qualifier('dataSource')
  def dataSource

  def importar(){

//    println ("Importando Ventas De credito" )

    def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

      def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

      servers.each(){server ->
    //    println "***  Importando de Por ReplicaService: ${server.server} ******* ${server.url}****  "
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

      def config= EntityConfiguration.findByName("Cfdi")

      def queryAuditLog="select c.*,a.persisted_object_id from audit_log a join cfdi c on(a.persisted_object_id=c.id) join cuenta_por_cobrar u on (u.cfdi_id=c.id) where date_replicated is null and u.tipo in ('CON') AND event_name='INSERT' order by a.date_created"

      def cfdis=sqlSuc.rows(queryAuditLog)

      def queryId="select * from cfdi where id=?"

      cfdis.each{row ->

        try{
          def cfdi=sqlCen.firstRow(queryId,[row.id])

          if(cfdi){
    //         println "EL registro ya fue importado Solo actualizar"
            sqlCen.executeUpdate(row, config.updateSql)

          }else{
      //       println "El registro no ha sido importado se debe importar"
            SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cfdi")
             def res=insert.execute(row)
          }

          def queryCxc="Select * from cuenta_por_cobrar where cfdi_id=?"
          def queryCxcCen="select * from cuenta_por_cobrar where id=?"
          def cxc=sqlSuc.firstRow(queryCxc,[row.id])


          if(cxc){

            def cxcCen=sqlCen.firstRow(queryCxcCen,[cxc.id])
            def configCxc=EntityConfiguration.findByName("CuentaPorCobrar")

            if(cxcCen){
        //      println "EL registro de cxc ya fue importado Solo actualizar"
              sqlCen.executeUpdate(cxc, configCxc.updateSql)
            }else{
          //      println "El registro no ha sido importado se debe importar"
                SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cuenta_por_cobrar")
                def res=insert.execute(cxc)
            }

          //   println "*** ************** ------ CXC "+cxc

              def queryVenta="select * from venta where cuenta_por_cobrar_id=?"
              def venta=sqlSuc.firstRow(queryVenta,[cxc.id])


              if(venta){

                def queryVentaCen="select * from venta where id=?"
                def ventaCen=sqlCen.firstRow(queryVentaCen,[venta.id])
                def configVta=EntityConfiguration.findByName("venta")

                if(ventaCen){
          //        println "EL registro de venta ya fue importado Solo actualizar"
                  sqlCen.executeUpdate(venta, configVta.updateSql)
                }else{
              //      println "El registro no ha sido importado se debe importar"
                    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("venta")
                    def res=insert.execute(venta)
                }

            //      println "***************** ------ ++++++ VENTA "+venta
                  def queryCond="select * from condicion_de_envio where venta_id =?"
                  def condicionDeEnvio=sqlSuc.firstRow(queryCond,[venta.id])

                  if(condicionDeEnvio){

                    def queryCondCen="select * from condicion_de_envio where id=?"
                    def condicionDeEnvioCen=sqlCen.firstRow(queryCondCen,[condicionDeEnvio.id])
                    def configEnv=EntityConfiguration.findByName("CondicionDeEnvio")

                   //   println "*** ************** ------ ++++++----------------  CONDICION ENVIO "+condicionDeEnvio/
                   if(condicionDeEnvioCen){
              //       println "EL registro de condicion de envio ya fue importado Solo actualizar"
                     sqlCen.executeUpdate(condicionDeEnvio, configEnv.updateSql)
                   }else{
            //           println "El registro de condicion de  envio no ha sido importado se debe importar"
                       SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("condicion_de_envio")
                       def res=insert.execute(condicionDeEnvio)
                   }

                  }

              def queryVentaDet="select * from Venta_det where venta_id=?"
              def ventasDet=sqlSuc.rows(queryVentaDet,[venta.id])



                  if(ventasDet){
                      ventasDet.each{ ventaDet ->
                      //    println "*** ************** ------ ++++++-------------------- VENTADET "+ventaDet
                          if(ventaDet.inventario_id  ){

                             def queryInv="select * from inventario where id = ?"
                              def invent=sqlSuc.firstRow(queryInv,[ventaDet.inventario_id])
                              def queryInvCen="select * from inventario where id=?"
                              def inventCen=sqlCen.firstRow(queryInvCen,[invent.id])
                              def configInv=EntityConfiguration.findByName("Inventario")
                             if(inventCen){

                      //            println "EL registro  de inventario ya fue importado Solo actualizar"
                                  sqlCen.executeUpdate(invent, configInv.updateSql)
                              }else{
                          //      println "El registro de inventario  no ha sido importado se debe importar"
                                SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("inventario")
                                def res=insert.execute(invent)
                              }
                          }

                          def queryVentaDetCen="select * from venta_det where id=?"
                          def ventaDetCen=sqlCen.firstRow(queryVentaDetCen,[ventaDet.id])
                          def configVentaDet=EntityConfiguration.findByName('VentaDet')
                          if(ventaDetCen){
                      //      println "EL registro  de ventaDet ya fue importado Solo actualizar"
                            sqlCen.executeUpdate(ventaDet, configVentaDet.updateSql)
                          }else{
                      //      println "El registro de ventaDet no ha sido importado se debe importar"
                            SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("venta_det")
                            def res=insert.execute(ventaDet)
                          }

                          def queryCorte="select * from instruccion_corte where venta_det_id=?"
                          def corte=sqlSuc.firstRow(queryCorte,[ventaDet.id])

                          if(corte){

                            def queryCorteCen="select * from instruccion_corte where id=?"
                            def corteCen=sqlCen.firstRow(queryCorteCen,[corte.id])
                            def configCorte=EntityConfiguration.findByName('InstruccionCorte')

                            if(corteCen){
                    //          println "EL registro  de corte ya fue importado Solo actualizar"
                              sqlCen.executeUpdate(corte, configCorte.updateSql)
                            }else{
                        //      println "El registro de corte no ha sido importado se debe importar"
                              SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("instruccion_corte")
                              def res=insert.execute(corte)
                            }
                           //   println "*** ************** ------ ++++++-------------------- CORTE "+corte
                          }
                      }
                  }
              }
          }
          sqlSuc.execute("UPDATE AUDIT_LOG SET DATE_REPLICATED=NOW(),MESSAGE=? WHERE PERSISTED_OBJECT_ID=? ", ["Registro replicado",row.persisted_object_id])
      }catch(Exception e){
          e.printStackTrace()
      }
    }

  }


}

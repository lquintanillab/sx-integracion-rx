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
class ImportadorDeCxc{

    @Autowired
     @Qualifier('dataSourceLocatorService')
    def dataSourceLocatorService
    @Autowired
    @Qualifier('dataSource')
    def dataSource
    @Autowired
     @Qualifier('replicaOperacionService')
    def replicaOperacionService

    def importarOperacionesVenta(){

      def fecha=new Date()

      importarOperacionesVentaFecha(fecha)
    }

   def importarOperacionesVentaFecha(fecha1){

     def fecha=fecha1.format('yyyy/MM/dd')
    //  println "Importando operaciones de venta  "+fecha
      def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

      //def servers=DataSourceReplica.findAllByServer('TACUBA')

      def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

      def datasourceCentral=dataSourceLocatorService.dataSourceLocator(central.server)

      def centralSql=new Sql(datasourceCentral)

      servers.each { server ->

          def datasourceOrigen=dataSourceLocatorService.dataSourceLocator(server.server)

          def sql=new Sql(datasourceOrigen)

        //  def query="Select * from cfdi where tipo_de_comprobante='I' and date(date_created)=? and serie like'%FACCRE'  "
          def query="Select * from cfdi where tipo_de_comprobante='I' and date(date_created)=? "

          sql.rows(query,[fecha]).each { audit ->

            try{
          //    println "***  Importando desde: ${server.server} *****************************************************************  CFDI   ${audit.id} "+fecha
          //    println "***  Importando desde: ${server.server} *****************************************************************  CFDI   ${audit.id} "+fecha
            //  println "***  Importando desde: ${server.server} *****************************************************************  CFDI   ${audit.id} "+fecha

              def queryCfdi= "select * from cfdi where origen='VENTA' and id=?"

              def cfdi=sql.firstRow(queryCfdi,[audit.id])

          //    println "***************** CFDI "+cfdi

              if(cfdi){

                    def cfdiCen=centralSql.firstRow("select * from cfdi where id=?",[cfdi.id])
                    def config= EntityConfiguration.findByName("Cfdi")

                      if(cfdiCen){
                    //    println "EL registro ya fue importado Solo actualizar"
                        centralSql.executeUpdate(cfdi, config.updateSql)
                      }else{
                //        println "El registro no ha sido importado se debe importar"
                        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cfdi")
                        def res=insert.execute(cfdi)
                      }

                  def queryCxc="Select * from cuenta_por_cobrar where cfdi_id=?"
                  def queryCxcCen="select * from cuenta_por_cobrar where id=?"
                  def cxc=sql.firstRow(queryCxc,[cfdi.id])


                  if(cxc){

                    def cxcCen=centralSql.firstRow(queryCxcCen,[cxc.id])
                    def configCxc=EntityConfiguration.findByName("CuentaPorCobrar")

                    if(cxcCen){
                  //    println "EL registro de cxc ya fue importado Solo actualizar"
                      centralSql.executeUpdate(cxc, configCxc.updateSql)
                    }else{
                    //    println "El registro no ha sido importado se debe importar"
                        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cuenta_por_cobrar")
                        def res=insert.execute(cxc)
                    }

                  //   println "*** ************** ------ CXC "+cxc

                      def queryVenta="select * from venta where cuenta_por_cobrar_id=?"
                      def venta=sql.firstRow(queryVenta,[cxc.id])


                      if(venta){

                        def queryVentaCen="select * from venta where id=?"
                        def ventaCen=centralSql.firstRow(queryVentaCen,[venta.id])
                        def configVta=EntityConfiguration.findByName("venta")

                        if(ventaCen){
                      //    println "EL registro de venta ya fue importado Solo actualizar"
                          centralSql.executeUpdate(venta, configVta.updateSql)
                        }else{
                      //      println "El registro no ha sido importado se debe importar"
                            SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("venta")
                            def res=insert.execute(venta)
                        }

                    //      println "***************** ------ ++++++ VENTA "+venta
                          def queryCond="select * from condicion_de_envio where venta_id =?"
                          def condicionDeEnvio=sql.firstRow(queryCond,[venta.id])

                          if(condicionDeEnvio){

                            def queryCondCen="select * from condicion_de_envio where id=?"
                            def condicionDeEnvioCen=centralSql.firstRow(queryCondCen,[condicionDeEnvio.id])
                            def configEnv=EntityConfiguration.findByName("CondicionDeEnvio")

                           //   println "*** ************** ------ ++++++----------------  CONDICION ENVIO "+condicionDeEnvio/
                           if(condicionDeEnvioCen){
                        //     println "EL registro de condicion de envio ya fue importado Solo actualizar"
                             centralSql.executeUpdate(condicionDeEnvio, configEnv.updateSql)
                           }else{
                        //       println "El registro de condicion de  envio no ha sido importado se debe importar"
                               SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("condicion_de_envio")
                               def res=insert.execute(condicionDeEnvio)
                           }

                          }

                      def queryVentaDet="select * from Venta_det where venta_id=?"
                      def ventasDet=sql.rows(queryVentaDet,[venta.id])



                          if(ventasDet){
                              ventasDet.each{ ventaDet ->
                              //    println "*** ************** ------ ++++++-------------------- VENTADET "+ventaDet
                                  if(ventaDet.inventario_id){

                                     def queryInv="select * from inventario where id = ?"
                                      def invent=sql.firstRow(queryInv,[ventaDet.inventario_id])
                                      def queryInvCen="select * from inventario where id=?"
                                      def inventCen=centralSql.firstRow(queryInvCen,[invent.id])
                                      def configInv=EntityConfiguration.findByName("Inventario")
                                     if(inventCen){

                                        //  println "EL registro  de inventario ya fue importado Solo actualizar"
                                          centralSql.executeUpdate(invent, configInv.updateSql)
                                      }else{
                                    //    println "El registro de inventario  no ha sido importado se debe importar"
                                        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("inventario")
                                        def res=insert.execute(invent)
                                      }

                                      def queryVentaDetCen="select * from venta_det where id=?"
                                      def ventaDetCen=centralSql.firstRow(queryVentaDetCen,[ventaDet.id])
                                      def configVentaDet=EntityConfiguration.findByName('VentaDet')
                                      if(ventaDetCen){
                                    //    println "EL registro  de ventaDet ya fue importado Solo actualizar"
                                        centralSql.executeUpdate(ventaDet, configVentaDet.updateSql)
                                      }else{
                                    //    println "El registro de ventaDet no ha sido importado se debe importar"
                                        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("venta_det")
                                        def res=insert.execute(ventaDet)
                                      }

                                      def queryCorte="select * from instruccion_corte where venta_det_id=?"
                                      def corte=sql.firstRow(queryCorte,[ventaDet.id])

                                      if(corte){

                                        def queryCorteCen="select * from instruccion_corte where id=?"
                                        def corteCen=centralSql.firstRow(queryCorteCen,[corte.id])
                                        def configCorte=EntityConfiguration.findByName('InstruccionCorte')

                                        if(corteCen){
                                    //      println "EL registro  de corte ya fue importado Solo actualizar"
                                          centralSql.executeUpdate(corte, configCorte.updateSql)
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

                      def aplicaciones=sql.rows("select * from aplicacion_de_cobro where cuenta_por_cobrar_id=?",[cxc.id])
                      aplicaciones.each{ aplicacion ->

                          def cobro=sql.firstRow("select * from cobro where id=?",[aplicacion.cobro_id])
                          if(cobro){
                        //    println "*** ************** ------ ++++++-------------------- COBRO "+cobro

                            def queryCobroCen="select * from cobro where id=?"
                            def cobroCen=centralSql.firstRow(queryCobroCen,[cobro.id])
                            def configCobro=EntityConfiguration.findByName('Cobro')
                            if(cobroCen){
                          //    println "EL registro  de cobro ya fue importado Solo actualizar"
                              centralSql.executeUpdate(cobro, configCobro.updateSql)
                            }else{
                          //    println "El registro de cobro no ha sido importado se debe importar"
                              SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cobro")
                              def res=insert.execute(cobro)
                            }

                          }
                      //    println "*** ************** ------ ++++++-------------------- APLICACION "+aplicacion

                            def queryAplicacionCen="select * from aplicacion_de_cobro where id=?"
                            def aplicacionCen=centralSql.firstRow(queryAplicacionCen,[aplicacion.id])
                            def configAplic=EntityConfiguration.findByName('AplicacionDeCobro')

                            if(aplicacionCen){
                        //      println "EL registro  de aplicacion ya fue importado Solo actualizar"
                              centralSql.executeUpdate(aplicacion, configAplic.updateSql)
                            }else{
                        //      println "El registro de aplicacion no ha sido importado se debe importar"
                              SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("aplicacion_de_cobro")
                              def res=insert.execute(aplicacion)
                            }

                      }

                  }
              }
            //Termina Try
          }catch(Exception e){
            e.printStackTrace()
          }

          }


      }

   }



   def importarOperacionesVentaSucursalFecha(sucursal,fecha1){

     def fecha=fecha1.format('yyyy/MM/dd')

    //  println "Importando operaciones de venta  "+fecha
    //  def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

      def servers=DataSourceReplica.findAllByServer(sucursal)

      def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

      def datasourceCentral=dataSourceLocatorService.dataSourceLocator(central.server)

      def centralSql=new Sql(datasourceCentral)

      servers.each { server ->

          def datasourceOrigen=dataSourceLocatorService.dataSourceLocator(server.server)

          def sql=new Sql(datasourceOrigen)

          def query="Select * from cfdi where tipo_de_comprobante='I' and date(date_created)=?  "

          sql.rows(query,[fecha]).each { audit ->

            //  println "***  Importando desde: ${server.server} *****************************************************************  CFDI   ${audit.id} "+fecha
          //    println "***  Importando desde: ${server.server} *****************************************************************  CFDI   ${audit.id} "+fecha
            //  println "***  Importando desde: ${server.server} *****************************************************************  CFDI   ${audit.id} "+fecha

              def queryCfdi= "select * from cfdi where origen='VENTA' and id=?"

              def cfdi=sql.firstRow(queryCfdi,[audit.id])

            //  println "***************** CFDI "+cfdi

              if(cfdi){

                    def cfdiCen=centralSql.firstRow("select * from cfdi where id=?",[cfdi.id])
                    def config= EntityConfiguration.findByName("Cfdi")

                      if(cfdiCen){
                  //      println "EL registro ya fue importado Solo actualizar"
                        centralSql.executeUpdate(cfdi, config.updateSql)
                      }else{
                    //    println "El registro no ha sido importado se debe importar"
                        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cfdi")
                        def res=insert.execute(cfdi)
                      }

                  def queryCxc="Select * from cuenta_por_cobrar where cfdi_id=?"
                  def queryCxcCen="select * from cuenta_por_cobrar where id=?"
                  def cxc=sql.firstRow(queryCxc,[cfdi.id])


                  if(cxc){

                    def cxcCen=centralSql.firstRow(queryCxcCen,[cxc.id])
                    def configCxc=EntityConfiguration.findByName("CuentaPorCobrar")

                    if(cxcCen){
                  //    println "EL registro de cxc ya fue importado Solo actualizar"
                      centralSql.executeUpdate(cxc, configCxc.updateSql)
                    }else{
                  //      println "El registro no ha sido importado se debe importar"
                        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cuenta_por_cobrar")
                        def res=insert.execute(cxc)
                    }

                  //   println "*** ************** ------ CXC "+cxc

                      def queryVenta="select * from venta where cuenta_por_cobrar_id=?"
                      def venta=sql.firstRow(queryVenta,[cxc.id])


                      if(venta){

                        def queryVentaCen="select * from venta where id=?"
                        def ventaCen=centralSql.firstRow(queryVentaCen,[venta.id])
                        def configVta=EntityConfiguration.findByName("venta")

                        if(ventaCen){
                    //      println "EL registro de venta ya fue importado Solo actualizar"
                          centralSql.executeUpdate(venta, configVta.updateSql)
                        }else{
                      //      println "El registro no ha sido importado se debe importar"
                            SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("venta")
                            def res=insert.execute(venta)
                        }

                      //    println "***************** ------ ++++++ VENTA "+venta
                          def queryCond="select * from condicion_de_envio where venta_id =?"
                          def condicionDeEnvio=sql.firstRow(queryCond,[venta.id])

                          if(condicionDeEnvio){

                            def queryCondCen="select * from condicion_de_envio where id=?"
                            def condicionDeEnvioCen=centralSql.firstRow(queryCondCen,[condicionDeEnvio.id])
                            def configEnv=EntityConfiguration.findByName("CondicionDeEnvio")

                           //   println "*** ************** ------ ++++++----------------  CONDICION ENVIO "+condicionDeEnvio/
                           if(condicionDeEnvioCen){
                      //       println "EL registro de condicion de envio ya fue importado Solo actualizar"
                             centralSql.executeUpdate(condicionDeEnvio, configEnv.updateSql)
                           }else{
                        //       println "El registro de condicion de  envio no ha sido importado se debe importar"
                               SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("condicion_de_envio")
                               def res=insert.execute(condicionDeEnvio)
                           }

                          }

                      def queryVentaDet="select * from Venta_det where venta_id=?"
                      def ventasDet=sql.rows(queryVentaDet,[venta.id])



                          if(ventasDet){
                              ventasDet.each{ ventaDet ->
                              //    println "*** ************** ------ ++++++-------------------- VENTADET "+ventaDet
                                  if(ventaDet.inventario_id){

                                     def queryInv="select * from inventario where id = ?"
                                      def invent=sql.firstRow(queryInv,[ventaDet.inventario_id])
                                      def queryInvCen="select * from inventario where id=?"
                                      def inventCen=centralSql.firstRow(queryInvCen,[invent.id])
                                      def configInv=EntityConfiguration.findByName("Inventario")
                                     if(inventCen){

                                    //      println "EL registro  de inventario ya fue importado Solo actualizar"
                                          centralSql.executeUpdate(invent, configInv.updateSql)
                                      }else{
                                    //    println "El registro de inventario  no ha sido importado se debe importar"
                                        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("inventario")
                                        def res=insert.execute(invent)
                                      }

                                      def queryVentaDetCen="select * from venta_det where id=?"
                                      def ventaDetCen=centralSql.firstRow(queryVentaDetCen,[ventaDet.id])
                                      def configVentaDet=EntityConfiguration.findByName('VentaDet')
                                      if(ventaDetCen){
                                //        println "EL registro  de ventaDet ya fue importado Solo actualizar"
                                        centralSql.executeUpdate(ventaDet, configVentaDet.updateSql)
                                      }else{
                                  //      println "El registro de ventaDet no ha sido importado se debe importar"
                                        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("venta_det")
                                        def res=insert.execute(ventaDet)
                                      }

                                      def queryCorte="select * from instruccion_corte where venta_det_id=?"
                                      def corte=sql.firstRow(queryCorte,[ventaDet.id])

                                      if(corte){

                                        def queryCorteCen="select * from instruccion_corte where id=?"
                                        def corteCen=centralSql.firstRow(queryCorteCen,[corte.id])
                                        def configCorte=EntityConfiguration.findByName('InstruccionCorte')

                                        if(corteCen){
                                      //    println "EL registro  de corte ya fue importado Solo actualizar"
                                          centralSql.executeUpdate(corte, configCorte.updateSql)
                                        }else{
                                    ///      println "El registro de corte no ha sido importado se debe importar"
                                          SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("instruccion_corte")
                                          def res=insert.execute(corte)
                                        }
                                       //   println "*** ************** ------ ++++++-------------------- CORTE "+corte

                                      }

                                  }


                              }
                          }
                      }

                      def aplicaciones=sql.rows("select * from aplicacion_de_cobro where cuenta_por_cobrar_id=?",[cxc.id])
                      aplicaciones.each{ aplicacion ->

                          def cobro=sql.firstRow("select * from cobro where id=?",[aplicacion.cobro_id])
                          if(cobro){
                        //    println "*** ************** ------ ++++++-------------------- COBRO "+cobro

                            def queryCobroCen="select * from cobro where id=?"
                            def cobroCen=centralSql.firstRow(queryCobroCen,[cobro.id])
                            def configCobro=EntityConfiguration.findByName('Cobro')
                            if(cobroCen){
                        //      println "EL registro  de cobro ya fue importado Solo actualizar"
                              centralSql.executeUpdate(cobro, configCobro.updateSql)
                            }else{
                          //    println "El registro de cobro no ha sido importado se debe importar"
                              SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cobro")
                              def res=insert.execute(cobro)
                            }

                          }
                        //  println "*** ************** ------ ++++++-------------------- APLICACION "+aplicacion

                            def queryAplicacionCen="select * from aplicacion_de_cobro where id=?"
                            def aplicacionCen=centralSql.firstRow(queryAplicacionCen,[aplicacion.id])
                            def configAplic=EntityConfiguration.findByName('AplicacionDeCobro')

                            if(aplicacionCen){
                        //      println "EL registro  de aplicacion ya fue importado Solo actualizar"
                              centralSql.executeUpdate(aplicacion, configAplic.updateSql)
                            }else{
                        //      println "El registro de aplicacion no ha sido importado se debe importar"
                              SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("aplicacion_de_cobro")
                              def res=insert.execute(aplicacion)
                            }
                      }
                  }
              }
          }
      }
   }
}

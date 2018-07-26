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
class ImportadorDeDevoluciones{

    @Autowired
    @Qualifier('dataSourceLocatorService')
    def dataSourceLocatorService
    @Autowired
    @Qualifier('dataSource')
    def dataSource
    @Autowired
    @Qualifier('replicaOperacionService')
    def replicaOperacionService

    def importar(){
        importar(new Date())
    }

    def importar(fecha){
    //  println ("Importando Devoluciones del : ${fecha.format('dd/MM/yyyy')}" )

      def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

        servers.each(){server ->

        //  println "***  Importando de Por ReplicaService: ${server.server} ******* ${server.url}****  "
          importarServerFecha(server,fecha)
        }
    }


    def importarSucursalFecha(nombreSuc,fecha){

      def server=DataSourceReplica.findByServer(nombreSuc)

    //  println "nombre: ${nombreSuc} fecha: ${fecha.format('dd/MM/yyyy')} URL: ${server.url} "

      importarServerFecha(server,fecha)

    }

    def importarServerFecha(server,fechaImpo){

      def fecha=fechaImpo.format('yyyy/MM/dd')

  //    println "Importando Por Server Fecha   "+fecha+ "   "+server.server
      def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
      def sqlSuc=new Sql(dataSourceSuc)
      def sqlCen=new Sql(dataSource)
      def configDevo= EntityConfiguration.findByName("DevolucionDeVenta")

      def configCobro= EntityConfiguration.findByName("Cobro")

      def queryDevSuc="select * from devolucion_de_venta where date(fecha)=?"

      def devolucionesSuc=sqlSuc.rows(queryDevSuc,[fechaImpo])

      def queryDevCen="select * from devolucion_de_venta where id=?"


      if(devolucionesSuc){

        devolucionesSuc.each{devoSuc ->

      //    println "Devosuc"+ devoSuc.id

            if(devoSuc.cobro_id){
                def queryCobro="Select * from cobro where id=?"
                def cobroSuc=sqlSuc.firstRow(queryCobro,[devoSuc.cobro_id])
                if(cobroSuc){
                  def cobroCen=sqlCen.firstRow(queryCobro,[devoSuc.cobro_id])
                  if(!cobroCen){
                    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cobro")
                     def res=insert.execute(cobroSuc)
                  }
                }
            }

            def devoCen=sqlCen.firstRow(queryDevCen,[devoSuc.id])
            if(devoCen){
        //       println "EL registro ya fue importado Solo actualizar devolucion maestro"
              sqlCen.executeUpdate(devoSuc, configDevo.updateSql)

            }else{
      //         println "El registro no ha sido importado se debe importar devolucion maestro"
               importadorDeVentaDevolucion(devoSuc.id,server)
              SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("devolucion_de_venta")
               def res=insert.execute(devoSuc)
            }

             importadorDeVentaDevolucion(devoSuc.id,server)

            def partidasSuc=sqlSuc.rows("Select * from devolucion_de_venta_det where devolucion_de_venta_id=?",[devoSuc.id])
            def configDevoDet=EntityConfiguration.findByName('DevolucionDeVentaDet')

            partidasSuc.each{devoSucDet ->
      //          println "DevosucDet"+devoSucDet.id

                def queryDevoDetCen="Select * from devolucion_de_venta_det where id=?"

                def devoDetCen=sqlCen.firstRow(queryDevoDetCen,devoSucDet.id)

                if(devoSucDet.inventario_id){
                  def queryInventSuc="Select * from inventario where id=?"
                  def inventSuc=sqlSuc.firstRow(queryInventSuc,devoSucDet.inventario_id)

                  if(inventSuc){
                      def queryInventCen="Select * from inventario where id=?"
                      def inventCen=sqlCen.firstRow(queryInventCen,inventSuc.id)
                      def configInvent=EntityConfiguration.findByName("Inventario")
                      if(inventCen){
        //                 println "EL registro ya de inventario fue importado Solo actualizar"
                        sqlCen.executeUpdate(inventSuc, configInvent.updateSql)

                      }else{
        //                 println "El registro de inventario no ha sido importado se debe importar"

                        SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("inventario")
                         def res=insert.execute(inventSuc)
                      }

                  }

                }

                if(devoDetCen){
        //           println "EL registro ya fue importado Solo actualizar"
                  sqlCen.executeUpdate(devoSucDet, configDevoDet.updateSql)

                }else{
          //         println "El registro no ha sido importado se debe importar"

                  SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("devolucion_de_venta_det")
                   def res=insert.execute(devoSucDet)
                }

          }
        }
      }


    }

    def importadorDeVentaDevolucion(devolucionId,server){
  //    println "Importando Por Server Fecha de Ventas "
      def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
      def sqlSuc=new Sql(dataSourceSuc)
      def sqlCen=new Sql(dataSource)

      def config= EntityConfiguration.findByName("Cfdi")

      def query = "SELECT f.* FROM  devolucion_de_venta d join venta v on (v.id=d.venta_id) join cuenta_por_cobrar c on (v.cuenta_por_cobrar_id=c.id) join  cfdi f on (f.id=c.cfdi_id) where d.id=?"
      def queryId="select * from cfdi where id=?"

      def cfdi=sqlSuc.firstRow(query,[devolucionId])
      if(cfdi){
        def cfdiCen=sqlCen.firstRow(queryId,[cfdi.id])

        if(cfdiCen){
    //       println "EL registro ya fue importado Solo actualizar"
          sqlCen.executeUpdate(cfdi, config.updateSql)

        }else{
      //     println "El registro no ha sido importado se debe importar"
          SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cfdi")
           def res=insert.execute(cfdi)
        }

        def cxcSuc=sqlSuc.firstRow("select * from cuenta_por_cobrar where cfdi_id=?",[cfdi.id])
        def configCxc=EntityConfiguration.findByName("CuentaPorCobrar")

        if(cxcSuc){
            def cxcCen=sqlCen.firstRow("select * from cuenta_por_cobrar where id=?",[cxcSuc.id])
          if(cxcCen){
    //          println "El registro de cxc ya fue importado solo se debe actulizar"
              sqlCen.executeUpdate(cxcSuc, configCxc.updateSql)
          }else{
    //         println "El registro de cxc no se ha importado se debe importar"
               SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cuenta_por_cobrar")
                def res=insert.execute(cxcSuc)
          }
        }


        def vtaSuc=sqlSuc.firstRow("select * from venta where cuenta_por_cobrar_id=?",[cxcSuc.id])

        if(vtaSuc){
            def vtaCen=sqlCen.firstRow("select * from venta where id=?",[vtaSuc.id])
            def configVta=EntityConfiguration.findByName("Venta")


            if(vtaCen){
      //        println "El registro de venta ya fue importado solo se debe actulizar  "+vtaSuc.id
                sqlCen.executeUpdate(vtaSuc, configVta.updateSql)
            }else{
        //      println "El registro de cxc no se ha importado se debe importar"
                SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("venta")
                def res=insert.execute(vtaSuc)
            }

            def queryVentaDet="select * from Venta_det where venta_id=?"
            def ventasDet=sqlSuc.rows(queryVentaDet,[vtaSuc.id])

                if(ventasDet){
                    ventasDet.each{ ventaDet ->
          //              println "*** ************** ------ ++++++-------------------- VENTADET "+ventaDet
                        if(ventaDet.inventario_id){
        //                  println "*** ************** ------ ++++++-------------------- VENTADET "
                           def queryInv="select * from inventario where id = ?"
                            def invent=sqlSuc.firstRow(queryInv,[ventaDet.inventario_id])
                            def queryInvCen="select * from inventario where id=?"
                            def inventCen=sqlCen.firstRow(queryInvCen,[invent.id])
                            def configInv=EntityConfiguration.findByName("Inventario")
                           if(inventCen){

          //                      println "EL registro  de inventario ya fue importado Solo actualizar"
                                sqlCen.executeUpdate(invent, configInv.updateSql)
                            }else{
              //                println "El registro de inventario  no ha sido importado se debe importar"
                              SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("inventario")
                              def res=insert.execute(invent)
                            }
                        }

                        def queryVentaDetCen="select * from venta_det where id=?"
                        def ventaDetCen=sqlCen.firstRow(queryVentaDetCen,[ventaDet.id])
                        def configVentaDet=EntityConfiguration.findByName('VentaDet')
                        if(ventaDetCen){
            //              println "EL registro  de ventaDet ya fue importado Solo actualizar"
                          sqlCen.executeUpdate(ventaDet, configVentaDet.updateSql)
                        }else{
              //            println "El registro de ventaDet no ha sido importado se debe importar"
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
              //              println "EL registro  de corte ya fue importado Solo actualizar"
                            sqlCen.executeUpdate(corte, configCorte.updateSql)
                          }else{
                //            println "El registro de corte no ha sido importado se debe importar"
                            SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("instruccion_corte")
                            def res=insert.execute(corte)
                          }
                         //   println "*** ************** ------ ++++++-------------------- CORTE "+corte

                        }


                    }
                }
        }


      }



    }


}

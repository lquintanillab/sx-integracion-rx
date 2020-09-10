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
class ImportadorDeCancelaciones{

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
    println ("Importando Cancelaciones del : ${fecha.format('dd/MM/yyyy')}" )

    def servers=DataSourceReplica.findAllByActivaAndCentralAndSucursal(true,false,true)

      def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

      servers.each(){server ->

        println "***  Importando Cancelaciones: ${server.server} ******* ${server.url}****  "
        importarServerFecha(server,fecha)
      }
  }


  def importarSucursalFecha(nombreSuc,fecha){

    def server=DataSourceReplica.findByServer(nombreSuc)
    println  "*************************************************************"
    println "nombre: ${nombreSuc} fecha: ${fecha.format('dd/MM/yyyy')} URL: ${server.url} "


    importarServerFecha(server,fecha)

  }

  def importarServerFecha(server,fechaImpo){

    def fecha=fechaImpo.format('yyyy/MM/dd')

    println "//////////////////////////////////////////////////////////////////////Importando Por Server Fecha   "+server.server +" --  "+fecha+ "   "+server.server
    def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
    def sqlSuc=new Sql(dataSourceSuc)
    def sqlCen=new Sql(dataSource)
    def configCxc= EntityConfiguration.findByName("CuentaPorCobrar")

    def queryCancelacionSuc="select * from cuenta_por_cobrar where cancelada=?"
    //def queryCancelacionSuc="select * from cfdi_cancelado where date(date_created)='2018/04/10'"

    def cancelacionesSuc=sqlSuc.rows(queryCancelacionSuc,[fecha])
    //def cancelacionesSuc=sqlSuc.rows(queryCancelacionSuc)

    cancelacionesSuc.each{cancelacion ->
        println "-Cancelacionoooooooooo- "+cancelacion.id
          def queryCancelacionCen="select * from cuenta_por_cobrar where id=? "

          def cancelacionCen=sqlCen.firstRow(queryCancelacionCen,[cancelacion.id])

           def configCfdi= EntityConfiguration.findByName("Cfdi")

           def cfdiCen=sqlCen.firstRow("select * from cfdi where id=?",[cancelacion.cfdi_id])

          
          if(cfdiCen){

                println "UUID encontrado para cancelar"+cfdiCen.id

                  def cfdiSuc=sqlSuc.firstRow("select * from cfdi where uuid=?",[cancelacion.uuid])
                  println "UUID "+cfdiSuc.id
                  sqlCen.executeUpdate(cfdiSuc, configCfdi.updateSql)

          }
/*
          if(!cfdiCen){

            println "Importando Cancelacion de Cfdi"+cancelacion.uuid
            SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cfdi_cancelado")
            def res=insert.execute(cancelacion)

            }
*/
            def queryCxcCen="Select * from cuenta_por_cobrar where uuid=?"

            def queryCxcSuc="select * from cuenta_por_cobrar where id=?"

            def cxcCen=sqlCen.firstRow(queryCxcCen,[cancelacion.uuid])

            if(cxcCen){
              println "Actualizando cuenta_por_cobrar de "+cancelacion.uuid
              def cxcCenId=cxcCen.id
              println "-/***************************************************/-"+cxcCen.id
              def cxcSuc=sqlSuc.firstRow(queryCxcSuc,[cxcCen.id])
              sqlCen.executeUpdate(cxcSuc, configCxc.updateSql)

              def queryVenta="select * from venta where cuenta_por_cobrar_id=?"
              def queryAplicacion="select * from aplicacion_de_cobro where cuenta_por_cobrar_id=?"
              def queryCobro="select * from cobro where id=?"

              def ventaCen=sqlCen.firstRow(queryVenta,[cxcCenId])

                      println "-/***************************************************/-"+cxcCenId
                       println "-/***************************************************/-"+ventaCen

              if(ventaCen){
                  println "-/***************************************************/-"+ventaCen.id
                def ventaSuc=sqlSuc.firstRow("Select * from venta where id=?",[ventaCen.id])
                if(ventaSuc){
                                    println "-/***************************************************/-"+ventaCen.id
                def configVenta=EntityConfiguration.findByName("Venta")
                sqlCen.executeUpdate(ventaSuc,configVenta.updateSql)

                def queryventasDet="select * from venta_det where venta_id=?"

                def ventasDet= sqlCen.rows(queryventasDet,[ventaCen.id])

                ventasDet.each{ventaDet ->
                                       println "-/********************Ventas DEt*******************************/-"+ventaDet.id
                  def inventarioID=ventaDet.inventario_id

                  def queryDet="select * from venta_det where id=?"
                  def detSuc=sqlSuc.firstRow(queryDet,[ventaDet.id]);

                  def configDetSuc=EntityConfiguration.findByName("VentaDet")
                  sqlCen.executeUpdate(detSuc,configDetSuc.updateSql)

                  def queryInventario="select * from inventario where id=?"

                  def inventario =sqlCen.firstRow(queryInventario,[inventarioID])

                  if(inventario){
                      sqlCen.execute("Delete from inventario where id=?",inventarioID)
                  }

                }
               

                }
              }

              def aplicacionesCen=sqlCen.rows(queryAplicacion,[cxcCen.id])

              aplicacionesCen.each{ aplicacionCen ->
                  def cobroId=aplicacionCen.cobro_id
                  sqlCen.execute("delete from aplicacion_de_cobro where id=?",[aplicacionCen.id]);
                  def cobro=sqlCen.firstRow(queryCobro,[cobroId])
                  if(cobro){
                    if(cobro.forma_de_pago == 'EFECTIVO' || cobro.forma_de_pago == 'TARJETA_CREDITO' || cobro.forma_de_pago == 'TARJETA_DEBITO' || cobro.forma_de_pago == 'CHEQUE' || cobro.forma_de_pago == 'PAGO_DIF' ){
                      def aplicacionesCobro=sqlCen.rows("select * from aplicacion_de_cobro where cobro_id=?",[cobro.id])
                      if(aplicacionesCobro.size() == 0){
                        sqlCen.execute("delete from cobro where id=?",[cobro.id])
                      }
                    }    
                  }
              }
          }


    }


  }

}

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
  //  println ("Importando Cancelaciones del : ${fecha.format('dd/MM/yyyy')}" )

    def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

      def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

      servers.each(){server ->

      //  println "***  Importando de Por ReplicaService: ${server.server} ******* ${server.url}****  "
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

    println "Importando Por Server Fecha   "+server.server +" --  "+fecha+ "   "+server.server
    def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
    def sqlSuc=new Sql(dataSourceSuc)
    def sqlCen=new Sql(dataSource)
    def configCxc= EntityConfiguration.findByName("CuentaPorCobrar")

    def queryCancelacionSuc="select * from cfdi_cancelado where date(date_created)=?"
    //def queryCancelacionSuc="select * from cfdi_cancelado where date(date_created)='2018/04/10'"

    def cancelacionesSuc=sqlSuc.rows(queryCancelacionSuc,[fechaImpo])
    //def cancelacionesSuc=sqlSuc.rows(queryCancelacionSuc)

    cancelacionesSuc.each{cancelacion ->
      //  println "-- "+cancelacion.id
          def queryCancelacionCen="select * from cfdi_cancelado where id=? "

          def cancelacionCen=sqlCen.firstRow(queryCancelacionCen,[cancelacion.id])

          if(!cancelacionCen){

        //    println "Importando Cancelacion de Cfdi"+cancelacion.uuid
            SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cfdi_cancelado")
            def res=insert.execute(cancelacion)

            }

            def queryCxcCen="Select * from cuenta_por_cobrar where uuid=?"

            def queryCxcSuc="select * from cuenta_por_cobrar where id=?"

            def cxcCen=sqlCen.firstRow(queryCxcCen,[cancelacion.uuid])

            if(cxcCen){
          //    println "Actualizando cuenta_por_cobrar de "+cancelacion.uuid
              def cxcSuc=sqlSuc.firstRow(queryCxcSuc,[cxcCen.id])
              sqlCen.executeUpdate(cxcSuc, configCxc.updateSql)

              def queryVenta="select * from venta where cuenta_por_cobrar_id=?"
              def queryAplicacion="select * from aplicacion_de_cobro where cuenta_por_cobrar_id=?"
              def queryCobro="select * from cobro where id=?"

              def ventaCen=sqlCen.firstRow(queryVenta,[cxcCen.id])

              if(ventaCen){

                def ventaSuc=sqlSuc.firstRow(queryVenta,[cxcCen.id])
                if(ventaSuc){
                                    println "--"+ventaCen.id
                def configVenta=EntityConfiguration.findByName("Venta")
                sqlCen.executeUpdate(ventaSuc,configVenta.updateSql)

                def queryventasDet="select * from venta_det where venta_id=?"

                def ventasDet= sqlCen.rows(queryventasDet,[ventaCen.id])

                ventasDet.each{ventaDet ->

                  def inventarioID=ventaDet.inventario_id

                  def queryDet="select * from venta_det where id=?"
                  def detSUC=sqlSuc.firstRow(queryDet,[ventaDet.id]);

                  def configDetSuc=EntityConfiguration.findByName("VentaDet")
                  sqlCen.executeUpdate(detSuc,configDetSuc.updateSql)

                  def queryinventario="select * from inventario where id=?"

                  def inventario =sqlCen.firstRow(queryInventario,[inventarioId])

                  if(inventario){
                      sqlCen.execute("Delete from inventario where id=?",inventarioID)
                  }

                }
               

                }
              }

              def aplicacionesCen=sqlCen.rows(queryAplicacion,[cxcCen.id])

              aplicacionesCen.each{ aplicacionCen ->
                  def cobroId=aplicacionCen.cobro_id
                  sqlCen.execute("delete * from aplicacion_de_cobro where cobro_id=?",[cobroId]);
                  sqlCen.execute("delete * from cobro where id=?",[cobroId])

              }




          }


    }


  }

}

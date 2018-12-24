package sx.importacion

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
class ImportadorDeCompras {


    @Autowired
     @Qualifier('dataSourceLocatorService')
    def dataSourceLocatorService
    @Autowired
    @Qualifier('dataSource')
    def dataSource

  def importar(){
    def fecha= new Date()

    importar(fecha)

    }

    def importar(fecha){
     // println ("Importando Compras del : ${fecha.format('dd/MM/yyyy')}" )

      def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

        servers.each(){server ->
        //  println "***  Importando de Por ReplicaService: ${server.server} ******* ${server.url}****  "
          importarServerFecha(server,fecha)
        }
    }



    def importarSucursalFecha(nombreSuc,fecha){

      def server=DataSourceReplica.findByServer(nombreSuc)

    //  println "nombre: ${nombreSuc} fecha: ${fecha.format('dd/MM/yyyy')} URL: ${server} "

      importarServerFecha(server,fecha)

    }

  def importarServerFecha(server,fechaImpo){

        def fecha=fechaImpo.format('yyyy/MM/dd')
        println "Importando Por Server"+ server.server+" Fecha"+fecha
          def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
          def sqlSuc=new Sql(dataSourceSuc)
          def sqlCen=new Sql(dataSource)

          def sucursal=Sucursal.findByNombre(server.server)


          importarOp(sqlSuc,sqlCen,fecha,sucursal,queryCompras,queryCompra,'compra')

          importarOp(sqlSuc,sqlCen,fecha,sucursal,queryComprasDet,queryCompraDet,'compraDet')

          importarOp(sqlSuc,sqlCen,fecha,sucursal,queryInventarios,queryInventario,'inventario')

          importarOp(sqlSuc,sqlCen,fecha,sucursal,queryRecepciones,queryRecepcion,'recepcionDeCompra')

          importarOp(sqlSuc,sqlCen,fecha,sucursal,queryRecepcionesDet,queryRecepcionDet,'recepcionDeCompraDet')
  
  }

  def importarOp(sqlSuc,sqlCen,fecha,sucursal,queryOperaciones,queryOperacion,entity){

     println "***** Importando ${entity} *****"

    def config=EntityConfiguration.findByName(entity)

        def params=[]

      if (entity=='compra' || entity == 'compraDet'){
       // println "Es una compra o  compraDet"
          params=[sucursal.id,fecha,fecha]
      }else{
        
          params=[fecha,sucursal.id]

      }

          def operaciones=sqlSuc.rows(queryOperaciones,params)

        operaciones.each{ operacionSuc ->

            println "*********************------------- *********************************************************"+operacionSuc.id

            def operacionCen=sqlCen.firstRow(queryOperacion,[operacionSuc.id])

            if(!operacionCen){
                println "La opoeracion no existe, importar:  "+operacionSuc.id
               // try{
                    SimpleJdbcInsert insert= new SimpleJdbcInsert(dataSource).withTableName(config.tableName)
                    def res=insert.execute(operacionSuc)
             /*   }catch(Exception e){
                    e.printStackTrace()
                }*/
            }else{
              if( entity=='compra' && operacionCen.last_updated < operacionSuc.last_updated  ){
                  println "Compra Actualizada "
                   sqlCen.executeUpdate(operacionSuc, config.updateSql)
              }
                 
            }

        }
    
  }

  
String queryCompras="Select * from compra where cerrada is not null  and sucursal_id=? and centralizada is false and (date(cerrada)=? or date(last_updated)=?)"

String queryCompra="select * from compra where id=?"

String queryComprasDet="select * from compra_det where compra_id in (Select id from compra where cerrada is not null and sucursal_id=? and (date(cerrada)=? or date(last_updated)=?))"

String queryCompraDet="select * from compra_det  where id=? "

String queryInventarios="select * from inventario where tipo='COM' and date(fecha)=? and sucursal_id=?"

String queryInventario="select * from inventario where id=?"

String queryRecepciones="select * from recepcion_de_compra where date(fecha)=? and sucursal_id=?"

String  queryRecepcion="select * from recepcion_de_compra where  id=?"

String queryRecepcionesDet="select * from recepcion_de_compra_det where recepcion_id in (select id from recepcion_de_compra where date(fecha)=? and sucursal_id=?)"

String queryRecepcionDet="select * from recepcion_de_compra_det where id=?"

}

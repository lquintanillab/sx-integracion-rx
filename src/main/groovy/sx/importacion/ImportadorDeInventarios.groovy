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
class ImportadorDeInventarios{

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

    def servers=DataSourceReplica.findAllByActivaAndCentralAndSucursal(true,false,true)

      def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

      servers.each(){server ->

        println "***  Importando de Por ReplicaService: ${server.server} ******* ${server.url}****  "
        importarServerFecha(server,fecha)
      }
  }
 

  def importarSucursalFecha(nombreSuc,fecha){

    def server=DataSourceReplica.findByServer(nombreSuc)


    importarServerFecha(server,fecha)

  }

  def importarServerFecha(server,fecha){

    def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
    def sqlSuc=new Sql(dataSourceSuc)
    def sqlCen=new Sql(dataSource)

    def configInv= EntityConfiguration.findByName("Inventario")

    def queryInv="select * from inventario where tipo not in ('RMD','TPS','FAC','TPE','COM') AND DATE(FECHA)=? "

    def inventarios=sqlSuc.rows(queryInv,[fecha])
    inventarios.each{ inventarioSuc ->
          def queryInvId="select *  from inventario where id=?"

          def invCen=sqlCen.firstRow(queryInvId,[inventarioSuc.id])

          try{

              if(!invCen){
        
                SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("inventario")
                def res=insert.execute(inventarioSuc)

              }else{
          
                sqlCen.executeUpdate(inventarioSuc, configInv.updateSql)
              }
          }catch(Exception e){
            e.printStackTrace()
          }

            def table=""
          switch(inventarioSuc.tipo) {
            case 'TRS':
            case 'REC':
                  table='transformacion'
            break
            case 'DEC':
                  table='devolucion_de_compra'

            break
            case 'MER':
            case 'CIS':
            case 'VIR':
            case 'CIM':
            case 'OIM':
            case 'RMC':
                    table='movimiento_de_almacen'
            break
            default:

            break
          }
    
            def tableDet=table+'_det'
            def queryPartida="select * from ${tableDet} where inventario_id=?"

          def partidaSuc=sqlSuc.firstRow(queryPartida,[inventarioSuc.id])

          if(partidaSuc){


            if(partidaSuc.partidas_idx == 0){
            

              def queryMovId="Select distinct(m.id) as id from  ${table} m join ${table}_det d on (m.id=d.${table}_id) join inventario i on (i.id=d.inventario_id) where i.id=?"
           
              def movId=sqlSuc.firstRow(queryMovId,[inventarioSuc.id])

              def queryMov="select * from ${table} where id=?"

              def moviSuc=sqlSuc.firstRow(queryMov,[movId.id])
              if(moviSuc){
          
                def moviCen=sqlCen.firstRow(queryMov,[movId.id])
                try{
                    if(!moviCen){
                       // println "El movimiento no esta se va a importar"
                      SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName(table)
                      def res=insert.execute(moviSuc)
                    }else{
                      def config= EntityConfiguration.findByTableName(table)
                      sqlCen.executeUpdate(moviSuc, config.updateSql)
                    }
                }catch(Exception e){
                  e.printStackTrace()
                }

              }
            }

            def queryPartidaCen="select * from ${tableDet} where id=?"
            def partidaCen=sqlCen.firstRow(queryPartidaCen,[partidaSuc.id])
            
           // println queryPartida+"  -------------- "+tableDet+" --------- "+partidaSuc.partidas_idx+"  ------ "+partidaSuc.id+" ------- "+partidaCen
            
            try{
                if(!partidaCen){
         
                SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName(tableDet)
                def res=insert.execute(partidaSuc)
                }else{
       
                  def configDet= EntityConfiguration.findByTableName(tableDet)
                  sqlCen.executeUpdate(partidaSuc, configDet.updateSql)

                }
            }catch(Exception e){
              e.printStackTrace()
            }



          }


    }
  }

}

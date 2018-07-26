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
class ImportadorDeFichas{


  @Autowired
   @Qualifier('dataSourceLocatorService')
  def dataSourceLocatorService
  @Autowired
  @Qualifier('dataSource')
  def dataSource

      def importar(){
          importar(new Date())
      }
      def importar(fecha){
    //    println ("Importando Fichas del : ${fecha.format('dd/MM/yyyy')}" )

        def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

          def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

          servers.each(){server ->

    //        println "***  Importando de Por ReplicaService: ${server.server} ******* ${server.url}****  "
            importarServerFecha(server,fecha)
          }
      }


      def importarSucursalFecha(nombreSuc,fecha){

        def server=DataSourceReplica.findByServer(nombreSuc)

    //    println "Importando Fichas nombre: ${nombreSuc} fecha: ${fecha.format('dd/MM/yyyy')} URL: ${server} "

      importarServerFecha(server,fecha)

      }

      def importarServerFecha(server,fecha){
  //      println "Importando Por Server Fecha de Fichas  "
        def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
        def sqlSuc=new Sql(dataSourceSuc)
        def sqlCen=new Sql(dataSource)

        def config= EntityConfiguration.findByName("Ficha")

        def query = "select * from ficha where fecha=?"
        def queryId="select * from ficha where id=?"

        sqlSuc.rows(query,[fecha.format('yyyy/MM/dd')]).each{row ->

            def found=sqlCen.firstRow(queryId,[row.id])

            if(found){
        //       println "EL registro ya fue importado Solo actualizar"
              sqlCen.executeUpdate(row, config.updateSql)

            }else{
        //       println "El registro no ha sido importado se debe importar"
              SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("ficha")
               def res=insert.execute(row)
            }


        }

      }

  }

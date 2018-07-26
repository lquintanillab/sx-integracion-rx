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
class ImportadorDeCobros{


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
      //  println ("Importando Cobros del : ${fecha.format('dd/MM/yyyy')}" )

        def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

          def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

          servers.each(){server ->
        //    println "***  Importando de Por ReplicaService: ${server.server} ******* ${server.url}****  "
            importarServerFecha(server,fecha)
          }
      }


      def importarSucursalFecha(nombreSuc,fecha){

        def server=DataSourceReplica.findByServer(nombreSuc)

      //  println "nombre: ${nombreSuc} fecha: ${fecha.format('dd/MM/yyyy')} URL: ${server} "

      importarServerFecha(server,fecha)

      }

      def importarServerFecha(server,fecha){
      //  println "Importando Por Server Fecha"
        def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
        def sqlSuc=new Sql(dataSourceSuc)
        def sqlCen=new Sql(dataSource)

        def config= EntityConfiguration.findByName("Cobro")

        def query = "select * from cobro where fecha=? or date(primera_aplicacion)=?"
        def queryId="select * from cobro where id=?"

        sqlSuc.rows(query,[fecha.format('yyyy/MM/dd'),fecha.format('yyyy/MM/dd')]).each{row ->

            def found=sqlCen.firstRow(queryId,[row.id])

            if(found){
          //     println "EL registro ya fue importado Solo actualizar"
              sqlCen.executeUpdate(row, config.updateSql)

            }else{
          //     println "El registro no ha sido importado se debe importar"
              SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cobro")
               def res=insert.execute(row)
            }

            switch(row.forma_de_pago) {
              case 'CHEQUE':

                def configChe= EntityConfiguration.findByName("CobroCheque")
                def queryChe="select * from cobro_cheque where cobro_id=?"
                def queryCheCen="select * from cobro_cheque where id=?"
                def cheque=sqlSuc.firstRow(queryChe,[row.id])
                if(cheque){
                  def foundChe=sqlCen.firstRow(queryCheCen,cheque.id)
                  if(foundChe){
                  //   println "EL registro del cheque ya fue importado Solo actualizar"
                    sqlCen.executeUpdate(cheque, configChe.updateSql)

                  }else{
                  //   println "El registro del cheque no ha sido importado se debe importar"
                    SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cobro_cheque")
                     def res=insert.execute(cheque)
                  }
                }


              break

              case 'TARJETA_CREDITO':
              case 'TARJETA_DEBITO':

              def configTar= EntityConfiguration.findByName("CobroTarjeta")
              def queryTar="select * from cobro_tarjeta where cobro_id=?"
              def queryTarCen="select * from cobro_tarjeta where id=?"
              def tarjeta=sqlSuc.firstRow(queryTar,[row.id])
              def foundTar=sqlCen.firstRow(queryTarCen,tarjeta.id)
              if(foundTar){
              //   println "EL registro de la tarjeta ya fue importado Solo actualizar"
                sqlCen.executeUpdate(tarjeta, configTar.updateSql)

              }else{
              //   println "El registro de la tarjeta no ha sido importado se debe importar"
                SimpleJdbcInsert insert=new SimpleJdbcInsert(dataSource).withTableName("cobro_tarjeta")
                 def res=insert.execute(tarjeta)
              }

              default:

              break
            }

        }

      }

      def importarAplicacionDeCobro(){

      }

  }

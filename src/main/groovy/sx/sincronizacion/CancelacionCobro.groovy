package sx.sincronizacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier
import groovy.sql.Sql
import sx.CuentaPorCobrar
import sx.Cobro
import sx.DataSourceReplica
import sx.EntityConfiguration
import sx.Sucursal

@Component
class CancelacionCobro{

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
        println ("Importando Cancelaciones de Cobro del : ${fecha.format('dd/MM/yyyy')}" )

        def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

        servers.each(){server ->

           // println "***  Importando Cancelaciones de Cobro: ${server.server} ******* ${server.url}****  "
            importarServerFecha(server,fecha)
        }
    }

    def importarServerFecha(server,fechaImpo){

       println "Importando por server y fecha"+server.server


        def fecha=fechaImpo.format('yyyy/MM/dd')

        def sucursal = Sucursal.findByNombre(server.server)

        println "**********"+sucursal.nombre

        def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
        def sqlSuc=new Sql(dataSourceSuc)
        def sqlCen=new Sql(dataSource)
        def configCxc= EntityConfiguration.findByName("Cobro")

        def cobros = sqlCen.rows(queryCobro,[sucursal.id,fecha,sucursal.id,fecha])

        cobros.each{ cobro ->
            

            def cobroSuc = sqlSuc.firstRow("select * from cobro where id = ?",[cobro.id])

            if(!cobroSuc){
                println "Cobro a cancelar:  "+cobro.id+"  --- "+cobro.fecha +"  --- "+cobro.forma_de_pago  

                switch(cobro.forma_de_pago) {
                    case 'PAGO_DIF':
                    case 'EFECTIVO':
                         println "El cobro es efectivo o pago dif solo borrar el cobro"
                    break

                    case 'TARJETA_CREDITO':
                    case 'TARJETA_DEBITO':

                        println "El cobro es  tarjeta  buscar el cobro tarjeta"

                        def cobroTarjeta = sqlCen.firstRow("select * from cobro_tarjeta where cobro_id = ?",[cobro.id])

                        if(cobroTarjeta){
                                 println "El cobro tarjeta fue encontrado borralo"
                                 sqlCen.execute("Delete from cobro_tarjeta where cobro_id=?",[cobro.id])
                        }else{
                                println "El cobro tarjeta no fue encontrado solo borra el cobro"
                        }

                    break 

                    case 'CHEQUE':
                        
                        println "El cobro es  cheque  buscar el cobro cheque"

                        def cobroCheque = sqlCen.firstRow("select * from cobro_cheque where cobro_id = ?",[cobro.id])

                        if(cobroCheque){
                                 println "El cobro chequefue encontrado borralo"
                                 sqlCen.execute("Delete from cobro_cheque where cobro_id=?",[cobro.id])
                        }else{
                                println "El cobro cheque no fue encontrado solo borra el cobro"
                        }

                    break
                    
                }

                sqlCen.execute("Delete from cobro where id=?",[cobro.id])

            }else{
                println "El cobro ${cobroSuc.id} si esta en la sucursal "
            }

        }

    }


    def queryCobro="""
        SELECT c.* FROM cobro c left join aplicacion_de_cobro a on (a.cobro_id=c.id)
        where c.forma_de_pago in('PAGO_DIF','EFECTIVO','TARJETA_CREDITO','TARJETA_DEBITO') and a.id is null and sucursal_id = ? and c.fecha = ?
        union
        SELECT c.* FROM cobro c left join aplicacion_de_cobro a on (a.cobro_id=c.id) join cobro_cheque o on (o.cobro_id=c.id)
        where c.forma_de_pago in('CHEQUE') and a.id is null and sucursal_id = ? and c.fecha = ? and o.cambio_por_efectivo is false
"""

}



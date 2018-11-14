package sx.sincronizacion


import sx.utils.Periodo
import groovy.sql.Sql
import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.stereotype.Component
import sx.VentaPorFacturista
import sx.Sucursal
import java.sql.SQLException
import sx.DataSourceReplica
import sx.EntityConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@Component
class VentaPorFacturistaIntegration {

    @Autowired
    @Qualifier('dataSourceLocatorService')
    def dataSourceLocatorService
    @Autowired
    @Qualifier('dataSource')
    def dataSource

    static String SQL_COMMAND = """
        SELECT fecha,sucursal_id,sucursalNom, create_user as createUser,facturista
        ,sum(con) con,sum(cod) cod,sum(cre) as cre,sum(canc) canc,sum(devs) devs,sum(facs) facs,sum(importe) importe,sum(partidas) partidas,sum(ped_fact) pedFact
        ,sum(ped) ped,sum(pedMosCON) pedMosCON,sum(pedMosCOD) pedMosCOD,sum(pedMosCRE) pedMosCRE,sum(pedTelCON) pedTelCON,sum(pedTelCOD) pedTelCOD,sum(pedTelCRE) pedTelCRE
        FROM (
        SELECT 'PED' as tipo,fecha,sucursal_id,(SELECT s.nombre FROM sucursal s WHERE v.sucursal_id=s.id) as sucursalNom,create_user
        ,(SELECT u.nombre FROM user u where v.create_user=u.username) as facturista
        ,0 con,0 cod,0 cre,0 canc,0 devs,0 facs,0 importe,0 partidas
        ,SUM(case when v.facturar is not null and v.facturar_usuario <> v.update_user then 1 else 0 end) as ped_fact
        ,count(*) as ped
        ,SUM(case when v.tipo = 'CON' and v.atencion not like 'TEL%' then 1 else 0 end) as pedMosCON
        ,SUM(case when v.tipo = 'CON' and v.cod is true and v.atencion not like 'TEL%' then 1 else 0 end) as pedMosCOD
        ,SUM(case when v.tipo = 'CRE' and v.atencion not like 'TEL%' then 1 else 0 end) as pedMosCRE
        ,SUM(case when v.tipo = 'CON' and v.atencion like 'TEL%' then 1 else 0 end) as pedTelCON
        ,SUM(case when v.tipo = 'CON' and v.cod is true and v.atencion like 'TEL%' then 1 else 0 end) as pedTelCOD
        ,SUM(case when v.tipo = 'CRE' and v.atencion like 'TEL%' then 1 else 0 end) as pedTelCRE
         FROM VENTA V WHERE FECHA='%FECHA%' and sucursal_id not in ('402880fc5e4ec411015e4ec6512a0136','402880fc5e4ec411015e4ec652710139')
         GROUP BY fecha,sucursal_id,create_user
        union
        SELECT 'FAC' as tipo,fecha,sucursal_id,(SELECT s.nombre FROM sucursal s WHERE v.sucursal_id=s.id) as sucursalNom,update_user
        ,(SELECT u.nombre FROM user u where v.update_user=u.username) as facturista
        ,SUM(case when v.tipo = 'CON' then 1 else 0 end) as con
        ,SUM(case when v.tipo = 'COD' then 1 else 0 end) as cod
        ,SUM(case when v.tipo = 'CRE' then 1 else 0 end) as cre
        ,SUM(case when v.cancelada is not null then 1 else 0 end) as canc
        ,(SELECT count(*) FROM devolucion_de_venta d join venta x on(d.venta_id=x.id) where d.parcial is false and x.cuenta_por_cobrar_id is not null and x.update_user = v.update_user and date(d.fecha)='%FECHA%') as devs
        ,count(*) as facs,sum(subtotal * tipo_de_cambio) as importe
        ,(SELECT count(*) FROM venta_det d join venta x on(d.venta_id=x.id) join cuenta_por_cobrar z on(x.cuenta_por_cobrar_id=z.id) where z.cfdi_id is not null and z.cancelada is null and x.update_user = v.update_user and date(z.fecha)='%FECHA%') as partidas
        ,0 as ped_fact,0 ped,0 pedMosCON,0 pedMosCOD,0 pedMosCRE,0 pedTelCON,0 pedTelCOD,0 pedTelCRE
        FROM cuenta_por_cobrar V WHERE cfdi_id is not null and sw2 is null and FECHA='%FECHA%' and sucursal_id not in ('402880fc5e4ec411015e4ec6512a0136','402880fc5e4ec411015e4ec652710139')
        GROUP BY fecha,sucursal_id,update_user
        ) AS A
        GROUP BY
        fecha,sucursal_id,create_user
    """
 

    String getCommand(Date fecha) {
        return SQL_COMMAND.replaceAll("%FECHA%", fecha.format('yyyy-MM-dd'))
    }

    def actualizar(){
        actualizar(new Date())
    }

    def actualizar(fecha){
        println ("Importando Ventas por facturista del : ${fecha.format('dd/MM/yyyy')}" )

           deleteRecords(fecha)

        def servers=DataSourceReplica.findAllByActivaAndCentral(true,false)

        def central=DataSourceReplica.findAllByActivaAndCentral(true,true)

        servers.each(){server ->
         
           // println "***  Importando Ventas por Facturista: ${server.server} ******* ${server.url}****  "
           ///
           if(server.server != 'SOLIS' && server.server != 'VERTIZ 176' ){
                 actualizar(server,fecha)
           }
               
        }
    }

    def actualizar(server,fecha){
         def dataSourceSuc=dataSourceLocatorService.dataSourceLocatorServer(server)
        def sqlSuc=new Sql(dataSourceSuc)
        def sqlCen=new Sql(dataSource)
        def sucursal = Sucursal.findByNombre(server.server)

        try {
            println "*********  Importando Ventas por Facturista: ${server.server} ******* ${server.url}****  "
            def ventas = sqlSuc.rows(getCommand(fecha))
            ventas.each { row ->
            println row
                VentaPorFacturista vta = new VentaPorFacturista()
                vta.properties = row
                vta.sucursal = sucursal
                vta.save failOnError: true, flush: true
           }
        }catch (Exception e){
            def message = ExceptionUtils.getRootCauseMessage(e)
            
        }
    }



    def actualizarMTD() {
        Date fechaInicial = Periodo.getCurrentMonth().fechaInicial
        Date fechaFinal = new Date() - 1
        (fechaInicial..fechaFinal).each {
            actualizar(it)
        }
    }


    def deleteRecords(Date fecha) {
        def res = VentaPorFacturista.executeUpdate("delete VentaPorFacturista v where date(v.fecha)=?",[fecha])
        println(' registros eliminando para '+ fecha.format('dd/MM/yyyy'))

    }

}

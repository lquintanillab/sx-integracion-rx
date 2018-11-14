package sx.sincronizacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier
import sx.CuentaPorCobrar
import sx.Cliente
import sx.ClienteCredito
import sx.Cobro
import sx.NotaDeCredito
import sx.AplicacionDeCobro
import sx.AuditLog

@Component
class ActualizacionCredito{

  @Autowired
  @Qualifier('dataSource')
  def dataSource

def actualizarSaldo(){
  def clientesCre=ClienteCredito.findAll()

    clientesCre.each{clienteCre ->

    def cliente= clienteCre.cliente
    def cuentas=CuentaPorCobrar.findAllByClienteAndTipo(cliente,'CRE')

    def totalCxc=0
    def totalMoratorios=0

    cuentas.each{cuenta ->

              def semanas=0
              def semanasD=0
              def dias=0
              def moratorios=0

              def hoy=new Date()
              def vto=cuenta.vencimiento?:cuenta.fecha+clienteCre.plazo.intValue()


              if(vto<hoy){

                  semanas=((hoy-vto)/7).toDouble().trunc()
                  semanasD=((hoy-vto)/7)
                  dias=hoy-vto
              }
              else
                  semanas=0.0

              def saldo=cuenta.getSaldo()

              if(saldo!=0){
                  moratorios=cuenta.total*(semanas/100)
              }
              totalMoratorios=totalMoratorios+moratorios
              totalCxc=totalCxc+saldo
          }

           def notas=NotaDeCredito.findAllByClienteAndTipoCartera(cliente,'CRE')

          def totalNotas=0

          notas.each{nota ->
              def cobro=nota.cobro
              if(cobro){
                def aplicaciones=cobro.aplicaciones.sum{it.importe}?:0
                def saldo=cobro.importe-aplicaciones
                  totalNotas=totalNotas+saldo
              }
          }

          def saldoReal=totalCxc+totalMoratorios-totalNotas

      //if(saldoReal!=0)
         // println cliente.nombre+"|"+saldoReal

      if(saldoReal<0)
        saldoReal=0

      clienteCre.saldo=saldoReal

      //println cliente.nombre+"   "+saldoReal

      clienteCre.save(failOnError: true, flush:true)

      /*

      def audit=new AuditLog()

      audit.persistedObjectId=clienteCre.id
      audit.target='CENTRAL'
      audit.dateCreated=audit.lastUpdated=new Date()
      audit.name='ClienteCredito'
      audit.tableName='cliente_credito'
      audit.eventName='UPDATE'
      audit.source='NA'

      audit.save(failOnError: true, flush:true)
*/
    }

}

def actualizarAtraso(){
  def clientesCre=ClienteCredito.findAll()


clientesCre.each{clienteCre ->
    def cliente= clienteCre.cliente
    def cuentas=CuentaPorCobrar.findAllByClienteAndTipo(cliente,'CRE')
    def atrasos=[]
    cuentas.each{cuenta ->
       if(cuenta.getSaldo()>0){
           def hoy=new Date()
           def vto=cuenta.vencimiento?:cuenta.fecha+clienteCre.plazo.intValue()
           def atraso=(hoy-vto)?:0

           atrasos.add(atraso)
       }

   }
		def atrasoMax=0
    if(atrasos.max()){
          atrasoMax=atrasos.max()

    }else{
     atrasoMax=0
    }
    if(atrasoMax<0)
    	atrasoMax=0

   //println cliente.nombre+"atrasoMax   "+atrasoMax

    clienteCre.atrasoMaximo=atrasoMax

    clienteCre.save(failOnError: true, flush:true)

    def audit=new AuditLog()

    audit.persistedObjectId=clienteCre.id
    audit.target='CENTRAL'
    audit.dateCreated=audit.lastUpdated=new Date()
    audit.name='ClienteCredito'
    audit.tableName='cliente_credito'
    audit.eventName='UPDATE'
    audit.source='NA'

    audit.save(failOnError: true, flush:true)

}
}

}

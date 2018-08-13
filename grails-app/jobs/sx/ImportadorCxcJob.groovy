package sx

class ImportadorDeCxcJob {

  def importadorDeCxc

    static triggers = {
       cron name:   'impCxc',   startDelay: 10000, cronExpression: '0 0 0/2 * * ?'
    }

    def execute() {

      println "************************************************************"
      println "*                                                          *"
      println "*              Importador De CuentaPorCobrar  ${new Date()}  "
      println "*                                                          *"
      println "************************************************************"

      try{
        println "Se incio la importacion de CxC operaciones Venta ${new Date()}"
        importadorDeCxc.importarOperacionesVenta()
        println "Se importaron  con exito las operaciones cxc Ventas con exito ${new Date()} !!!"

      }catch(Exception e){
        e.printStackTrace()
      }

    }
}

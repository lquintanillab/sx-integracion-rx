package sx

class ImportadorVentasCreditoJob {

  def importadorDeVentasCredito

    static triggers = {
      cron name:   'impVtasCre',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def execute() {

      println "************************************************************"
      println "*                                                          *"
      println "*            Importando Ventas Credito     ${new Date()}  "
      println "*                                                          *"
      println "************************************************************"

      try{
        println "Se inicio la importacion de ventas credito "
        importadorDeVentasCredito.importar()

        println "Se importaron las ventas de credito con exito ${new Date()} !!!"

      }catch(Exception e){
        e.printStackTrace()
      }

    }
}

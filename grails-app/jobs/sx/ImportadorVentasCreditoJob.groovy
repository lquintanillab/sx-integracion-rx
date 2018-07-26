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
        importadorDeVentasCredito.importar()
      }catch(Exception e){
        e.printStackTrace()
      }

    }
}

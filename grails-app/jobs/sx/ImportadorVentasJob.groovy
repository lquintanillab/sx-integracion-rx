package sx

class ImportadorVentasJob {
  def importadorDeVentas

    static triggers = {
      cron name:   'impVtas',   startDelay: 20000, cronExpression: '0 0/8 * * * ?'
    }

    def execute() {

      println "************************************************************"
      println "*                                                          *"
      println "*            Importando Ventas         ${new Date()}  "
      println "*                                                          *"
      println "************************************************************"

      try{
        importadorDeVentas.importar()
      }catch(Exception e){
        e.printStackTrace()
      }

    }
}

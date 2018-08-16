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
        println "Se inicio la importacion de Ventas "
        importadorDeVentas.importar()
        println "Se importaron las ventas con exito ${new Date()} !!!"
      }catch(Exception e){
        e.printStackTrace()
      }

    }
}

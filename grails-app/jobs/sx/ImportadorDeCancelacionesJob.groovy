package sx

class ImportadorDeCancelacionesJob {

  def importadorDeCancelaciones
    static triggers = {
        cron name:   'impCanc',   startDelay: 10000, cronExpression: '0 15 19 * * ?'
    }

    def execute() {
      println "************************************************************"
      println "                                     "
      println "                   Importador De Cancelaciones     ${new Date()}  "
      println "                                                         "
      println "************************************************************"

      try{
        println "Se inicio la importacion de cancelaciones"
        importadorDeCancelaciones.importar()
        println "Se importaron con exito las cancelaciones ${new Date()} !!!"
        }catch(Exception e){
        e.printStackTrace()
      }
    }
}

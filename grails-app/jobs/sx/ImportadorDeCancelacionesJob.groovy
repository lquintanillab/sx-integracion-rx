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
       // importadorDeCancelaciones.importar()
      }catch(Exception e){
        e.printStackTrace()
      }
    }
}

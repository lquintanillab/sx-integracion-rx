package sx

class ImportadorTrasladosJob {

    def importadorDeTraslados

    static triggers = {
      cron name:   'impTraslados',   startDelay: 20000, cronExpression: '0 0/15 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                 Importador Traslados ${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
          println  "Se inicio el importador de Traslados ${new Date()}"
          importadorDeTraslados.importar()
          println "Se importaron con exito lls traslados ${new Date()}"
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

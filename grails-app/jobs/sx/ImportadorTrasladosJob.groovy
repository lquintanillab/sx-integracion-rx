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
          importadorDeTraslados.importar()
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

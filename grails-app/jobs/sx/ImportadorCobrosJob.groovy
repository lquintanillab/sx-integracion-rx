package sx

class ImportadorCobrosJob {

    def importadorDeCobros

    static triggers = {
      cron name:   'impCobros',   startDelay: 20000, cronExpression: '0 0/20 * * * ?'
    }

    def execute() {

        println "************************************************************"
        println "                                                          "
        println "                 ImportadorCobros ${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
          importadorDeCobros.importar()
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

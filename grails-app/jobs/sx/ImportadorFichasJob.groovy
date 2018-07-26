package sx

class ImportadorFichasJob {
    
    def importadorDeFichas 

    static triggers = {
      cron name:   'impFichas',   startDelay: 20000, cronExpression: '0 0/15 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                 ImportadorFichas ${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
          importadorDeFichas.importar()
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

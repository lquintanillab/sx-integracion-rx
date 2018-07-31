package sx

class ImportadorAplicacionesCONJob {

       def importadorDeAplicacionesCON

    static triggers = {
      cron name:   'impApsCON',   startDelay: 20000, cronExpression: '0 0/20 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                 Importador Aplicaciones CON  ${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
        //  importadorDeAplicacionesCON.importar()
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

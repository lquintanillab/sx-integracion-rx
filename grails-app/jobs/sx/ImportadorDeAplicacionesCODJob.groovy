package sx

class ImportadorDeAplicacionesCODJob {

    def importadorDeAplicacionesCOD

    static triggers = {
      cron name:   'impApsCOD',   startDelay: 20000, cronExpression: '0 0/10 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                 Importador Aplicaciones COD  ${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
         // importadorDeAplicacionesCOD.importar()
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

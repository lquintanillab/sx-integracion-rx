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
          println "Se ha iniciado la importacion de fichas ${new Date()}"
          importadorDeFichas.importar()
          println "Se importaron las fichas con exito ${new Date()} !!!"
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

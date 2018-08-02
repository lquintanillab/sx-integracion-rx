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
          println "Se ha iniciado la importacion de Cobros ${new Date()}"
          importadorDeCobros.importar()
          println "Se importaron los cobros con exito ${new Date()} !!!"
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

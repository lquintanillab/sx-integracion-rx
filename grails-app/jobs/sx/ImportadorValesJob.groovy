package sx

class ImportadorValesJob {

    def importadorDeVales

    static triggers = {
      cron name:   'impVales',   startDelay: 20000, cronExpression: '0 0/4 * * * ?' 
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                 Importador Vales ${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
         println "Se inicio el importador de Vales ${new Date()}"
          importadorDeVales.importar()
          println "Se importaron los vales con exito ${new Date()}"
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

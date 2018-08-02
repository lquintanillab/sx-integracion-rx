package sx

class ExportadorValesJob {

    def exportadorDeVales

    static triggers = {
      cron name:   'expVales',   startDelay: 20000, cronExpression: '0 0/4 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                  Exportando Vales ${new Date()}  "
        println "                                                          "
        println "************************************************************"


        try{
          println "Se inicio la exportacion de los vales ${new Date()}"
            exportadorDeVales.exportar()
          println "Se exportaron los vales con exito ${new Date()}  !!!"
        }catch(Exception e){
                e.printStackTrace()
        }
    }
}

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
          //  exportadorDeVales.exportar()
        }catch(Exception e){
                e.printStackTrace()
        }
    }
}

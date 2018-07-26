package sx

class ExportadorClientesCreJob {

    def exportadorDeClientesCredito

    static triggers = {
        cron name:   'expClientesCre',   startDelay: 20000, cronExpression: '0 0/2 * * * ?'
    }

    def execute() {

         println "************************************************************"
        println "                                                          "
        println "                  Exportando Clientes Credito ${new Date()}  "
        println "                                                          "
        println "************************************************************"


        try{
            exportadorDeClientesCredito.exportar()
        }catch(Exception e){
                e.printStackTrace()
        }
    }
}

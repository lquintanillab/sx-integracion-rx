package sx

class ExportadorDepositosJob {

    def exportadorDeDepositos

    static triggers = {
      cron name:   'expDepositos',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println  "  "                                                    
        println  "         Exportando Depositos  ${new Date()}               "
        println  "  " 
        println "************************************************************"
    try {
          exportadorDeDepositos.exportar()
        println "Se exportaron con exito los depositos ${new Date()}!!!"

    }catch (Exception e){
            println "Hubo un error al expotar Depositos!!!"
          e.printStackTrace()
    }
    }
}

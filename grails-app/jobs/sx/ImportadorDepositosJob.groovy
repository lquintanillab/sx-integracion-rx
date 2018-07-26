package sx

class ImportadorDepositosJob {

    def importadorDeDepositos

    static triggers = {
      cron name:   'impDepositos',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println  "  "                                                    
        println  "         Importando Depositos  ${new Date()}               "
        println  "  " 
        println "************************************************************"
    try {
          importadorDeDepositos.importar()
    }catch (Exception e){
          e.printStackTrace()
    }
    }
}

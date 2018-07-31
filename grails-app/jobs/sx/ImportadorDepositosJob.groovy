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
          println "Se importaron con exito los depositos ${new Date()}!!!"
    }catch (Exception e){
        println "Hubo un error al importar los despositos!!!"
          e.printStackTrace()
    }
    }
}

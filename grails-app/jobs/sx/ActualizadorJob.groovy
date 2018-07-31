package sx

class ActualizadorJob {
    def  actualizacionCredito
    static triggers = {
      cron name:   'act',   startDelay: 10000, cronExpression: '0 50 23 * * ?'
    }

    def execute() {

         println "************************************************************"
        println "                                                          "
        println "                  Actualizando Saldos Cre ${new Date()}  "
        println "                                                          "
        println "************************************************************"


        try{
          //  actualizacionCredito.actualizarSaldo()
        }catch(Exception e){
          e.printStackTrace()
        }
        try{
          //  actualizacionCredito.actualizarAtraso()
        }catch(Exception e){
          e.printStackTrace()
        }
        
    }
}

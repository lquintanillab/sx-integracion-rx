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
          println "Se arranco la actualizacion de saldos ${new Date()}"
            actualizacionCredito.actualizarSaldo()
            println "Se actualizaron  los saldos con exito ${new Date()}"
        }catch(Exception e){
          e.printStackTrace()
        }
        try{
          println "Se arranco la actualizacion  de atraso ${new Date()}"
            actualizacionCredito.actualizarAtraso()
            println "Se actualizaron  los atrasos con exito ${new Date()}"
        }catch(Exception e){
          e.printStackTrace()
        }
        
    }
}

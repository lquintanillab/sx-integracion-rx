package sx

class AjusteReplicaJob {

    def sincronizacionService
    static triggers = {
      cron name:   'ajuReplica',   startDelay: 20000, cronExpression: '0 0/2 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                  Ajustando Replica ${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
          sincronizacionService.replicaClientesCredito()
      }catch (Exception e){
          e.printStackTrace()
      }

      try {

         sincronizacionService.depuraReplicaOficinas()
      }catch (Exception e){
          e.printStackTrace()
      }

    }
}

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

          println "Se ajusto la replica para enviar clientes credito ${new Date()}"
      }catch (Exception e){
          e.printStackTrace()
      }
      
      try {
          sincronizacionService.replicaClientes()

          println "Se ajusto la replica para enviar clientes  ${new Date()}"
      }catch (Exception e){
          e.printStackTrace()
      }

    }
}

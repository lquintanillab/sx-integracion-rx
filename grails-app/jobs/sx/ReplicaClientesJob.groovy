package sx

class ReplicaClientesJob {

    def importadorDeClientes
    def exportadorDeClientes
    
    static triggers = {
      cron name:   'repCliente',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def execute() {

println "************************************************************"
println  "  "                                                    
println  "         Importando Clientes   ${new Date()}               "
println  "  " 
println "************************************************************"



        try{
            importadorDeClientes.importar()
        }catch(Exception e){
            e.printStackTrace()
        } 
        try{
            exportadorDeClientes.exportar()
        }catch(Exception e){
              e.printStackTrace()
        } 
    }
}

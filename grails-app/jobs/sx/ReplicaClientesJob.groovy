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
            println "Se importaron con exito los clientes ${new Date()}!!!"

        }catch(Exception e){
            e.printStackTrace()
        } 
        try{
            exportadorDeClientes.exportar()
            println "Se exportaron con exito los clientes ${new Date()}!!!"
        }catch(Exception e){
              e.printStackTrace()
        } 
    }
}

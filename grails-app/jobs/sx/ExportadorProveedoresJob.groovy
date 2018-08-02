package sx

class ExportadorProveedoresJob {

    def exportadorDeProveedores

    static triggers = {
      cron name:   'expProveedores',   startDelay: 20000, cronExpression: '0 0/7 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                  Exportando Proveedores ${new Date()}  "
        println "                                                          "
        println "************************************************************"


        try{
            println "Exportando Proveedores ${new Date()}"
            exportadorDeProveedores.exportar()
            println "Se exportaron los proveedores con exito ${new Date()} !!!"
            
        }catch(Exception e){
                e.printStackTrace()
        }
    }
}

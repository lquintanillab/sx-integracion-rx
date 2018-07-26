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
            exportadorDeProveedores.exportar()
        }catch(Exception e){
                e.printStackTrace()
        }
    }
}

package sx

class ExportadorTrasladosJob {

    def exportadorDeTraslados

    static triggers = {
     cron name:   'expTraslados',   startDelay: 20000, cronExpression: '0 0/15 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                  Exportando Traslados ${new Date()}  "
        println "                                                          "
        println "************************************************************"


        try{
           println "Se inicio la exportacion de traslados ${new Date()}"
            exportadorDeTraslados.exportar()
            println "Se exportaron los traslados  con exito ${new Date()} !!!"
        }catch(Exception e){
                e.printStackTrace()
        }
    }
}

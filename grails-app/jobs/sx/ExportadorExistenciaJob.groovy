package sx

class ExportadorExistenciaJob {

    def exportadorDeExistencia

    static triggers = {
      cron name:   'expExistencia',   startDelay: 20000, cronExpression: '0 0/10 * * * ?'
    }

    def execute() {
       println "************************************************************"
        println "                                                          "
        println "                  Exportador Existencia ${new Date()}  "
        println "                                                          "
        println "************************************************************"

        try{
                println "Se inicio el importador de Existencias ${new Date()}"
              exportadorDeExistencia.exportar()
              println "Existencias exportadas con exito ${new Date()} !!!"
        }catch(Exception e){
            e.printStackTrace()
        }

    }
}

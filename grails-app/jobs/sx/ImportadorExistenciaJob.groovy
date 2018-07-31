package sx

class ImportadorExistenciaJob {

    def importadorDeExistencia

    static triggers = {
      cron name:   'impExistencia',   startDelay: 20000, cronExpression: '0 0/10 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                  Importador Existencia ${new Date()}  "
        println "                                                          "
        println "************************************************************"

    
    try{
           // importadorDeExistencia.importar()
        }catch(Exception e){
            e.printStackTrace()
        }

    }
}

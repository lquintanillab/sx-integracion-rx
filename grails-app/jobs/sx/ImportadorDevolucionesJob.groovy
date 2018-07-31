  package sx

class ImportadorDevolucionesJob {

def importadorDeDevoluciones

    static triggers = {
       cron name:   'impDevoluciones',   startDelay: 10000, cronExpression: '0 20 * * * ?'
    }

    def execute() {

      println "************************************************************"
      println "*                                                          *"
      println "*                    Importando Devoluciones     ${new Date()}  "
      println "*                                                          *"
      println "************************************************************"

      try{
         // importadorDeDevoluciones.importar()
      }catch(Exception e){
        e.printStackTrace()
      }

    }
}

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
        println "Se inicio el importador de Devoluciones de Venta ${new Date()}"
          importadorDeDevoluciones.importar()
         println "Se importaron las devoluciones de venta con exito ${new Date()} !!!"
      }catch(Exception e){
        e.printStackTrace()
      }

    }
}

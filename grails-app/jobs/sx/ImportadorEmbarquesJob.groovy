package sx

class ImportadorEmbarquesJob {

    def importadorDeEmbarques

    static triggers = {
      cron name:   'impEmb',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def execute() {

      println "************************************************************"
      println "*                                                          *"
      println "*            Importando Embarques     ${new Date()}  "
      println "*                                                          *"
      println "************************************************************"

      try{
        println "Se inicio la importacion de Embarques"
          importadorDeEmbarques.importar()
        println "Se importaron con exito las cancelaciones ${new Date()} !!!"
      }catch(Exception e){
        e.printStackTrace()
      }
    }
}

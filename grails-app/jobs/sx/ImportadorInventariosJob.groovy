package sx

class ImportadorInventariosJob {

  def importadorDeInventarios
    static triggers = {
      cron name:   'impInv',   startDelay: 10000, cronExpression: '0 0 21 * * ?'
    }

    def execute() {

      println "************************************************************"
      println "*                                                          *"
      println "*              Importador De inventarioSuc  ${new Date()}  "
      println "*                                                          *"
      println "************************************************************"

      try{
       // importadorDeInventarios.importar()
      }catch(Exception e){
        e.printStackTrace()
      }

    }
}

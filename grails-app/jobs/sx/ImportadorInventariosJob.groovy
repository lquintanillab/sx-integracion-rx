package sx

class ImportadorInventariosJob {

  def importadorDeInventarios
    static triggers = {
      cron name:   'impInv',   startDelay: 10000, cronExpression: '0 40 * * * ?'
      
    }

    def execute() {

      println "************************************************************"
      println "*                                                          *"
      println "*              Importador De inventarioSuc  ${new Date()}  "
      println "*                                                          *"
      println "************************************************************"

      try{
        println "Se incio el importador de  Inventarios ${new Date()}"
        importadorDeInventarios.importar()
       println "Se importaron los inventarios con exito ${new Date()} !!!"
      }catch(Exception e){
        e.printStackTrace()
      }

    }
}

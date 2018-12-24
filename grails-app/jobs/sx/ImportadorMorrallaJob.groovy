package sx

class ImportadorMorrallaJob {

    static triggers = {
      cron name:   'impMor',   startDelay: 10000, cronExpression: '0 0 20 * * ?'
      
    }


    def execute() {
      println "************************************************************"
      println "*                                                          *"
      println "*              Importador De  morralla ${new Date()}  "
      println "*                                                          *"
      println "************************************************************"

      try{
        println "Se incio el importador de  Morrallas ${new Date()}"
        importadorDeMorrallas.importar()
       println "Se importaron las Morrallas con exito ${new Date()} !!!"
      }catch(Exception e){
        e.printStackTrace()
      }
    }
}

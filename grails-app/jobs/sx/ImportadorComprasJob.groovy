package sx

class ImportadorComprasJob {

    def importadorDeCompras

    static triggers = {
      cron name:   'impCompras',   startDelay: 20000, cronExpression: '0 0/10 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                 Importador Compras${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
        println "Se inicio el importador de compras ${new Date()}"
           importadorDeCompras.importar()
        println "Se importaron las compras con exito ${new Date()} !!!"
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

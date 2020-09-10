package sx

class ExportadorComprasJob {

    def exportadorDeCompras

    static triggers = {
       cron name:   'expComp',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                 Exportador De  Compras ${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
          println "Se inicio la exportacion de Compras ${new Date()} !!!"
         // exportadorDeCompras.exportar()
          println "Se exportaron las Compras con exito ${new Date()}  !!!"
        
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

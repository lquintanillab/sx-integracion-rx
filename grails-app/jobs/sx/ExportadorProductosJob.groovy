package sx

class ExportadorProductosJob {
     def exportadorDeProductos

    static triggers = {
     cron name:   'expProductos',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "                                                          "
        println "                 Exportador Productos ${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
         // exportadorDeProductos.exportar()
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

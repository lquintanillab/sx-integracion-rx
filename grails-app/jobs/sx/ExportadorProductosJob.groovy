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
          println "Exportando los productos ${new Date()} !!!"
          exportadorDeProductos.exportar()
          println "Se exportaron los productos con exito ${new Date()}  !!!"
        
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

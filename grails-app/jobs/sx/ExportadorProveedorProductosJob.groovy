package sx

class ExportadorProveedorProductosJob {

    def exportadorDeProveedorProducto
    static triggers = {
       cron name:   'expPrvPrd',   startDelay: 20000, cronExpression: '0 0/3 * * * ?'
    }

    def execute() {
         println "************************************************************"
        println "                                                          "
        println "                 Exportador Proveedor Productos ${new Date()}  "
        println "                                                          "
        println "************************************************************"


      try {
          println "Se inicio la exportacion de Proveedor Producto ${new Date()} !!!"
          exportadorDeProveedorProducto.exportar()
          println "Se exportaron los Proveedores Productos con exito ${new Date()}  !!!"
        
      }catch (Exception e){
          e.printStackTrace()
      }
    }
}

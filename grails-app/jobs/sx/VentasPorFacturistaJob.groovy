package sx

class VentasPorFacturistaJob {


    def ventasPorFacturistaIntegracion

    static triggers = {
      cron name:   'impVxFac',   startDelay: 20000, cronExpression: '0 30 20-21 * * ?'
    }

    def execute() {
        try{
            ventasPorFacturistaIntegracion.actualizar()
        }catch(Exception e){
            e.printStackTrace()
        }
    }
}

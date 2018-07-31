package sx.cinco

class ExportadorCincoJob {

def exportadorDeVales
  def exportadorDeTraslados

    static triggers = {
      cron name:   'expCinco',   startDelay: 10000, cronExpression: '0 0/7 * * * ?'
    }

    def execute() {
      println "************************************************************"
      println "*                                                          *"
      println "*                    Exportando Cinco   ${new Date()}                  *"
      println "*                                                          *"
      println "************************************************************"

      def sucursal = 'CF5FEBRERO'

      try{
        // exportadorDeVales.exportarSucursal(sucursal)
      }catch (Exception e){
             e.printStackTrace()
     }
     try{
        //exportadorDeTraslados.exportarSucursal(sucursal)
     }catch (Exception e){
            e.printStackTrace()
    }
    }
}

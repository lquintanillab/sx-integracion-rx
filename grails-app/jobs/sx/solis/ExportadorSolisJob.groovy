package sx.solis

class ExportadorSolisJob {

def exportadorDeVales
  def exportadorDeTraslados

    static triggers = {
      cron name:   'expSolis',   startDelay: 10000, cronExpression: '0 0/7 * * * ?'
    }

    def execute() {
      println "************************************************************"
      println "*                                                          *"
      println "*                    Exportando Solis   ${new Date()}                  *"
      println "*                                                          *"
      println "************************************************************"

      def sucursal = 'SOLIS'

      try{
         exportadorDeVales.exportarSucursal(sucursal)
      }catch (Exception e){
             e.printStackTrace()
     }
     try{
        exportadorDeTraslados.exportarSucursal(sucursal)
     }catch (Exception e){
            e.printStackTrace()
    }
    }
}

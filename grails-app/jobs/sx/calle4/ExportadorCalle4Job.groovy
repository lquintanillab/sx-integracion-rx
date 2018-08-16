package sx.calle4

class ExportadorCalle4Job {

def exportadorDeVales
  def exportadorDeTraslados

    static triggers = {
      cron name:   'expCalle4',   startDelay: 10000, cronExpression: '0 0/7 * * * ?'
    }

    def execute() {
      println "************************************************************"
      println "*                                                          *"
      println "*                    Exportando Calle 4   ${new Date()}                  *"
      println "*                                                          *"
      println "************************************************************"

      def sucursal = 'CALLE 4'

      try{
        // exportadorDeVales.exportarSucursal(sucursal)
      }catch (Exception e){
             e.printStackTrace()
     }
     try{
       // exportadorDeTraslados.exportarSucursal(sucursal)
     }catch (Exception e){
            e.printStackTrace()
    }
    }
}

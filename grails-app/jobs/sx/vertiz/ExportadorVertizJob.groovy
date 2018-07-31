package sx.vertiz

class ExportadorVertizJob {

def exportadorDeVales
  def exportadorDeTraslados

    static triggers = {
      cron name:   'expVertiz',   startDelay: 10000, cronExpression: '0 0/7 * * * ?'
    }

    def execute() {
      println "************************************************************"
      println "*                                                          *"
      println "*                    Exportando Vertiz   ${new Date()}                  *"
      println "*                                                          *"
      println "************************************************************"

      def sucursal = 'VERTIZ 176'

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

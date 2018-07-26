package sx.tacuba

class ExportadorTacubaJob {

def exportadorDeVales
  def exportadorDeTraslados

    static triggers = {
      cron name:   'expTacuba',   startDelay: 10000, cronExpression: '0 0/7 * * * ?'
    }

    def execute() {
      println "************************************************************"
      println "*                                                          *"
      println "*                    Exportando Tacuba  ${new Date()}                  *"
      println "*                                                          *"
      println "************************************************************"

      def sucursal = 'TACUBA'

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

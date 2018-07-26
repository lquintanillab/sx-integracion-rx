package sx.andrade

class ExportadorAndradeJob {

def exportadorDeVales
  def exportadorDeTraslados

    static triggers = {
      cron name:   'expAndrade',   startDelay: 10000, cronExpression: '0 0/7 * * * ?'
    }

    def execute() {
      println "************************************************************"
      println "*                                                          *"
      println "*                    Exportando Andrade   ${new Date()}                  *"
      println "*                                                          *"
      println "************************************************************"

      def sucursal = 'ANDRADE'

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

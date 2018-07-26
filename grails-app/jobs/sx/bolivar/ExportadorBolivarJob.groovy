package sx.bolivar

class ExportadorBolivarJob {

def exportadorDeVales
  def exportadorDeTraslados

    static triggers = {
       cron name:   'expBolivar',   startDelay: 10000, cronExpression: '0 0/7 * * * ?'
    }

    def execute() {
    println "************************************************************"
      println "*                                                          *"
      println "*                    Exportando Bolivar   ${new Date()}                  *"
      println "*                                                          *"
      println "************************************************************"

      def sucursal = 'BOLIVAR'

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

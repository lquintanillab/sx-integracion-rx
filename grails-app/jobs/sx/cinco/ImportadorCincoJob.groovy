package sx.cinco

class ImportadorCincoJob {


def importadorDeVales
    def importadorDeTraslados
    def replicaService

    static triggers = {
      cron name:   'impCinco',   startDelay: 20000, cronExpression: '0 0/4 * * * ?'
    }

    def execute() {
       println "************************************************************"
       println "*                                                          *"
       println "*                    Importando Cinco   ${new Date()}                  *"
       println "*                                                          *"
       println "************************************************************"

       def sucursal = 'CF5FEBRERO'

       try{
    //      println "importando Vales: "+sucursal
          importadorDeVales.importarSucursal(sucursal)
       }catch (Exception e){
              e.printStackTrace()
      }
      try{
      //   println "importando Traslados: "+sucursal
         importadorDeTraslados.importarSucursal(sucursal)
      }catch (Exception e){
             e.printStackTrace()
     }
     try{
      //  println "******************************importando Embarques********************************** "+sucursal
      replicaService.importarServer(sucursal)
     }catch (Exception e){
            e.printStackTrace()
    }
    }
}

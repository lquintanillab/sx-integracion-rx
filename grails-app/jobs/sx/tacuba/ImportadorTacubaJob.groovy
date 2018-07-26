package sx.tacuba

class ImportadorTacubaJob {


def importadorDeVales
    def importadorDeTraslados
    def replicaService

    static triggers = {
      cron name:   'impTacuba',   startDelay: 20000, cronExpression: '0 0/4 * * * ?'
    }

    def execute() {
            println "************************************************************"
       println "*                                                          *"
       println "*                    Importando Tacuba  ${new Date()}                  *"
       println "*                                                          *"
       println "************************************************************"

       def sucursal = 'TACUBA'

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

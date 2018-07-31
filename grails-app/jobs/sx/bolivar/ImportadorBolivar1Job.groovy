package sx.bolivar

class ImportadorBolivar1Job {

 def importadorDeExistencia
      def importadorDeClientes
      def importadorDeVales
      def importadorDeTraslados

    static triggers = {
      cron name:   'impBolivar1',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def execute() {
        println "************************************************************"
         println "*                                                          *"
         println "*                    Importando Bolivar Uno  ${new Date()}              *"
         println "*                                                          *"
         println "************************************************************"

         def sucursal = 'BOLIVAR'

         try{
          //      println "importando Existencias: "+sucursal
              //   importadorDeExistencia.importar(sucursal)
              }catch (Exception e){
                     e.printStackTrace()
             }
             try{
            //    println "importando Clientes: "+sucursal
            //    importadorDeClientes.importar(sucursal)
             }catch (Exception e){
                    e.printStackTrace()
            }
            try{
        //       println "importando ComunicacionEmpresa: "+sucursal
           //    importadorDeClientes.importarComunicacionEmpresa(sucursal)
            }catch (Exception e){
                   e.printStackTrace()
           }
    }
}

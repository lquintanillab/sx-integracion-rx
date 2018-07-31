package sx.calle4

class ImportadorCalle41Job {

 def importadorDeExistencia
      def importadorDeClientes
      def importadorDeVales
      def importadorDeTraslados


    static triggers = {
      cron name:   'impCalle41',   startDelay: 20000, cronExpression: '0 0/5 * * * ?'
    }

    def execute() {
        println "************************************************************"
         println "*                                                          *"
         println "*                    Importando Calle 4 Uno  ${new Date()}              *"
         println "*                                                          *"
         println "************************************************************"

         def sucursal = 'CALLE 4'

         try{
          //      println "importando Existencias: "+sucursal
            //    importadorDeExistencia.importar(sucursal)
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
         //      importadorDeClientes.importarComunicacionEmpresa(sucursal)
            }catch (Exception e){
                   e.printStackTrace()
           }
    }
}

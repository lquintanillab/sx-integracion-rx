package sx.solis

class ExportadorSolis1Job {


  def exportadorDeClientes
    def exportadorDeClientesCredito
    def exportadorDeProductos
    def exportadorDeExistencia
    def exportadorDeVales
    def exportadorDeTraslados


    static triggers = {
       cron name:   'expSolis1',   startDelay: 10000, cronExpression: '0 0/5 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "*                                                          *"
        println "*                    Exportando SOLIS Uno ${new Date()}              *"
        println "*                                                          *"
        println "************************************************************"

        def sucursal = 'SOLIS'

              try{
                 exportadorDeClientesCredito.exportar(sucursal)
              }catch (Exception e){
                     e.printStackTrace()
             }
             try{
                exportadorDeClientes.exportar(sucursal)
             }catch (Exception e){
                    e.printStackTrace()
              }
              try{
                 exportadorDeProductos.exportar(sucursal)
              }catch (Exception e){
                     e.printStackTrace()
             }
             try{
                exportadorDeExistencia.exportar(sucursal)
             }catch (Exception e){
                    e.printStackTrace()
            }
    }
}

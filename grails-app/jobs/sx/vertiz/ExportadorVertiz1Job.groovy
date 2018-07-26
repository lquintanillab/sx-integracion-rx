package sx.vertiz

class ExportadorVertiz1Job {

  def exportadorDeClientes
    def exportadorDeClientesCredito
    def exportadorDeProductos
    def exportadorDeExistencia
    def exportadorDeVales
    def exportadorDeTraslados

    static triggers = {
       cron name:   'expVertiz1',   startDelay: 10000, cronExpression: '0 0/5 * * * ?'
    }

    def execute() {
        println "************************************************************"
        println "*                                                          *"
        println "*                    Exportando Vertiz Uno ${new Date()}              *"
        println "*                                                          *"
        println "************************************************************"

        def sucursal = 'VERTIZ 176'

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

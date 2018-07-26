package sx

class ImportadorDeCxcJob {

  def importadorDeCxc

    static triggers = {
       cron name:   'impCxc',   startDelay: 10000, cronExpression: '0 0 0/2 * * ?'
    }

    def execute() {

      println "************************************************************"
      println "*                                                          *"
      println "*              Importador De CuentaPorCobrar  ${new Date()}  "
      println "*                                                          *"
      println "************************************************************"

      try{
        importadorDeCxc.importarOperacionesVenta()
      }catch(Exception e){
        e.printStackTrace()
      }

    }
}

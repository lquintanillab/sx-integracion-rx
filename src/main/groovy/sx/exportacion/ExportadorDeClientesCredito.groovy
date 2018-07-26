package sx.exportacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier




@Component
class ExportadorDeClientesCredito{


  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def exportar(){
    replicaService.exportar('ClienteCredito')
  }

  def exportar(nombreSuc){
    replicaService.exportar('ClienteCredito',nombreSuc)
  }

}

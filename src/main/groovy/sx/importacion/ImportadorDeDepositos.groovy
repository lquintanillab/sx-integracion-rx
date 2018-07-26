package sx.importacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier




@Component
class ImportadorDeDepositos{

  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def importar(){
    replicaService.importar('SolicitudDeDeposito')
  }

  def importar(nombreSuc){
    replicaService.importar('SolicitudDeDeposito',nombreSuc)
  }

}

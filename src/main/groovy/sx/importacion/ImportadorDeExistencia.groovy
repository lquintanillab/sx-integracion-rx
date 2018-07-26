package sx.importacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier





@Component
class ImportadorDeExistencia{

  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def importar(){
    replicaService.importar('Existencia')
  }

  def importar(nombreSuc){
    replicaService.importar('Existencia',nombreSuc)
  }

}

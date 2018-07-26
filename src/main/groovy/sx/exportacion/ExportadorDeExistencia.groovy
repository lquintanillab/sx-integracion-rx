package sx.exportacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier





@Component
class ExportadorDeExistencia{

  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def exportar(){
    replicaService.exportar('Existencia')
  }

  def exportar(nombreSuc){
    replicaService.exportar('Existencia',nombreSuc)
  }

}

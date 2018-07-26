package sx.importacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier




@Component
class ImportadorDeClientes{

  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def importar(){
    replicaService.importar('Cliente')
  }

  def importar(nombreSuc){
    replicaService.importar('Cliente',nombreSuc)
  }

  def importarComunicacionEmpresa(){
    replicaService.importar('ComunicacionEmpresa')
  }

  def importarComunicacionEmpresa(nombreSuc){
    replicaService.importar('ComunicacionEmpresa',nombreSuc)
  }

}
package sx.exportacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier




@Component
class ExportadorDeClientes{


  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def exportar(){
    replicaService.exportar('Cliente')
  }

  def exportar(nombreSuc){
    replicaService.exportar('Cliente',nombreSuc)
  }


  def exportarComunicacionEmpresa(){
    replicaService.exportar('ComunicacionEmpresa')
  }

  def exportarComunicacionEmpresa(nombreSuc){
    replicaService.exportar('ComunicacionEmpresa',nombreSuc)
  }

}

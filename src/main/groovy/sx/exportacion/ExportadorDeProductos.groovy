package sx.exportacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier




@Component
class ExportadorDeProductos{

  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def exportar(){
    replicaService.exportar('Producto')
  }

  def exportar(nombreSuc){
    replicaService.exportar('Producto',nombreSuc)
  }

}

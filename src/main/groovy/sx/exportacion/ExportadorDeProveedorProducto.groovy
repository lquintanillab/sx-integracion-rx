package sx.exportacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier




@Component
class ExportadorDeProveedorProducto{

  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def exportar(){
    replicaService.exportar('ProveedorProducto')
  }

  def exportar(nombreSuc){
    replicaService.exportar('ProveedorProducto',nombreSuc)
  }

  def exportarProductos(){
      replicaService.exportar('ProveedorProducto')
  }

}

package sx.exportacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier




@Component
class ExportadorDeProveedores{

  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def exportar(){
    replicaService.exportar('Proveedor')
  }

  def exportar(nombreSuc){
    replicaService.exportar('Proveedor',nombreSuc)
  }

  def exportarProductos(){
      replicaService.exportar('ProveedorProductos')
  }

}

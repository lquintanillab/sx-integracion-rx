package sx.importacion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.DataSourceReplica
import sx.EntityConfiguration
import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.SimpleJdbcInsert


@Component
class ImportadorDeMorralla{

  @Autowired
  @Qualifier('replicaService')
  def replicaService


  def importar(){
    replicaService.importar('Morralla')
  }

  def importar(nombreSuc){
    replicaService.importar('Morralla',nombreSuc)
  }

}

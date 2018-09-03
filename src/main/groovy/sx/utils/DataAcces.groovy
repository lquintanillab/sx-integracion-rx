package sx.utils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier
import groovy.sql.Sql
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.core.simple.SimpleJdbcInsert

import sx.EntityConfiguration
import sx.Sucursal


@Component
class DataAcces{


    @Autowired
    @Qualifier('dataSource')
    def dataSource


     def importarOp(ip, bd, user,password, fecha, sucName, queryOperaciones ,queryOperacion ,entity,actualizar,params ){

        def dataSourceRemote=dataSourceResolve(ip, bd, user, password)

        def sqlSuc=getSql(dataSourceRemote)

        def sqlCen=getSql(dataSource)

        def sucursal=getSucursal(sucName)

        def config=getConfig(entity)

            if(!params){
                params=[]

                if (fecha && sucursal){
                    params=[fecha,sucursal.id]
                }
                if(!fecha && sucursal){
                    params=[sucursal.id]
                }
                if(fecha && !sucursal){
                    params=[fecha]
                }
            }


            

                def operaciones=sqlSuc.rows(queryOperaciones,params)

                operaciones.each{ operacionSuc ->

                    def operacionCen=sqlCen.firstRow(queryOperacion,[operacionSuc.id])

                    if(!operacionCen){

                        println "La operacion No existe importar: "+operacionSuc.id
            
                        try{
                            SimpleJdbcInsert insert= new SimpleJdbcInsert(dataSource).withTableName(config.tableName)
                            def res=insert.execute(operacionSuc)
                        }catch(Exception e){
                            e.printStackTrace()
                        }
                    }else if(actualizar){
                    println "La operacion ya existe: "+operacionSuc.id
                            //sqlCen.executeUpdate(operacionSuc, config.updateSql)
                    }

                }

                
            
        }


     def validarOp(ip, bd, user,password, fecha, sucName, queryOperaciones ,queryOperacion ,entity,actualizar,params ){

        def dataSourceRemote=dataSourceResolve(ip, bd, user, password)

        def sqlSuc=getSql(dataSourceRemote)

        def sqlCen=getSql(dataSource)

        def sucursal=getSucursal(sucName)

        def config=getConfig(entity)

        def faltantes= []

            if(!params){
                params=[]

                if (fecha && sucursal){
                    params=[fecha,sucursal.id]
                }
                if(!fecha && sucursal){
                    params=[sucursal.id]
                }
                if(fecha && !sucursal){
                    params=[fecha]
                }
            }

                def operaciones=sqlSuc.rows(queryOperaciones,params)

                operaciones.each{ operacionSuc ->

                    def operacionCen=sqlCen.firstRow(queryOperacion,[operacionSuc.id])

                    if(!operacionCen){

                        //println "La operacion No existe importar: "+operacionSuc.id

                        faltantes.add(operacionSuc)
            
                        try{
                           
                        }catch(Exception e){
                            e.printStackTrace()
                        }
                    }else if(actualizar){
                   // println "La operacion ya existe: "+operacionSuc.id
                            
                    }

                }

                
                println "---*** Faltantes ${fecha} ***---   "+faltantes.size()
            
        }


    def revisionCentral(ip, bd, user,password, fecha, sucName, queryOperaciones ,queryOperacion , entity, actualizar, params){

        def dataSourceRemote=dataSourceResolve(ip, bd, user, password)

        def sqlSuc=getSql(dataSourceRemote)

        def sqlCen=getSql(dataSource)

        def sucursal=getSucursal(sucName)

        def config=getConfig(entity)

        def faltantes= []

            if(!params){
                params=[]

                if (fecha && sucursal){
                    params=[fecha,sucursal.id]
                }
                if(!fecha && sucursal){
                    params=[sucursal.id]
                }
                if(fecha && !sucursal){
                    params=[fecha]
                }
            }

                def operaciones=sqlCen.rows(queryOperaciones,params)

                operaciones.each{ operacionCen ->

                    def operacionSuc=sqlSuc.firstRow(queryOperacion,[operacionCen.id])

                    if(!operacionSuc){

                        //println "La operacion No existe importar: "+operacionSuc.id

                        faltantes.add(operacionCen)
            
                        try{
                           
                        }catch(Exception e){
                            e.printStackTrace()
                        }
                    }else if(actualizar){
                   // println "La operacion ya existe: "+operacionSuc.id
                            
                    }

                }

                
                println "---*** Faltantes ${fecha} ***---   "+faltantes.size()



    }


    def depurarCentral(ip, bd, user,password, fecha, sucName, queryOperaciones ,queryOperacion , entity, actualizar, params){
        
        def dataSourceRemote=dataSourceResolve(ip, bd, user, password)

        def sqlSuc=getSql(dataSourceRemote)

        def sqlCen=getSql(dataSource)

        def sucursal=getSucursal(sucName)

        def config=getConfig(entity)

            if(!params){
                params=[]

                if (fecha && sucursal){
                    params=[fecha,sucursal.id]
                }
                if(!fecha && sucursal){
                    params=[sucursal.id]
                }
                if(fecha && !sucursal){
                    params=[fecha]
                }
            }

                def operaciones=sqlCen.rows(queryOperaciones,params)

                operaciones.each{ operacionCen ->

                   // println "operacion Cen"+operacionCen.id


                    def operacionSuc=sqlSuc.firstRow(queryOperacion,[operacionCen.id])

                    if(!operacionSuc){

                        println "La operacion No existe depurar: "+operacionCen.id
            
                        try{
                          //  sqlCen.execute("DELETE FROM $config.tableName WHERE id = ?",[operacionCen.id]);
                        }catch(Exception e){
                            e.printStackTrace()
                        }
                    }else{
                      //  println "si existe ..."
                    }

                }
    }


def dataSourceResolve(ip, bd, user, password){

    def urlJdbc='jdbc:mysql://'+ip+"/"+bd
    def driverManagerDs = new DriverManagerDataSource()
   	driverManagerDs.driverClassName = "com.mysql.jdbc.Driver"
    driverManagerDs.url = urlJdbc
    driverManagerDs.username = user
    driverManagerDs.password = password

    return driverManagerDs

}

def dataSourceResolve(urlJdbc, user, password){

    def driverManagerDs = new DriverManagerDataSource()
   	driverManagerDs.driverClassName = "com.mysql.jdbc.Driver"
    driverManagerDs.url = urlJdbc
    driverManagerDs.username = user
    driverManagerDs.password = password

    return driverManagerDs

}

def getSql(dataSource){
    def sql=new Sql(dataSource)
    return sql
}

def getSucursal(sucName){
def sucursal=Sucursal.findByNombre(sucName) 
return sucursal
}

def getConfig(entity){
    def config = EntityConfiguration.findByName(entity)
    return config
}




}
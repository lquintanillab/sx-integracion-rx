package sx

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes = ['id,version,sw2,dateCreated,lastUpdated'],includeNames=true,includePackage=false)
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class CuentaPorCobrar {

    String	id

    Cliente	cliente

    Sucursal sucursal

    String tipoDocumento

    Long	documento	 = 0

    BigDecimal	importe	 = 0

    BigDecimal descuentoImporte = 0

    BigDecimal subtotal = 0

    BigDecimal	impuesto	 = 0

    BigDecimal	total	 = 0

    String	formaDePago

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal	tipoDeCambio	 = 1

    BigDecimal	cargo	 = 0

    String	comentario

    String	sw2

    String	uuid

    String tipo

    Date fecha

    Date vencimiento

    Date dateCreated

    Date lastUpdated

    String createUser

    String updateUser

    BigDecimal pagos = 0.0

    BigDecimal saldo = 0.0

    Integer atraso = 0

    Boolean chequePostFechado = false

    Date cancelada

    String cancelacionUsuario

    String cancelacionMotivo


    static constraints = {
        tipoDocumento inList:['VENTA','CHEQUE_DEVUELTO','DEVOLUCION_CLIENTE','NOTA_DE_CARGO']
        tipo nullable:true, inList:['CON','COD','CRE','PSF','INE','OTR','ACF','ANT','AND']
        documento maxSize: 20
        uuid nullable:true, unique:true
        tipoDeCambio(scale:6)
        uuid nullable: true
        comentario nullable:true
        sw2 nullable:true
        chequePostFechado nullable: true
        cancelada nullable: true
        cancelacionUsuario nullable: true
        cancelacionMotivo nullable: true
        vencimiento nullable: true
    }


    static mapping = {
        id generator:'uuid'
        fecha type:'date' ,index: 'CXC_IDX1'
        vencimiento type: 'date'
        cliente index: 'CXC_IDX3'
        cancelada type: 'date'
        pagos formula:'(select COALESCE(sum(x.importe),0) from aplicacion_de_cobro x where x.cuenta_por_cobrar_id=id)'
    }

    static transients = ['saldo','folio','atraso']

    BigDecimal getSaldo() {
        return total - pagos
    }

    String getFolio() {
        return "${tipo}-${documento}"
    }

    Integer getAtraso() {
        if (vencimiento) {
            return vencimiento - fecha
        }
        return 0
    }




}

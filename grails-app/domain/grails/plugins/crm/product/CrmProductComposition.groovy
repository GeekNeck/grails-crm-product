package grails.plugins.crm.product

/**
 * A product composition is a product that includes or excludes other products.
 */
class CrmProductComposition {

    public static final int EXCLUDES = -1
    public static final int INCLUDES = 0
    public static final int DEPENDS = 1
    public static final int EQUIVALENT = 2
    public static final int REPLACES = 3
    public static final int OPTION = 4

    CrmProduct product
    int type

    static belongsTo = [mainProduct: CrmProduct]

    static constraints = {
        type(inList: [EXCLUDES, INCLUDES, DEPENDS, EQUIVALENT, REPLACES, OPTION])
    }

    String toString() {
        product.toString()
    }
}

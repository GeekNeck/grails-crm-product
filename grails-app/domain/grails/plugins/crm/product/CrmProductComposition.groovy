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
    public static final int RELATED = 5

    public static final Map<String, Integer> TYPE_SYMBOLS = [excludes: EXCLUDES, includes: INCLUDES, depends: DEPENDS,
            equivalent: EQUIVALENT, replaces: REPLACES, option: OPTION, related: RELATED].asImmutable()

    CrmProduct product
    Double quantity
    int type

    static belongsTo = [mainProduct: CrmProduct]

    static constraints = {
        quantity(nullable: true)
        type(inList: TYPE_SYMBOLS.values().toList())
    }

    static transients = ['typeSymbol']

    String getTypeSymbol() {
        TYPE_SYMBOLS.find{it.value == type}?.key
    }

    void setTypeSymbol(String arg) {
        type = (TYPE_SYMBOLS[arg] ?: 0)
    }

    String toString() {
        product.toString()
    }
}

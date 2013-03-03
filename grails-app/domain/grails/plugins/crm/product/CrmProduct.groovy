/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.product

import grails.plugins.crm.contact.CrmContact
import grails.plugins.crm.core.TenantEntity
import grails.plugins.crm.core.TenantUtils

@TenantEntity
class CrmProduct {

    public static final List BIND_WHITELIST = ['number', 'name', 'displayNumber', 'displayName', 'description',
            'supplier', 'suppliersNumber', 'group', 'barcode', 'customsCode', 'weight', 'enabled', 'prices']

    String number
    String name
    String displayNumber
    String displayName
    String description
    String suppliersNumber
    CrmContact supplier
    CrmProductGroup group
    String barcode
    String customsCode
    Float weight // TODO should weight be in CrmProductPrice instead?
    boolean enabled

    static hasMany = [prices: CrmProductPrice, compositions: CrmProductComposition]
    static mappedBy = [compositions: 'mainProduct']

    static constraints = {
        number(maxSize: 40, blank: false, unique: 'tenantId')
        name(maxSize: 255, blank: false)
        displayNumber(maxSize: 80, nullable: true)
        displayName(maxSize: 255, nullable: true)
        description(maxSize: 2000, nullable: true, widget: 'textarea')
        supplier(nullable: true)
        suppliersNumber(maxSize: 40, nullable: true)
        group()
        barcode(maxSize: 255, nullable: true)
        customsCode(maxSize: 10, nullable: true)
        weight(nullable: true)
    }

    static mapping = {
        sort "number"
        prices sort: 'fromAmount', 'asc'
    }

    static transients = ['productPrice', 'price', 'vat', 'related', 'includes', 'excludes', 'depends']

    static taggable = true
    static attachmentable = true
    static dynamicProperties = true
    static relatable = true
    static auditable = true

    transient CrmProductPrice getProductPrice(Float amount = 0f, Object priceList = null, String unit = null) {
        if (!prices) {
            return null
        }
        if (priceList) {
            if (!(priceList instanceof CrmPriceList)) {
                priceList = CrmPriceList.findByParamAndTenantId(priceList.toString(), TenantUtils.tenant)
                if (!priceList) {
                    return null
                }
            }
        } else {
            // Grab the first price list ordered by it's orderIndex
            priceList = prices.sort { it.priceList.orderIndex }.head().priceList
        }
        if (!amount) {
            amount = 1
        }
        // Filter by price list
        def tmp = prices.findAll { it.priceList == priceList }
        if (!unit) {
            // Grab first unit we find in the list
            unit = tmp.collect { it.unit }.sort().head()
        }
        tmp = tmp.findAll { it.unit == unit }
        // Sort by descending fromAmount to find the best matching price
        tmp.sort { it.fromAmount }.reverse().find { it.fromAmount == null || it.fromAmount <= amount }
    }

    transient Float getPrice(Float amount = 0f, Object priceList = null, String unit = null) {
        getProductPrice(amount, priceList, unit)?.outPrice
    }

    transient Float getVat(Float amount = 0f, Object priceList = null, String unit = null) {
        getProductPrice(amount, priceList, unit)?.vat
    }

    transient List<CrmProduct> getRelated() {
        def result = []
        for (c in compositions) {
            if (c.type == CrmProductComposition.RELATED) {
                result << c.product
            }
        }
        return result
    }

    def addRelated(CrmProduct related, Float quantity = null) {
        if (related == this) {
            throw new IllegalArgumentException("self reference")
        }
        if (!compositions?.find { it.product == related }) {
            addToCompositions(product: related, quantity: quantity, type: CrmProductComposition.RELATED)
        }
        return this
    }

    transient List<CrmProduct> getIncludes() {
        def result = []
        for (c in compositions) {
            if (c.type == CrmProductComposition.INCLUDES) {
                result << c.product
            }
        }
        return result
    }

    def addIncludes(CrmProduct included, Float quantity = null) {
        if (included == this) {
            throw new IllegalArgumentException("self reference")
        }
        if (!compositions?.find { it.product == included }) {
            addToCompositions(product: included, quantity: quantity, type: CrmProductComposition.INCLUDES)
        }
        return this
    }

    transient List<CrmProduct> getExcludes() {
        def result = []
        for (c in compositions) {
            if (c.type == CrmProductComposition.EXCLUDES) {
                result << c.product
            }
        }
        return result
    }

    def addExcludes(CrmProduct excluded, Float quantity = null) {
        if (excluded == this) {
            throw new IllegalArgumentException("self reference")
        }
        if (!compositions?.find { it.product == excluded }) {
            addToCompositions(product: excluded, quantity: quantity, type: CrmProductComposition.EXCLUDES)
        }
        return this
    }

    transient List<CrmProduct> getDepends() {
        def result = []
        for (c in compositions) {
            if (c.type == CrmProductComposition.DEPENDS) {
                result << c.product
            }
        }
        return result
    }

    def addDepends(CrmProduct dependedOn, Float quantity = null) {
        if (dependedOn == this) {
            throw new IllegalArgumentException("self reference")
        }
        if (!compositions?.find { it.product == dependedOn }) {
            addToCompositions(product: dependedOn, quantity: quantity, type: CrmProductComposition.DEPENDS)
        }
        return this
    }

    String toString() {
        name
    }
}

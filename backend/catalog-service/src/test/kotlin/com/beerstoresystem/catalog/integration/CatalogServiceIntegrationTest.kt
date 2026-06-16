package com.beerstoresystem.catalog.integration

import com.beerstoresystem.catalog.application.CatalogUseCase
import com.beerstoresystem.catalog.infrastructure.persistence.entity.BeerEntity
import com.beerstoresystem.catalog.infrastructure.persistence.entity.BeerStyleEntity
import com.beerstoresystem.catalog.infrastructure.persistence.entity.BreweryEntity
import com.beerstoresystem.catalog.infrastructure.persistence.entity.ProductVariantEntity
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.BeerJpaRepository
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.BeerStyleJpaRepository
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.BreweryJpaRepository
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.ProductVariantJpaRepository
import com.beerstoresystem.catalog.infrastructure.persistence.jpa.ProductViewJpaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@Transactional
class CatalogServiceIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
    }

    @Autowired
    private lateinit var catalogUseCase: CatalogUseCase

    @Autowired
    private lateinit var breweryJpaRepository: BreweryJpaRepository

    @Autowired
    private lateinit var beerStyleJpaRepository: BeerStyleJpaRepository

    @Autowired
    private lateinit var beerJpaRepository: BeerJpaRepository

    @Autowired
    private lateinit var productVariantJpaRepository: ProductVariantJpaRepository

    @Autowired
    private lateinit var productViewJpaRepository: ProductViewJpaRepository

    private fun saveStyle(name: String = "IPA"): BeerStyleEntity =
        beerStyleJpaRepository.save(BeerStyleEntity().apply { this.name = name })

    private fun saveBrewery(name: String = "Test Brewery"): BreweryEntity =
        breweryJpaRepository.save(BreweryEntity().apply { this.name = name })

    private fun saveBeer(name: String, style: BeerStyleEntity, brewery: BreweryEntity, active: Boolean = true): BeerEntity =
        beerJpaRepository.save(BeerEntity().apply {
            this.name = name; this.style = style; this.brewery = brewery
            this.isActive = active; this.abv = BigDecimal("5.0")
        })

    private fun saveVariant(beer: BeerEntity, sku: String, price: String = "9.99", active: Boolean = true): ProductVariantEntity =
        productVariantJpaRepository.save(ProductVariantEntity().apply {
            this.beer = beer; this.sku = sku
            unitPrice = BigDecimal(price); isActive = active; volumeMl = 500
        })

    @Test
    fun `getAllStyles returns saved styles`() {
        saveStyle("IPA")
        saveStyle("Stout")

        val result = catalogUseCase.getAllStyles()

        assertTrue(result.any { it.name == "IPA" })
        assertTrue(result.any { it.name == "Stout" })
    }

    @Test
    fun `getAllBreweries returns saved breweries`() {
        saveBrewery("Craft Co")

        val result = catalogUseCase.getAllBreweries()

        assertTrue(result.any { it.name == "Craft Co" })
    }

    @Test
    fun `getBeerDetail throws when beer not found`() {
        assertThrows<NoSuchElementException> { catalogUseCase.getBeerDetail(999999L) }
    }

    @Test
    fun `getVariantById throws when variant not found`() {
        assertThrows<NoSuchElementException> { catalogUseCase.getVariantById(999999L) }
    }

    @Test
    fun `getCatalog returns active beers and filters inactive variants`() {
        val style = saveStyle("Lager")
        val brewery = saveBrewery("Big Brew")
        val beer = saveBeer("Gold Lager", style, brewery)
        saveVariant(beer, "GL-500", active = true)
        saveVariant(beer, "GL-1000", active = false)

        val result = catalogUseCase.getCatalog(0, 20)

        val found = result.content.find { it.beer.name == "Gold Lager" }
        assertNotNull(found)
        assertEquals(1, found!!.variants.size)
        assertEquals("GL-500", found.variants[0].sku)
    }

    @Test
    fun `getBeerDetail returns beer with all variants`() {
        val style = saveStyle("Porter")
        val brewery = saveBrewery("Dark Arts")
        val beer = saveBeer("Midnight Porter", style, brewery)
        saveVariant(beer, "MP-330", "8.50")

        val result = catalogUseCase.getBeerDetail(beer.id)

        assertEquals("Midnight Porter", result.beer.name)
        assertEquals(1, result.variants.size)
        assertEquals("MP-330", result.variants[0].sku)
    }

    @Test
    fun `getVariantById returns variant`() {
        val style = saveStyle("Wheat")
        val brewery = saveBrewery("Wheat Farm")
        val beer = saveBeer("Wheat Beer", style, brewery)
        val variant = saveVariant(beer, "WB-500", "6.99")

        val result = catalogUseCase.getVariantById(variant.id)

        assertEquals("WB-500", result.sku)
        assertEquals(BigDecimal("6.99"), result.unitPrice)
    }

    @Test
    fun `recordView saves product view`() {
        val style = saveStyle("Ale")
        val brewery = saveBrewery("Ale House")
        val beer = saveBeer("Pale Ale", style, brewery)
        val variant = saveVariant(beer, "PA-500")
        val countBefore = productViewJpaRepository.count()

        catalogUseCase.recordView(variant.id, customerId = 42L)

        assertEquals(countBefore + 1, productViewJpaRepository.count())
    }
}

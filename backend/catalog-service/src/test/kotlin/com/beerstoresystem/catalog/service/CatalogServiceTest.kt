package com.beerstoresystem.catalog.service

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import com.beerstoresystem.catalog.application.CatalogApplicationService
import com.beerstoresystem.catalog.domain.model.Beer
import com.beerstoresystem.catalog.domain.model.BeerStyle
import com.beerstoresystem.catalog.domain.model.Brewery
import com.beerstoresystem.catalog.domain.model.ProductVariant
import com.beerstoresystem.catalog.domain.repository.BeerRepository
import com.beerstoresystem.catalog.domain.repository.BeerStyleRepository
import com.beerstoresystem.catalog.domain.repository.BreweryRepository
import com.beerstoresystem.catalog.domain.repository.ProductVariantRepository
import com.beerstoresystem.catalog.domain.repository.ProductViewRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class CatalogServiceTest {

    private val beerRepository = mockk<BeerRepository>()
    private val beerStyleRepository = mockk<BeerStyleRepository>()
    private val breweryRepository = mockk<BreweryRepository>()
    private val productVariantRepository = mockk<ProductVariantRepository>()
    private val productViewRepository = mockk<ProductViewRepository>()

    private val service = CatalogApplicationService(
        beerRepository, beerStyleRepository, breweryRepository,
        productVariantRepository, productViewRepository
    )

    private fun makeBeer(id: Long = 1L, name: String = "Test Beer"): Beer {
        val style = BeerStyle(id = 10, name = "IPA", description = null)
        val brewery = Brewery(id = 20, name = "Test Brewery", websiteUrl = null, foundedYear = null, country = null)
        return Beer(
            id = id,
            name = name,
            description = null,
            abv = BigDecimal("5.0"),
            isActive = true,
            brewery = brewery,
            style = style,
            createdAt = null
        )
    }

    private fun makeVariant(id: Long = 100L, isActive: Boolean = true): ProductVariant =
        ProductVariant(
            id = id,
            sku = "SKU-001",
            volumeMl = 500,
            unitPrice = BigDecimal("9.99"),
            isActive = isActive,
            beer = null,
            packageType = null,
            createdAt = null
        )

    @Test
    fun `getCatalog returns paged beers`() {
        val beer = makeBeer()
        val variant = makeVariant()
        val page = PageImpl(listOf(beer), PageRequest.of(0, 10), 1)
        every { beerRepository.findAllActiveWithDetails(any()) } returns page
        every { productVariantRepository.findAllByBeerIdWithDetails(beer.id) } returns listOf(variant)

        val result = service.getCatalog(0, 10)

        assertEquals(1, result.content.size)
        assertEquals("Test Beer", result.content[0].beer.name)
        assertEquals(1L, result.totalElements)
        assertEquals(0, result.page)
    }

    @Test
    fun `getCatalog filters inactive variants`() {
        val beer = makeBeer()
        val active = makeVariant(id = 100L, isActive = true)
        val inactive = makeVariant(id = 200L, isActive = false).copy(sku = "SKU-002")
        val page = PageImpl(listOf(beer), PageRequest.of(0, 10), 1)
        every { beerRepository.findAllActiveWithDetails(any()) } returns page
        every { productVariantRepository.findAllByBeerIdWithDetails(beer.id) } returns listOf(active, inactive)

        val result = service.getCatalog(0, 10)

        assertEquals(1, result.content[0].variants.size)
        assertEquals("SKU-001", result.content[0].variants[0].sku)
    }

    @Test
    fun `getBeerDetail returns detail when found`() {
        val beer = makeBeer(id = 5L)
        val variant = makeVariant()
        every { beerRepository.findByIdWithDetails(5L) } returns beer
        every { productVariantRepository.findAllByBeerIdWithDetails(5L) } returns listOf(variant)

        val result = service.getBeerDetail(5L)

        assertEquals(5L, result.beer.id)
        assertEquals("Test Beer", result.beer.name)
        assertEquals(1, result.variants.size)
    }

    @Test
    fun `getBeerDetail throws when not found`() {
        every { beerRepository.findByIdWithDetails(99L) } returns null

        assertThrows<NoSuchElementException> { service.getBeerDetail(99L) }
    }

    @Test
    fun `getVariantById throws when not found`() {
        every { productVariantRepository.findByIdWithDetails(99L) } returns null

        assertThrows<NoSuchElementException> { service.getVariantById(99L) }
    }

    @Test
    fun `getVariantById returns variant when found`() {
        val variant = makeVariant()
        every { productVariantRepository.findByIdWithDetails(100L) } returns variant

        val result = service.getVariantById(100L)

        assertEquals("SKU-001", result.sku)
        assertEquals(BigDecimal("9.99"), result.unitPrice)
    }

    @Test
    fun `getAllStyles returns list`() {
        val style = BeerStyle(id = 1, name = "Stout", description = null)
        every { beerStyleRepository.findAll() } returns listOf(style)

        val result = service.getAllStyles()

        assertEquals(1, result.size)
        assertEquals("Stout", result[0].name)
    }

    @Test
    fun `getAllBreweries returns list`() {
        val brewery = Brewery(id = 1, name = "Craft Brewing Co", websiteUrl = null, foundedYear = null, country = null)
        every { breweryRepository.findAll() } returns listOf(brewery)

        val result = service.getAllBreweries()

        assertEquals(1, result.size)
        assertEquals("Craft Brewing Co", result[0].name)
    }

    @Test
    fun `recordView saves product view`() {
        val variant = makeVariant()
        every { productVariantRepository.findByIdOrNull(100L) } returns variant
        every { productViewRepository.save(any()) } returns mockk()

        service.recordView(100L, customerId = 42L)

        verify { productViewRepository.save(any()) }
    }

    @Test
    fun `recordView throws when variant not found`() {
        every { productVariantRepository.findByIdOrNull(999L) } returns null

        assertThrows<NoSuchElementException> { service.recordView(999L, null) }
    }
}

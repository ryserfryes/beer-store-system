package com.beerstoresystem.catalog.application

import com.beerstoresystem.catalog.domain.model.BeerStyle
import com.beerstoresystem.catalog.domain.model.BeerWithVariants
import com.beerstoresystem.catalog.domain.model.Brewery
import com.beerstoresystem.catalog.domain.model.CatalogPage
import com.beerstoresystem.catalog.domain.model.ProductVariant
import com.beerstoresystem.catalog.domain.model.ProductView
import com.beerstoresystem.catalog.domain.repository.BeerRepository
import com.beerstoresystem.catalog.domain.repository.BeerStyleRepository
import com.beerstoresystem.catalog.domain.repository.BreweryRepository
import com.beerstoresystem.catalog.domain.repository.ProductVariantRepository
import com.beerstoresystem.catalog.domain.exception.NotFoundException
import com.beerstoresystem.catalog.domain.repository.ProductViewRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
@Transactional(readOnly = true)
class CatalogApplicationService(
    private val beerRepository: BeerRepository,
    private val beerStyleRepository: BeerStyleRepository,
    private val breweryRepository: BreweryRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val productViewRepository: ProductViewRepository
) : CatalogUseCase {

    override fun getCatalog(page: Int, size: Int): CatalogPage {
        val pageable = PageRequest.of(page, size)
        val beerPage = beerRepository.findAllActiveWithDetails(pageable)

        val content = beerPage.content.map { beer ->
            val variants = productVariantRepository
                .findAllByBeerIdWithDetails(beer.id)
                .filter { it.isActive }
            BeerWithVariants(beer = beer, variants = variants)
        }

        return CatalogPage(
            content = content,
            page = page,
            size = size,
            totalElements = beerPage.totalElements,
            totalPages = beerPage.totalPages
        )
    }

    override fun getBeerDetail(id: Long): BeerWithVariants {
        val beer = beerRepository.findByIdWithDetails(id)
            ?: throw NotFoundException("Beer not found: $id")
        val variants = productVariantRepository.findAllByBeerIdWithDetails(id)
        return BeerWithVariants(beer = beer, variants = variants)
    }

    override fun getVariantById(id: Long): ProductVariant {
        return productVariantRepository.findByIdWithDetails(id)
            ?: throw NotFoundException("Variant not found: $id")
    }

    override fun getAllStyles(): List<BeerStyle> =
        beerStyleRepository.findAll()

    override fun getAllBreweries(): List<Brewery> =
        breweryRepository.findAll()

    override fun getVariantsByIds(ids: List<Long>): List<ProductVariant> =
        productVariantRepository.findAllByIdInWithDetails(ids)

    @Transactional
    override fun recordView(variantId: Long, customerId: Long?) {
        productVariantRepository.findByIdOrNull(variantId)
            ?: throw NotFoundException("Variant not found: $variantId")
        val view = ProductView(
            id = 0,
            variantId = variantId,
            customerId = customerId,
            viewedAt = OffsetDateTime.now()
        )
        productViewRepository.save(view)
    }
}

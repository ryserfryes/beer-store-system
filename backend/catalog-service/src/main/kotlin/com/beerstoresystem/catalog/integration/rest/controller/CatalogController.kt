package com.beerstoresystem.catalog.integration.rest.controller

import com.beerstoresystem.catalog.application.CatalogUseCase
import com.beerstoresystem.catalog.integration.rest.dto.BeerDetailDto
import com.beerstoresystem.catalog.integration.rest.dto.BeerStyleDto
import com.beerstoresystem.catalog.integration.rest.dto.BreweryDto
import com.beerstoresystem.catalog.integration.rest.dto.CatalogPageDto
import com.beerstoresystem.catalog.integration.rest.dto.RecordViewRequest
import com.beerstoresystem.catalog.integration.rest.dto.VariantDto
import com.beerstoresystem.catalog.integration.rest.mapper.toDetailDto
import com.beerstoresystem.catalog.integration.rest.mapper.toDto
import com.beerstoresystem.catalog.integration.rest.mapper.toSummaryDto
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/catalog")
class CatalogController(
    private val catalogUseCase: CatalogUseCase
) {

    @GetMapping
    fun getCatalog(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): CatalogPageDto {
        val catalogPage = catalogUseCase.getCatalog(page, size)
        return CatalogPageDto(
            content = catalogPage.content.map { it.toSummaryDto() },
            page = catalogPage.page,
            size = catalogPage.size,
            totalElements = catalogPage.totalElements,
            totalPages = catalogPage.totalPages
        )
    }

    @GetMapping("/beers/{id}")
    fun getBeerById(@PathVariable id: Long): BeerDetailDto {
        val beerWithVariants = catalogUseCase.getBeerDetail(id)
        return beerWithVariants.beer.toDetailDto(beerWithVariants.variants)
    }

    @GetMapping("/variants/{id}")
    fun getVariantById(@PathVariable id: Long): VariantDto =
        catalogUseCase.getVariantById(id).toDto()

    @GetMapping("/styles")
    fun getAllStyles(): List<BeerStyleDto> =
        catalogUseCase.getAllStyles().map { it.toDto() }

    @GetMapping("/breweries")
    fun getAllBreweries(): List<BreweryDto> =
        catalogUseCase.getAllBreweries().map { it.toDto() }

    @PostMapping("/variants/{id}/view")
    fun recordView(
        @PathVariable id: Long,
        @RequestBody(required = false) body: RecordViewRequest?
    ): Map<String, String> {
        catalogUseCase.recordView(id, body?.customerId)
        return mapOf("status" to "ok")
    }
}

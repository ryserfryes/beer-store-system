package com.beerstoresystem.gateway

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import java.net.URI

@RestController
class GatewayController(
    @Value("\${services.catalog}") catalogUrl: String,
    @Value("\${services.warehouse}") warehouseUrl: String,
    @Value("\${services.supply}") supplyUrl: String,
    @Value("\${services.order}") orderUrl: String,
    @Value("\${services.review}") reviewUrl: String,
) {
    private val restClient = RestClient.create()

    private val routes = listOf(
        "/api/catalog" to catalogUrl,
        "/api/warehouses" to warehouseUrl,
        "/api/pickup-points" to warehouseUrl,
        "/api/suppliers" to supplyUrl,
        "/api/purchase-orders" to supplyUrl,
        "/api/customers" to orderUrl,
        "/api/orders" to orderUrl,
        "/api/employees" to orderUrl,
        "/api/reviews" to reviewUrl,
    )

    @RequestMapping("/**")
    fun proxy(request: HttpServletRequest, @RequestBody(required = false) body: ByteArray?): ResponseEntity<ByteArray> {
        val path = request.requestURI
        val targetBase = routes.firstOrNull { (prefix, _) -> path.startsWith(prefix) }?.second
            ?: return ResponseEntity.notFound().build()

        val query = request.queryString?.let { "?$it" } ?: ""
        val targetUri = URI.create("$targetBase$path$query")

        return restClient.method(HttpMethod.valueOf(request.method))
            .uri(targetUri)
            .headers { h ->
                request.headerNames.asSequence()
                    .filter { it.lowercase() !in setOf("host", "content-length") }
                    .forEach { name -> h[name] = request.getHeader(name) }
            }
            .apply { if (body != null && body.isNotEmpty()) body(body) }
            .exchange { _, response ->
                ResponseEntity.status(response.statusCode)
                    .headers(response.headers)
                    .body(response.body.readBytes())
            }
    }
}

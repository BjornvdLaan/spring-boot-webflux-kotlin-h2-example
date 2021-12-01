package nl.bjornvanderlaan.springwebfluxkotlinfunctional.handler

import kotlinx.coroutines.flow.map
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.model.CatDto
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.model.toDto
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.model.toEntity
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.repository.CatRepository
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBodyOrNull
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

@Component
class CatHandler(
    private val catRepository: CatRepository
) {
    suspend fun getAll(req: ServerRequest): ServerResponse {
        return ServerResponse
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyAndAwait(
                catRepository.findAll().map { it.toDto() }
            )
    }

    suspend fun getById(req: ServerRequest): ServerResponse {
        val id = Integer.parseInt(req.pathVariable("id"))
        val existingCat = catRepository.findById(id.toLong())

        return existingCat?.let {
            ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait(it)
        } ?: ServerResponse.notFound().buildAndAwait()
    }

    suspend fun add(req: ServerRequest): ServerResponse {
        val receivedCat = req.awaitBodyOrNull(CatDto::class)

        return receivedCat?.let {
            ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValueAndAwait(
                    catRepository
                        .save(it.toEntity())
                        .toDto()
                )
        } ?: ServerResponse.badRequest().buildAndAwait()
    }

    suspend fun update(req: ServerRequest): ServerResponse {
        val id = req.pathVariable("id")

        val receivedCat = req.awaitBodyOrNull(CatDto::class) ?: return ServerResponse.badRequest().buildAndAwait()
        val existingCat = catRepository.findById(id.toLong()) ?: return ServerResponse.notFound().buildAndAwait()

        return ServerResponse
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValueAndAwait(
                catRepository.save(
                    receivedCat.toEntity().copy(id = existingCat.id)
                ).toDto()
            )
    }


    suspend fun delete(req: ServerRequest): ServerResponse {
        val id = req.pathVariable("id")

        return if (catRepository.existsById(id.toLong())) {
            catRepository.deleteById(id.toLong())
            ServerResponse.noContent().buildAndAwait()
        } else {
            ServerResponse.notFound().buildAndAwait()
        }
    }
}
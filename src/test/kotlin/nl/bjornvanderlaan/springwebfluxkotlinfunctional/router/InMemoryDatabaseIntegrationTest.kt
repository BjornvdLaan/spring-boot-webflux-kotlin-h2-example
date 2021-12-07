package nl.bjornvanderlaan.springwebfluxkotlinfunctional.router

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.model.Cat
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.model.CatDto
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.model.toDto
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.repository.CatRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.web.reactive.function.BodyInserters.fromValue


@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InMemoryDatabaseIntegrationTest(
    @Autowired val client: WebTestClient,
    @Autowired val repository: CatRepository
) {
    private fun aCat(
        name: String = "Obi",
        type: String = "Dutch Ringtail",
        age: Int = 3
    ) =
        Cat(
            name = name,
            type = type,
            age = age
        )

    private fun anotherCat(
        name: String = "Wan",
        type: String = "Japanese Bobtail",
        age: Int = 5
    ) =
        aCat(
            name = name,
            type = type,
            age = age
        )

    private fun CatRepository.seed(vararg cats: Cat) =
        runBlocking {
            repository.saveAll(cats.toList()).toList()
        }

    @AfterEach
    fun afterEach() {
        runBlocking {
            repository.deleteAll()
        }
    }

    @Test
    fun `Retrieve all cats`() {
        repository.seed(aCat(), anotherCat())

        client
            .get()
            .uri("/api/cats")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<CatDto>()
            .hasSize(2)
            .contains(aCat().toDto(), anotherCat().toDto())
    }

    @Test
    fun `Retrieve cat by existing id`() {
        repository.seed(aCat(), anotherCat())

        client
            .get()
            .uri("/api/cats/2")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CatDto>()
            .isEqualTo(anotherCat().toDto())
    }

    @Test
    fun `Retrieve cat by non-existing id`() {
        client
            .get()
            .uri("/api/cats/2")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `Add a new cat`() {
        client
            .post()
            .uri("/api/cats")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(aCat().toDto())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CatDto>()
            .isEqualTo(aCat().toDto())
    }

    @Test
    fun `Add a new cat with empty request body`() {
        client
            .post()
            .uri("/api/cats")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(fromValue("{}"))
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `Update a cat`() {
        repository.seed(aCat(), anotherCat())

        val updatedCat = aCat(name = "New fancy name").toDto()
        client
            .put()
            .uri("/api/cats/2")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedCat)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CatDto>()
            .isEqualTo(updatedCat)
    }

    @Test
    fun `Update cat with non-existing id`() {
        val updatedCat = aCat(name = "New fancy name").toDto()

        client
            .put()
            .uri("/api/cats/2")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedCat)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `Update cat with empty request body id`() {
        client
            .put()
            .uri("/api/cats/2")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(fromValue("{}"))
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `Delete cat with existing id`() {
        repository.seed(aCat(), anotherCat())

        client
            .delete()
            .uri("/api/cats/2")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `Delete cat by non-existing id`() {
        client
            .delete()
            .uri("/api/cats/2")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}


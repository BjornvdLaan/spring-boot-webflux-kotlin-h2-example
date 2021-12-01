package nl.bjornvanderlaan.springwebfluxkotlinfunctional.router

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.slot
import kotlinx.coroutines.flow.flow
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.model.Cat
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.model.CatDto
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.model.toDto
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.repository.CatRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.web.reactive.function.BodyInserters.fromValue


@SpringBootTest
class BindToRouterVariantCatRouterTest(
    @Autowired val configuration: CatRouterConfiguration
) {
    @MockkBean
    private lateinit var repository: CatRepository

    private lateinit var client: WebTestClient

    @BeforeEach
    fun beforeEach() {
        this.client = WebTestClient
            .bindToRouterFunction(configuration.apiRouter())
            .configureClient()
            .baseUrl("/api/cats")
            .build()
    }

    @Test
    fun `Retrieve all cats`() {
        every {
            repository.findAll()
        } returns flow {
            emit(aCat())
            emit(anotherCat())
        }

        client
            .get()
            .uri("/")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList<CatDto>()
            .hasSize(2)
            .contains(aCat().toDto(), anotherCat().toDto())
    }

    @Test
    fun `Retrieve cat by existing id`() {
        val requestedId = slot<Long>()
        coEvery {
            repository.findById(capture(requestedId))
        } coAnswers {
            aCat(id = requestedId.captured)
        }

        client
            .get()
            .uri("/2")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CatDto>()
            .isEqualTo(aCat().toDto())
    }

    @Test
    fun `Retrieve cat by non-existing id`() {
        coEvery {
            repository.findById(any())
        } returns null

        client
            .get()
            .uri("/2")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `Add a new cat`() {
        val savedCat = slot<Cat>()
        coEvery {
            repository.save(capture(savedCat))
        } coAnswers {
            savedCat.captured
        }

        client
            .post()
            .uri("/")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(aCat().toDto())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<CatDto>()
            .isEqualTo(savedCat.captured.toDto())
    }

    @Test
    fun `Add a new cat with empty request body`() {
        val savedCat = slot<Cat>()
        coEvery {
            repository.save(capture(savedCat))
        } coAnswers {
            savedCat.captured
        }

        client
            .post()
            .uri("/")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(fromValue("{}"))
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `Update a cat`() {
        val requestedId = slot<Long>()
        coEvery {
            repository.findById(capture(requestedId))
        } coAnswers {
            aCat(id = requestedId.captured)
        }

        val savedCat = slot<Cat>()
        coEvery {
            repository.save(capture(savedCat))
        } coAnswers {
            savedCat.captured
        }

        val updatedCat = aCat(name = "New fancy name").toDto()

        client
            .put()
            .uri("/2")
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
        val requestedId = slot<Long>()
        coEvery {
            repository.findById(capture(requestedId))
        } coAnswers {
            nothing
        }

        val updatedCat = aCat(name = "New fancy name").toDto()

        client
            .put()
            .uri("/2")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedCat)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `Update cat with empty request body id`() {
        val requestedId = slot<Long>()
        coEvery {
            repository.findById(capture(requestedId))
        } coAnswers {
            aCat(id = requestedId.captured)
        }

        client
            .put()
            .uri("/2")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(fromValue("{}"))
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `Delete cat with existing id`() {
        coEvery {
            repository.existsById(any())
        } coAnswers {
            true
        }

        coEvery {
            repository.deleteById(any())
        } coAnswers {
            nothing
        }

        client
            .delete()
            .uri("/2")
            .exchange()
            .expectStatus()
            .isNoContent

        coVerify { repository.deleteById(any()) }
    }

    @Test
    fun `Delete cat by non-existing id`() {
        coEvery {
            repository.existsById(any())
        } coAnswers {
            false
        }

        client
            .delete()
            .uri("/2")
            .exchange()
            .expectStatus()
            .isNotFound

        coVerify(exactly = 0) { repository.deleteById(any()) }
    }

    private fun aCat(
        id: Long = 1,
        name: String = "Obi",
        type: String = "Dutch Ringtail",
        age: Int = 3
    ) =
        Cat(
            id = id,
            name = name,
            type = type,
            age = age
        )

    private fun anotherCat(
        id: Long = 1,
        name: String = "Wan",
        type: String = "Japanese Bobtail",
        age: Int = 5
    ) =
        aCat(
            id = id,
            name = name,
            type = type,
            age = age
        )
}


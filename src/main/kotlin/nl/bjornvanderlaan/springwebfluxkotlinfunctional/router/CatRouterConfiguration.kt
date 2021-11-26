package nl.bjornvanderlaan.springwebfluxkotlinfunctional.router

import nl.bjornvanderlaan.springwebfluxkotlinfunctional.handler.CatHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class CatRouterConfiguration(
    private val catHandler: CatHandler
) {
    @Bean
    fun apiRouter() = coRouter {
        "/api/cats".nest {
            accept(APPLICATION_JSON).nest {
                GET("", catHandler::getAll)

                contentType(APPLICATION_JSON).nest {
                    POST("", catHandler::add)
                }

                "/{id}".nest {
                    GET("", catHandler::getById)
                    DELETE("", catHandler::delete)

                    contentType(APPLICATION_JSON).nest {
                        PUT("", catHandler::update)
                    }
                }
            }
        }
    }
}


package nl.bjornvanderlaan.springwebfluxkotlinfunctional.repository

import kotlinx.coroutines.flow.Flow
import nl.bjornvanderlaan.springwebfluxkotlinfunctional.model.Cat
import org.springframework.data.repository.kotlin.CoroutineCrudRepository


interface CatRepository : CoroutineCrudRepository<Cat, Long> {
    override fun findAll(): Flow<Cat>
    override suspend fun findById(id: Long): Cat?
}
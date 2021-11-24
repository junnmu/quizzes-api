package com.example.quizzesapi

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.Valid
import javax.validation.constraints.*

@Entity
data class Category(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String
)

@Repository
interface CategoryRepository : JpaRepository<Category, UUID> {
    fun existsByName(name: String): Boolean
    fun existsByNameAndIdIsNot(name: String, id: UUID): Boolean
}

data class CategoryReq(
    @field:[NotNull NotBlank Size(min = 3, max = 20)]
    val name: String
)

@RestController
@RequestMapping("categories")
class CategoryController(val categoryRepository: CategoryRepository) {

    @GetMapping
    fun index() = ResponseEntity.ok(categoryRepository.findAll())

    @PostMapping
    fun create(@Valid @RequestBody categoryReq: CategoryReq): ResponseEntity<Category> {
        if (categoryRepository.existsByName(categoryReq.name))
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category already exists")

        val category = Category(name = categoryReq.name)
        categoryRepository.save(category)
        return ResponseEntity(category, HttpStatus.CREATED)
    }

    @GetMapping("{id}")
    fun show(@PathVariable id: UUID): ResponseEntity<Category> {
        val category = getCategory(id)
        return ResponseEntity.ok(category)
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody categoryReq: CategoryReq): ResponseEntity<Category> {
        if (categoryRepository.existsByNameAndIdIsNot(categoryReq.name, id))
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category already exists")

        val category = getCategory(id)
        val updatedCategory = category.copy(name = categoryReq.name)
        categoryRepository.save(updatedCategory)
        return ResponseEntity.ok(updatedCategory)
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Category> {
        val category = getCategory(id)
        categoryRepository.delete(category)
        return ResponseEntity.noContent().build()
    }

    private fun getCategory(id: UUID): Category {
        return categoryRepository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")
    }
}
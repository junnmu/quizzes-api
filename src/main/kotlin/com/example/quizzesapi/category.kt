package com.example.quizzesapi

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.*

data class Category(val id: UUID = UUID.randomUUID(), val name: String)
data class CategoryReq(
    @field:[NotNull NotBlank Size(min = 3, max = 20)]
    val name: String
)

val categories = mutableListOf<Category>()


@RestController
@RequestMapping("categories")
class CategoryController {

    @GetMapping
    fun index() = ResponseEntity.ok(categories)

    @PostMapping
    fun create(@Valid @RequestBody categoryReq: CategoryReq): ResponseEntity<Category> {
        if (categories.any { it.name == categoryReq.name })
            throw ResponseStatusException(HttpStatus.CONFLICT, "Category already exists")

        val category = Category(name = categoryReq.name)
        categories.add(category)
        return ResponseEntity(category, HttpStatus.CREATED)
    }

    @GetMapping("{id}")
    fun show(@PathVariable id: UUID): ResponseEntity<Category> {
        val category = getCategory(id)
        return ResponseEntity.ok(category)
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: UUID, @RequestBody categoryReq: CategoryReq): ResponseEntity<Category> {
        val category = getCategory(id)
        val updatedCategory = category.copy(name = categoryReq.name)
        categories.remove(category)
        categories.add(updatedCategory)
        return ResponseEntity.ok(updatedCategory)
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Category> {
        val category = getCategory(id)
        categories.remove(category)
        return ResponseEntity.noContent().build()
    }

    private fun getCategory(id: UUID): Category {
        return categories.firstOrNull { it.id == id }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")
    }
}
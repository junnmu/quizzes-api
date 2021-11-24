package com.example.quizzesapi

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.validation.Valid
import javax.validation.constraints.*

@Entity
data class Question(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne
    val category: Category,
    val text: String,
    val answer: String,
    val score: Int
)

interface QuestionRepository : JpaRepository<Question, UUID> {
    fun existsByText(text: String): Boolean
    fun existsByTextAndIdIsNot(text: String, id: UUID): Boolean
    fun findByCategoryName(name: String): List<Question>
}

data class QuestionReq(
    @field:[NotNull]
    val categoryId: UUID,
    @field:[NotNull NotBlank Size(min = 5, max = 100)]
    val text: String,
    @field:[NotNull NotBlank Size(min = 1, max = 100)]
    val answer: String,
    @field:[Min(value = 1) Max(value = 100)]
    val score: Int
)

@RestController
@RequestMapping("questions")
class QuestionController(
    val questionRepository: QuestionRepository,
    val categoryRepository: CategoryRepository
) {

    @GetMapping
    fun index(@RequestParam(name = "category", required = false) categoryName: String?) =
        if(categoryName == null) ResponseEntity.ok(questionRepository.findAll())
        else ResponseEntity.ok(questionRepository.findByCategoryName(categoryName))

    @PostMapping
    fun create(@Valid @RequestBody questionReq: QuestionReq): ResponseEntity<Question> {
        val category = categoryRepository.findByIdOrNull(questionReq.categoryId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")

        if (questionRepository.existsByText(questionReq.text))
            throw ResponseStatusException(HttpStatus.CONFLICT, "Question already exists")

        val question = Question(
            category = category,
            text = questionReq.text,
            answer = questionReq.answer,
            score = questionReq.score
        )

        questionRepository.save(question)
        return ResponseEntity(question, HttpStatus.CREATED)
    }

    @GetMapping("{id}")
    fun show(@PathVariable id: UUID) = ResponseEntity.ok(getQuestion(id))

    @PutMapping("{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody questionReq: QuestionReq): ResponseEntity<Question> {
        val question = getQuestion(id)
        val category = categoryRepository.findByIdOrNull(questionReq.categoryId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")

        if (questionRepository.existsByTextAndIdIsNot(questionReq.text, id))
            throw ResponseStatusException(HttpStatus.CONFLICT, "Question already exists")

        val updatedQuestion = question.copy(
            category = category,
            text = questionReq.text,
            answer = questionReq.answer,
            score = questionReq.score
        )

        questionRepository.save(updatedQuestion)
        return ResponseEntity.ok(updatedQuestion)
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Question> {
        val question = getQuestion(id)
        questionRepository.delete(question)
        return ResponseEntity.noContent().build()
    }

    private fun getQuestion(id: UUID): Question {
        return questionRepository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found")
    }
}
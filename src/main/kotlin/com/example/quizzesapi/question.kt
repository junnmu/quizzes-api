package com.example.quizzesapi

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.*

data class Question(
    val id: UUID = UUID.randomUUID(),
    val category: Category,
    val text: String,
    val answer: String,
    val score: Int
)
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

val questions = mutableListOf<Question>()

@RestController
@RequestMapping("questions")
class QuestionController {

    @GetMapping
    fun index() = ResponseEntity.ok(questions)

    @PostMapping
    fun create(@Valid @RequestBody questionReq: QuestionReq): ResponseEntity<Question> {
        val category = categories.firstOrNull { it.id == questionReq.categoryId }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")

        if (questions.any { it.text == questionReq.text })
            throw ResponseStatusException(HttpStatus.CONFLICT, "Question already exists")

        val question = Question(
            category = category,
            text = questionReq.text,
            answer = questionReq.answer,
            score = questionReq.score
        )

        questions.add(question)
        return ResponseEntity(question, HttpStatus.CREATED)
    }
}
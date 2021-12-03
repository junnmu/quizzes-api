package com.example.quizzesapi

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
data class Quiz(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToMany
    val questions: List<Question>,
    @OneToOne
    val user: User
)

class QuizReq(
    @field:[NotNull Size(min = 2)]
    val questionsIds: List<UUID>,
    @field:[NotNull]
    val userId: UUID,
)

interface QuizRepository : JpaRepository<Quiz, UUID> {
    fun findAllByUserId(userId: UUID): List<Quiz>
}

@RestController
class QuizController (
    val questionRepository: QuestionRepository,
    val userRepository: UserRepository,
    val quizRepository: QuizRepository
) {
    @PostMapping("quizzes")
    fun create(@Valid @RequestBody quizReq: QuizReq): ResponseEntity<Quiz> {
        val questions = questionRepository.findAllById(quizReq.questionsIds)
        if (questions.size == 0)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no course was found")
        val user = getUser(quizReq.userId)

        val quiz = Quiz(questions = questions, user = user)
        return ResponseEntity(quizRepository.save(quiz), HttpStatus.CREATED)
    }

    @GetMapping("quizzes/{id}")
    fun show(@PathVariable id: UUID) = ResponseEntity.ok(quizRepository.findByIdOrNull(id)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"))

    @GetMapping("users/{userId}/quizzes")
    fun indexByUserId(@PathVariable userId: UUID): ResponseEntity<List<Quiz>> {
        getUser(userId)
        return ResponseEntity.ok(quizRepository.findAllByUserId(userId))
    }

    private fun getUser(id: UUID): User {
        return userRepository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found with id: $id")
    }
}
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
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name = "users")
data class User(
    @Id
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val email: String,
    val active: Boolean = true,
    @OneToMany
    val quizzes: List<Quiz> = emptyList()
)

interface UserRepository : JpaRepository<User, UUID> {
    fun findByActive(active: Boolean): List<User>
    fun existsByEmail(email: String): Boolean
    fun existsByEmailAndIdIsNot(email: String, id: UUID): Boolean
}

data class UserReq(
    @field:[NotNull NotBlank]
    val name: String,
    @field:[NotNull NotBlank]
    val email: String,
    val active: Boolean
)

@RestController
@RequestMapping("users")
class UserController(val userRepository: UserRepository) {

    @GetMapping
    fun index(@RequestParam(name = "active", required = false) active: Boolean?) =
        if (active == null) ResponseEntity.ok(userRepository.findAll())
        else ResponseEntity.ok(userRepository.findByActive(active))

    @PostMapping
    fun create(@Valid @RequestBody userReq: UserReq): ResponseEntity<User> {
        if (userRepository.existsByEmail(userReq.email))
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists with this email")

        val user = User(name = userReq.name, email = userReq.email)
        return ResponseEntity(userRepository.save(user), HttpStatus.CREATED)
    }

    @GetMapping("{id}")
    fun show(@PathVariable id: UUID) = ResponseEntity.ok(getUser(id))

    @PutMapping("{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody userReq: UserReq): ResponseEntity<User> {
        if (userRepository.existsByEmailAndIdIsNot(userReq.email, id))
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists with this email")

        val user = getUser(id)
        val userUpdated = user.copy(name = userReq.name, email = userReq.email, active = userReq.active)
        return ResponseEntity.ok(userRepository.save(userUpdated))
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<User> {
        val user = getUser(id)
        userRepository.delete(user)
        return ResponseEntity.noContent().build()
    }

    private fun getUser(id: UUID): User {
        return userRepository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
    }
}

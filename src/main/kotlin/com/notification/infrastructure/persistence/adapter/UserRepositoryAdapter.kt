package com.notification.infrastructure.persistence.adapter

import com.notification.domain.model.User
import com.notification.domain.repository.UserRepository
import com.notification.infrastructure.persistence.mapper.EntityMapper
import com.notification.infrastructure.persistence.repository.JpaUserRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryAdapter(
    private val jpaRepository: JpaUserRepository
) : UserRepository {
    override fun findById(id: UUID): User? =
        jpaRepository.findById(id).orElse(null)?.let { EntityMapper.toDomain(it) }

    override fun findByEmail(email: String): User? =
        jpaRepository.findByEmail(email)?.let { EntityMapper.toDomain(it) }

    override fun save(user: User): User =
        EntityMapper.toDomain(jpaRepository.save(EntityMapper.toEntity(user)))
}

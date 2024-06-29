package manager.redis.consumer

import manager.manager.model.enums.ComplianceSnippet
import manager.manager.service.ManagerServiceSpec
import manager.redis.events.LintResult
import manager.redis.events.LintResultStatus
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class LintConsumer
    @Autowired
    constructor(
        redis: RedisTemplate<String, String>,
        @Value("\${manager.redis.result_lint_key}") streamKey: String,
        @Value("\${manager.redis.groups.lint}") groupId: String,
        private val managerService: ManagerServiceSpec,
    ) : RedisStreamConsumer<LintResult>(streamKey, groupId, redis) {
        init {
            subscription()
        }

        override fun onMessage(record: ObjectRecord<String, LintResult>) {
            println("Received record: with user: ${record.value.userId} and snippetId: ${record.value.snippetId}")

            val payload = record.value
            val newStatus = statusParser(payload.result)

            managerService.updateSnippetStatus(payload.userId, payload.snippetId, newStatus)

            println("Processed record: with user: ${record.value.userId} and snippetId: ${record.value.snippetId}")
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, LintResult>> {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(java.time.Duration.ofMillis(10000))
                .targetType(LintResult::class.java)
                .build()
        }

        private fun statusParser(status: LintResultStatus): ComplianceSnippet {
            return when (status) {
                LintResultStatus.SUCCESS -> ComplianceSnippet.COMPLIANT
                LintResultStatus.FAILURE -> ComplianceSnippet.NOT_COMPLIANT
                LintResultStatus.PENDING -> ComplianceSnippet.PENDING
            }
        }
    }

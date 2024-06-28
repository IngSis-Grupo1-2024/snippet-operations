package manager.redis.events

data class LintResult(
    val userId: String,
    val snippetId: String,
    val ruleConfig: String,
    val result: LintResultStatus,
)

enum class LintResultStatus {
    SUCCESS,
    FAILURE,
    PENDING,
}

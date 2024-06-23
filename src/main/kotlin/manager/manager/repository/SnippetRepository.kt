package manager.manager.repository
import manager.manager.model.entity.Snippet
import manager.manager.model.entity.UserSnippet
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SnippetRepository : JpaRepository<Snippet, Long>

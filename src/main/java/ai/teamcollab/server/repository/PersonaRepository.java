package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Persona;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonaRepository extends CrudRepository<Persona, Long> {
    List<Persona> findByCompanyIdOrCompanyIdIsNull(Long companyId);
}
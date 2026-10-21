package easy.springboot.jpa.repository;

import easy.springboot.jpa.domain.Person;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends BaseRepository<Person, Long> {
}

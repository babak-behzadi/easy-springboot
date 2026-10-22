package easy.springboot.web.test.repository;

import easy.springboot.jpa.repository.BaseRepository;
import easy.springboot.web.test.domain.Person;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends BaseRepository<Person, Long> {
}

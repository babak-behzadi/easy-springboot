package easy.springboot.jpa.test.service;

import easy.springboot.jpa.exception.EntityNotFoundException;
import easy.springboot.jpa.service.BaseService;
import easy.springboot.jpa.test.domain.Person;
import easy.springboot.jpa.test.model.PersonModel;
import easy.springboot.jpa.test.repository.PersonRepository;
import org.springframework.stereotype.Service;

@Service
public class PersonService extends BaseService<Person, Long, PersonRepository> {

    protected PersonService(PersonRepository repository) {
        super(repository);
    }

    public Person save(PersonModel model) {
        Person person;
        if (model.id() != null) {
            person = findById(model.id()).orElseThrow(() -> new EntityNotFoundException(model.id()));
        } else {
            person = new Person();
        }
        person.setFirstName(model.firstName());
        person.setLastName(model.lastName());
        person.setBirthDate(model.birthDate());
        return save(person);
    }
}

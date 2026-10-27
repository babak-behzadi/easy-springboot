package easy.springboot.web.test.service;

import easy.springboot.jpa.exception.EntityNotFoundException;
import easy.springboot.jpa.service.BaseService;
import easy.springboot.web.test.domain.Person;
import easy.springboot.web.test.model.PersonModel;
import easy.springboot.web.test.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PersonService extends BaseService<Person, Long, PersonRepository> {

    protected PersonService(PersonRepository repository) {
        super(repository);
    }

    public Person find(Long id) {
        return super.findById(id).orElseThrow(() -> new EntityNotFoundException(id));
    }

    public Person save(PersonModel model) {
        Person person;
        if (model.id() != null) {
            person = find(model.id());
        } else {
            person = new Person();
        }
        person.setFirstName(model.firstName());
        person.setLastName(model.lastName());
        person.setBirthDate(model.birthDate());
        return super.save(person);
    }

    public Person savePhoto(MultipartFile photo, Long id) {
        Person person = find(id);
        //saving photo...
        return super.save(person);
    }
}

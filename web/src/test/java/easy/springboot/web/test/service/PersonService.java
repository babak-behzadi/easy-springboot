/*
 * Copyright 2026 Babak Behzadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

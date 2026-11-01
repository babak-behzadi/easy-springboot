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

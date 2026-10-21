package easy.springboot.jpa.domain;

import easy.springboot.jpa.query.QueryResultField;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table
public class Person extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @QueryResultField
    private Long id;
    @QueryResultField
    private String firstName;
    @QueryResultField
    private String lastName;
    @QueryResultField
    private Date birthDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}

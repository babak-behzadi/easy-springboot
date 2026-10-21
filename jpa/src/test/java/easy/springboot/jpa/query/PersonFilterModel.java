package easy.springboot.jpa.query;

import java.util.Date;
import java.util.List;

public class PersonFilterModel extends FilterModel {

    @FilterField(value = "firstName", operand = FilterOperand.LIKE_CI)
    private String firstName;
    @FilterField(value = "lastName", operand = FilterOperand.LIKE_CI)
    private String lastName;
    @FilterField(value = "birthDate", operand = FilterOperand.BETWEEN)
    private List<Date> birthDate;

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

    public List<Date> getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(List<Date> birthDate) {
        this.birthDate = birthDate;
    }
}

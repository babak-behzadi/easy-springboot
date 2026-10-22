package easy.springboot.web;

import easy.springboot.web.test.model.PersonModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.Calendar;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestConfig.class)
@AutoConfigureMockMvc
public class PersonControllerTest {

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;

    @Test
    void person_save() throws Exception {
        PersonModel model = new PersonModel(null, "Babak", "Behzadi", Calendar.getInstance().getTime());

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/person/save")
                                .content(mapper.writeValueAsString(model))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value(model.firstName()))
                .andExpect(jsonPath("$.lastName").value(model.lastName()))
                .andDo(print());
    }

    @Test
    void person_find() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/person/find")
                                .param("id", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Babak"))
                .andDo(print());
    }

}

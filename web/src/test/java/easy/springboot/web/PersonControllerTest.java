package easy.springboot.web;

import easy.springboot.web.test.model.PersonModel;
import easy.springboot.web.test.query.PersonFilterModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.Calendar;
import java.util.List;

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

    @Test
    void search_person() throws Exception {
        PersonFilterModel filterModel = new PersonFilterModel();
        filterModel.setFirstName("bab");
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DATE, -1);
//        filterModel.setBirthDate(List.of(from.getTime(), Calendar.getInstance().getTime()));
        filterModel.setPage(0);
        filterModel.setPageSize(1);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/person/query")
                                .content(mapper.writeValueAsString(filterModel))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(filterModel.getPage()))
                .andExpect(jsonPath("$.pageSize").value(filterModel.getPageSize()))
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.rows").isNotEmpty())
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].firstName").value("Babak"))
                .andDo(print());
    }

}

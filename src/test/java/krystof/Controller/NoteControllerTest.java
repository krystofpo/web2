package krystof.Controller;

import krystof.business.Label;
import krystof.business.Note;
import krystof.business.NoteHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteHandler handler;


    @Before
    public void setUp() throws Exception {
when(handler.findNote(anyLong())).thenReturn(new Note("testMock", new ArrayList<Label>(Arrays.asList(new Label("necolabelMock")))));

    }

    @Test
    public void showNote() throws Exception {

        mockMvc.perform(get("/note/123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("label")))
                .andExpect(content().string(containsString("note")))
                .andExpect(content().string(containsString("testMock")));
verify(handler, times(1)).findNote(eq(123L));

    }



}
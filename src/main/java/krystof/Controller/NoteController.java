package krystof.Controller;

import krystof.business.Note;
import krystof.business.NoteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller

public class NoteController {


    private NoteHandler handler;

    @Autowired
    public NoteController(NoteHandler handler) {
        this.handler = handler;
    }

    @RequestMapping(value = "/note/{Id}", method = RequestMethod.GET)
    public @ResponseBody
    Note showNote(@PathVariable("Id") long id) {
        return handler.findOne(id);
    }


    @RequestMapping(value = "/savenote", method = RequestMethod.GET)
    public String showSaveNoteForm() {
        return "savenote";
    }

    @RequestMapping(value = "/savenote", method = RequestMethod.POST)
    public String submitNote(@ModelAttribute(name = "noteentity") Note note, Model model) {

        System.out.println("------------------------\n\n\n\n\n\n\n\n");
        System.out.println(note);

//        System.out.println(model);
        System.out.println("ulozim");
        note = handler.save(note);
        System.out.println(note);
        model.addAttribute("noteentity", note);
//        System.out.println(model);

//
//        System.out.println(multiMap);
//        multiMap.get("labels.label").forEach(val -> System.out.println(val.toString()));


        System.out.println("------------------------\n\n\n\n\n\n\n\n");

        return "noteresult";
    }

    @RequestMapping(value = "/findnotebynote", method = RequestMethod.GET)
    public String showFindNoteByNoteForm()
    {
    return "findnotebynote";
    }

    @RequestMapping(value = "/findnotebynote", method = RequestMethod.POST)
    public String showFindNoteByNoteResult(
            @RequestParam("note") String note, Model model) {
        System.out.println("note search" + note);
        Note noteReal = handler.findNoteByNote(note);
        System.out.println("found" + noteReal);
        if (noteReal != null) {
            model.addAttribute("noteentity", noteReal);
            return "noteresult";
        }
        return "noteresult";
    }
}

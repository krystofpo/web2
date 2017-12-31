package krystof.business;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import krystof.Data.LabelRepository;
import krystof.Data.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by polansky on 12.12.2017.
 */
@Service
public class NoteHandler { //todo refactor change name to reposservice

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private LabelRepository labelRepository;

    @PersistenceContext
    private EntityManager entityManager;


    public NoteHandler() {
    }

    protected NoteRepository getNoteRepository() {
        return noteRepository;
    }

    protected LabelRepository getLabelRepository() {
        return labelRepository;
    }

    public void setLabelRepository(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    public Note findOne(long id) {
        return noteRepository.findOne(id);
    }

    //saves a new Note or finds existing one.
    //method accepts a Note with saved or unsaved Labels
    public Note save(Note note) {
        if (note == null || isBlank(note.getNote())) {
            return null;
        }

        if (sameNoteIsSavedAlready(note)) {
            throw new NoteHandlerException(
                    "Error: Attempt to save a note which " +
                            "already exists. Use update instead.");
        } else {
            return saveNewNote(note);
        }
    }

    private Note saveNewNote(Note note) {
        List<Label> labels = note.getLabels();
        if (!isEmpty(labels)) {
            setSavedLabelsToNote(note);
        }
        return noteRepository.save(note);
    }

    private boolean sameNoteIsSavedAlready(Note note) {
        return !isEmpty(noteRepository.findByNote(note.getNote()));
    }


    private void setSavedLabelsToNote(Note note) {
        List<Label> savedLabels = createSetOfSavedLabels(note.getLabels());
        note.setLabels(savedLabels);
            }

    private List<Label> createSetOfSavedLabels(List<Label> unsavedLabels) {
        List<Label> savedLabels = new ArrayList<>();
        unsavedLabels.stream().
                forEach(l -> saveLabelAndAddToSet(l, savedLabels));
        return savedLabels;
    }

    private void saveLabelAndAddToSet(Label label, List<Label> labels) {
        Label savedLabel = findOrSaveLabel(label);
        labels.add(savedLabel);
    }

    private Label findOrSaveLabel(Label label) {
        List<Label> savedLabels = labelRepository.findByLabel(label.getLabel());
        if (isEmpty(savedLabels)) {
            return labelRepository.save(label);
        } else {
            return savedLabels.get(0);
        }
    }

    public void deleteAllNotes() {
        noteRepository.deleteAll();
    }

    public void deleteAllLabels() {
        labelRepository.deleteAll();
    }

    public List<Note> findAllNotes() {
        List<Note> notes = noteRepository.findAll();
        if (notes == null) {
            return new ArrayList<>();
        } else {
            return notes;
        }
    }

    public List<Label> findAllLabels() {
        List<Label> labels = labelRepository.findAll();
        if (labels == null) {
            return new ArrayList<>();
        } else {
            return labels;
        }
    }

    public List<Note> findNoteByOneLabel(Label label) {
        return findNoteByManyLabels(Arrays.asList(label));
    }

    public Label save(Label label) {
        if (label == null || isBlank(label.getLabel())) {
            return null;
        }
        return findOrSaveLabel(label);
    }

    //todo has to be tested
    // todo correct returns, change some to null, clean code, readiility, logic
    public List<Note> findNoteByManyLabels(List<Label> labels) {
        if (isEmpty(labels)) {
            return new ArrayList<>();
        }

        //cannot search for a note containing a non existing label
        ListWithFlag checkedList = checkIfContainsNonExistingLabel(labels);
        if (checkedList.containsNonExistingLabel()) {
            return new ArrayList<>();
        }

        return findNoteBySavedLabels(checkedList);
    }

    private List<Note> findNoteBySavedLabels(ListWithFlag checkedList) {


        List<Label> existingLabels = checkedList.getExistingLabels();


        BooleanBuilder predicate = buildPredicate(existingLabels);

        return findNoteByPredicate(predicate);
    }

    private BooleanBuilder buildPredicate(List<Label> existingLabels) {
        krystof.business.QNote note = krystof.business.QNote.note1;

        BooleanBuilder builder = new BooleanBuilder();

        for (Label savedLabel : existingLabels) {
            builder.and(note.labels.contains(savedLabel));

        }
        return builder;
    }

    private List<Note> findNoteByPredicate(BooleanBuilder predicate) {
        krystof.business.QNote note = krystof.business.QNote.note1;

        JPAQuery query = new JPAQuery(entityManager);

        List<Note> notes = query.from(note)
                .where(predicate)
                .list(note);
        if (notes == null) {
            return new ArrayList<>();
        }
        return notes;
    }


    ListWithFlag checkIfContainsNonExistingLabel(List<Label> uncheckedLabels) {
        if (isEmpty(uncheckedLabels)) {
            return null;
        }

        ListWithFlag listWithFlag = new ListWithFlag();

        for (Label uncheckedLabel : uncheckedLabels) {


            LabelWithFlag checkedLabel = checkIfLabelExists(uncheckedLabel);
            if (checkedLabel.isNonExisting()) {
                listWithFlag.setExistingLabels(new ArrayList<>());
                listWithFlag.setContainsNonExistingLabel(true);
                return listWithFlag;
            }


            listWithFlag.getExistingLabels().add(checkedLabel.getExistingLabel());

        }


        return listWithFlag;
    }

    LabelWithFlag checkIfLabelExists(Label uncheckedLabel) {

        LabelWithFlag checkedLabel = new LabelWithFlag();


        if (uncheckedLabel == null) {
            checkedLabel.setExistingLabel(null);
            checkedLabel.setNonExisting(true);
            return checkedLabel;
        }

        Label existingLabel = findLabelByLabel(uncheckedLabel.getLabel());

        if (existingLabel == null) {
            checkedLabel.setNonExisting(true);
        }
        checkedLabel.setExistingLabel(existingLabel);
        return checkedLabel;


    }

    public Label findLabelByLabel(String label) {
        if (isBlank(label)) {
            return null;
        }

        List<Label> labels = labelRepository.findByLabel(label);
        if (isEmpty(labels)) {
            return null;
        }
        return labels.get(0);
    }

    public Note findNoteByNote(String note) {
        if (isBlank(note)) {
            return null;
        }
        List<Note> notes = noteRepository.findByNote(note);
        if (isEmpty(notes)) {
            return null;
        }
        return notes.get(0);
    }

    public Note updateNote(Note note) {

        if (note == null) {
            throw new NoteHandlerException("Error: cannot update null note");
        }

        if (note.getNoteId() == null) {
            throw new NoteHandlerException("Error: cannot update unsaved note");
        }

        if (!noteRepository.exists(note.getNoteId())) {
            throw new NoteHandlerException("Error: note with ID:" + note.getNoteId() + "does not exist.");
        }
        return noteRepository.save(note);
    }

    public Label updateLabel(Label label) {
        if (label == null) {
            throw new NoteHandlerException("Error: cannot update null label");
        }

        if (label.getLabelId() == null) {
            throw new NoteHandlerException("Error: cannot update unsaved label");
        }

        if (!labelRepository.exists(label.getLabelId())) {
            throw new NoteHandlerException("Error: label with ID:" + label.getLabelId() + "does not exist.");
        }
        return labelRepository.save(label);
    }

    public void deleteLabel(Label label) {
        throwIfLabelIsInvalid(label);

        List<Note> notesContainingLabel = findNoteByOneLabel(label);

        if (isEmpty(notesContainingLabel)) {
            deleteFreeLabel(label);
        } else {

            notesContainingLabel.forEach(note -> removeLabelAndUpdate(note, label));
            deleteFreeLabel(label);
        }

    }

    private void removeLabelAndUpdate(Note note, Label label) {
        note.getLabels().remove(label);
        updateNote(note);
    }

    private void deleteFreeLabel(Label label) {
        labelRepository.delete(label.getLabelId());
    }

    private void throwIfLabelIsInvalid(Label label) {
        if (label == null) {
            throw new NoteHandlerException("Error: cannot delete null label");
        }

        if (label.getLabelId() == null) {
            throw new NoteHandlerException("Error: cannot delete unsaved label");
        }

        if (!labelRepository.exists(label.getLabelId())) {
            throw new NoteHandlerException("Error: label with ID:" + label.getLabelId() + "does not exist.");
        }
    }

    public void deleteNote(Note note) {
        throwIfNoteIsInvalid(note);

        noteRepository.delete(note.getNoteId());
    }

    private void throwIfNoteIsInvalid(Note note) {
        if (note == null) {
            throw new NoteHandlerException("Error: cannot delete null note");
        }

        if (note.getNoteId() == null) {
            throw new NoteHandlerException("Error: cannot delete unsaved note");
        }

        if (!noteRepository.exists(note.getNoteId())) {
            throw new NoteHandlerException("Error: note with ID:" + note.getNoteId() + "does not exist.");
        }
    }
}



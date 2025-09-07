package com.krypt.backend.service;

import com.krypt.backend.dto.NoteDTO.NoteDTO;
import com.krypt.backend.model.Note;
import com.krypt.backend.model.User;
import com.krypt.backend.repository.NoteRepository;
import com.krypt.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public Note createNote(User user, NoteDTO noteDTO) {
        Note note = new Note();
        note.setUser(user);
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        return noteRepository.save(note);
    }

    public List<Note> getUserNotes(User user) {
        List<Note> notes = noteRepository.findByUser(user);

        if (notes.isEmpty()) {
            Note defaultNote = new Note();
            defaultNote.setUser(user);
            defaultNote.setTitle("Untitled 1");
            defaultNote.setContent("");
            noteRepository.save(defaultNote);
            notes.add(defaultNote);
        }

        return notes;
    }

    public Note getNoteById(Long noteID, User user) {
        Note note = noteRepository.findById(noteID).orElseThrow();
        if (!note.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return note;
    }

    public Note updateNote(Long noteID, User user, NoteDTO noteDTO) {
        Note note = getNoteById(noteID, user);
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        return noteRepository.save(note);
    }

    public void deleteNote(Long noteID, User user) {
        Note note = getNoteById(noteID, user);
        noteRepository.delete(note);
    }
}

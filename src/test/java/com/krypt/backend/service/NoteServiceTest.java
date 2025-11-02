package com.krypt.backend.service;

import com.krypt.backend.dto.NoteDTO.NoteDTO;
import com.krypt.backend.model.Note;
import com.krypt.backend.model.User;
import com.krypt.backend.repository.NoteRepository;
import com.krypt.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    // ------------------- CREATE -------------------
    @Test
    void createNoteShouldCreateNoteWithUserTitleAndContent() {
        NoteDTO dto = new NoteDTO();
        dto.setTitle("Test title");
        dto.setContent("Test content");

        Note savedNote = new Note();
        savedNote.setUser(user);
        savedNote.setTitle(dto.getTitle());
        savedNote.setContent(dto.getContent());

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        Note result = noteService.createNote(user, dto);

        verify(noteRepository).save(any(Note.class));
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getTitle()).isEqualTo("Test title");
        assertThat(result.getContent()).isEqualTo("Test content");
    }

    // ------------------- READ -------------------
    @Test
    void getUserNotesShouldReturnExistingNotes() {
        List<Note> notes = new ArrayList<>();
        Note note = new Note();
        note.setUser(user);
        note.setTitle("Existing note");
        note.setContent("Some content");
        notes.add(note);

        when(noteRepository.findByUserOrderByTitleAsc(user)).thenReturn(notes);

        List<Note> result = noteService.getUserNotes(user);

        verify(noteRepository, never()).save(any(Note.class));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Existing note");
    }

    @Test
    void getUserNotesShouldReturnNewDefaultNoteIfNotExists() {
        when(noteRepository.findByUserOrderByTitleAsc(user)).thenReturn(new ArrayList<>());

        Note defaultNote = new Note();
        defaultNote.setUser(user);
        defaultNote.setTitle("Untitled 1");
        defaultNote.setContent("");

        when(noteRepository.save(any(Note.class))).thenReturn(defaultNote);

        List<Note> result = noteService.getUserNotes(user);

        verify(noteRepository).save(any(Note.class));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Untitled 1");
        assertThat(result.get(0).getContent()).isEmpty();
    }

    // ------------------- UPDATE -------------------
    @Test
    void updateNoteShouldUpdateNote() {
        Note note = new Note();
        note.setId(1L);
        note.setUser(user);
        note.setTitle("Old title");
        note.setContent("Old content");

        NoteDTO dto = new NoteDTO();
        dto.setTitle("New title");
        dto.setContent("New content");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note updatedNote = noteService.updateNote(1L, user, dto);

        assertThat(updatedNote.getTitle()).isEqualTo("New title");
        assertThat(updatedNote.getContent()).isEqualTo("New content");
        verify(noteRepository).save(note);
    }

    @Test
    void updateNoteShouldThrowIfNoteNotFound() {
        when(noteRepository.findById(1L)).thenReturn(Optional.empty());

        NoteDTO dto = new NoteDTO();
        dto.setTitle("New title");
        dto.setContent("New content");

        assertThrows(RuntimeException.class, () -> noteService.updateNote(1L, user, dto));
    }

    // ------------------- DELETE -------------------
    @Test
    void deleteNoteShouldDeleteNote() {
        User user = new User();
        user.setId(1L);

        Note note = new Note();
        note.setId(1L);
        note.setUser(user);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        Note deletedNote = noteService.deleteNote(1L, user);

        verify(noteRepository).delete(note);
        assertThat(deletedNote).isEqualTo(note);
    }

    @Test
    void deleteNoteShouldThrowIfNoteNotFound() {
        lenient().when(noteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> noteService.deleteNote(1L, user));
    }
}

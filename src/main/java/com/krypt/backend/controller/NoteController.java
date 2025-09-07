package com.krypt.backend.controller;

import com.krypt.backend.dto.NoteDTO.NoteDTO;
import com.krypt.backend.dto.NoteDTO.NoteResponseDTO;
import com.krypt.backend.model.Note;
import com.krypt.backend.model.User;
import com.krypt.backend.repository.UserRepository;
import com.krypt.backend.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteService noteService;
    private final UserRepository userRepository;

    public NoteController(final NoteService noteService, final UserRepository userRepository) {
        this.noteService = noteService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<NoteResponseDTO> createNote(Principal principal, @RequestBody NoteDTO noteDTO) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email));
        Note note = noteService.createNote(user, noteDTO);
        return ResponseEntity.ok(toResponseDTO(note));
    }

    @GetMapping
    public ResponseEntity<List<NoteResponseDTO>> getUserNotes(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email));

        List<NoteResponseDTO> notes = noteService.getUserNotes(user)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponseDTO> getNote(Principal principal, @PathVariable Long id) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email));
        return ResponseEntity.ok(toResponseDTO(noteService.getNoteById(id, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponseDTO> updateNote(Principal principal, @PathVariable Long id, @RequestBody NoteDTO noteDTO) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email));
        return ResponseEntity.ok(toResponseDTO(noteService.updateNote(id, user, noteDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(Principal principal, @PathVariable Long id) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email));
        noteService.deleteNote(id, user);
        return ResponseEntity.noContent().build();
    }

    private NoteResponseDTO toResponseDTO(Note note) {
        NoteResponseDTO noteResponseDTO = new NoteResponseDTO();
        noteResponseDTO.setId(note.getId());
        noteResponseDTO.setTitle(note.getTitle());
        noteResponseDTO.setContent(note.getContent());
        noteResponseDTO.setCreationDate(note.getCreationDate());
        noteResponseDTO.setModificationDate(note.getModificationDate());
        return noteResponseDTO;
    }
}

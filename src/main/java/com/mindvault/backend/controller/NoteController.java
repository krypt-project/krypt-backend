package com.mindvault.backend.controller;

import com.mindvault.backend.dto.NoteDTO.NoteDTO;
import com.mindvault.backend.dto.NoteDTO.NoteResponseDTO;
import com.mindvault.backend.model.Note;
import com.mindvault.backend.model.User;
import com.mindvault.backend.repository.UserRepository;
import com.mindvault.backend.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<NoteResponseDTO> createNote(@AuthenticationPrincipal String email, @RequestBody NoteDTO noteDTO) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Note note = noteService.createNote(user, noteDTO);
        return ResponseEntity.ok(toResponseDTO(note));
    }

    @GetMapping
    public ResponseEntity<List<NoteResponseDTO>> getUserNotes(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        String email = principal.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<NoteResponseDTO> notes = noteService.getUserNotes(user)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(notes);
    }


    @GetMapping("/{id}")
    public ResponseEntity<NoteResponseDTO> getNote(@AuthenticationPrincipal String email, @PathVariable Long id) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(toResponseDTO(noteService.getNoteById(id, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponseDTO> updateNote(@AuthenticationPrincipal String email, @PathVariable Long id, @RequestBody NoteDTO noteDTO) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(toResponseDTO(noteService.updateNote(id, user, noteDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@AuthenticationPrincipal String email, @PathVariable Long id) {
        User user = userRepository.findByEmail(email).orElseThrow();
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

package com.mindvault.backend.dto.NoteDTO;

import lombok.Data;

@Data
public class NoteDTO {
    private String title;
    private String content;

    // Getter & Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

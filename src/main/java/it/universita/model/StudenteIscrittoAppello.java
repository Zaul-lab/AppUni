package it.universita.model;

import java.time.LocalDateTime;

public class StudenteIscrittoAppello {
    private final long prenotazioneId;
    private final Studente studente;
    private final LocalDateTime prenotatoIl;

    public StudenteIscrittoAppello(long prenotazioneId,
                                   Studente studente,
                                   LocalDateTime prenotatoIl) {
        this.prenotazioneId = prenotazioneId;
        this.studente = studente;
        this.prenotatoIl = prenotatoIl;
    }

    public long getPrenotazioneId() {
        return prenotazioneId;
    }

    public Studente getStudente() {
        return studente;
    }

    public LocalDateTime getPrenotatoIl() {
        return prenotatoIl;
    }

    @Override
    public String toString() {
        return "StudenteIscrittoAppello{" +
                "prenotazioneId=" + prenotazioneId +
                ", studente=" + studente +
                ", prenotatoIl=" + prenotatoIl +
                '}';
    }
}

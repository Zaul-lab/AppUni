package it.universita.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Appello {
    private long id, materiaId;
    private LocalDateTime dataAppello;
    private String aula;
    private String stato;
    private String materia;
    private String professore;

    public Appello(long id, long materiaId, LocalDateTime dataAppello, String aula, String stato, String nomeMateria,String professore) {
        this.id = id;
        this.materiaId = materiaId;
        this.dataAppello = dataAppello;
        this.aula = aula;
        this.stato = stato;
        this.materia = nomeMateria;
        this.professore = professore;
    }

    public long getId() {
        return id;
    }

    public long getMateriaId() {
        return materiaId;
    }

    public LocalDateTime getDataAppello() {
        return dataAppello;
    }

    public String getAula() {
        return aula;
    }

    public String getStato() {
        return stato;
    }

    @Override
    public String toString() {
        return "Appello{" +
                "id=" + id +
                ", materiaId=" + materiaId +
                ", data= " + dataAppello +
                ", aula= " + Objects.toString(aula, "—") +
                ", stato= " + stato + ", nomeMateria= " + materia + ", nomeProfessore= " + professore +
                '}';
    }
}


package it.universita.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Appello {
    private long id, materiaId, docenteId;
    private int postiMax;
    private LocalDateTime dataAppello;
    private String aula;
    private String stato;

    public Appello(long id, long materiaId, long docenteId, LocalDateTime dataAppello, String aula,int postiMax, String stato) {
        this.id = id;
        this.materiaId = materiaId;
        this.docenteId = docenteId;
        this.dataAppello = dataAppello;
        this.postiMax = postiMax;
        this.aula = aula;
        this.stato = stato;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(long materiaId) {
        this.materiaId = materiaId;
    }

    public long getDocenteId() {
        return docenteId;
    }

    public void setDocenteId(long docenteId) {
        this.docenteId = docenteId;
    }

    public LocalDateTime getDataAppello() {
        return dataAppello;
    }

    public void setDataAppello(LocalDateTime dataAppello) {
        this.dataAppello = dataAppello;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public int getPostiMax() {
        return postiMax;
    }

    public void setPostiMax(int postiMax) {
        this.postiMax = postiMax;
    }

    @Override
    public String toString() {
        return "Appello{" +
                "id=" + id +
                ", materiaId=" + materiaId +
                ", docenteId=" + docenteId +
                ", data=" + dataAppello +
                ", aula=" + Objects.toString(aula, "—") +
                ", postiMax=" + postiMax +
                ", stato=" + stato +
                '}';
    }
}


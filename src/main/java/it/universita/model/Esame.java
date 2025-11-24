package it.universita.model;

import java.time.LocalDateTime;

public class Esame {
    private long idEsame, idPrenotazione, idProfessore;
    private String docente, materia, esito;
    private Integer voto;
    private boolean lode;
    private LocalDateTime dataRegistrazione;


    public Esame() {
    }

    public Esame(long idEsame, long idPrenotazione, long idProfessore, String docente, String materia, String esito, Integer voto, boolean lode, LocalDateTime dataRegistrazione) {
        this.idEsame = idEsame;
        this.idPrenotazione = idPrenotazione;
        this.idProfessore = idProfessore;
        this.docente = docente;
        this.materia = materia;
        this.esito = esito;
        this.voto = voto;
        this.lode = lode;
        this.dataRegistrazione = dataRegistrazione;
    }

    public long getIdEsame() {
        return idEsame;
    }

    public long getIdPrenotazione() {
        return idPrenotazione;
    }

    public String getDocente() {
        return docente;
    }

    public long getIdProfessore() {
        return idProfessore;
    }

    public String getMateria() {
        return materia;
    }

    public String getEsito() {
        return esito;
    }
    public Integer getVoto() {
        return voto;
    }

    public boolean isLode() {
        return lode;
    }

    public LocalDateTime getDataRegistrazione() {
        return dataRegistrazione;
    }

    @Override
    public String toString() {
        return "Libretto{" +
                "idEsame=" + idEsame +
                ", idPrenotazione=" + idPrenotazione +
                ", idProfessore=" + idProfessore +
                ", docente='" + docente + '\'' +
                ", materia='" + materia + '\'' +
                ", esito='" + esito + '\'' +
                ", voto=" + voto +
                ", dataRegistrazione=" + dataRegistrazione +
                '}';
    }
}

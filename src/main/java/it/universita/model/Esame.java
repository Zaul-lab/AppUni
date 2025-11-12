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
    public void setIdEsame(long idEsame) {
        this.idEsame = idEsame;
    }
    public long getIdPrenotazione() {
        return idPrenotazione;
    }
    public void setIdPrenotazione(long idPrenotazione) {
        this.idPrenotazione = idPrenotazione;
    }
    public String getDocente() {
        return docente;
    }
    public void setDocente(String docente) {
        this.docente = docente;
    }
    public long getIdProfessore() {
        return idProfessore;
    }

    public void setIdProfessore(long idProfessore) {
        this.idProfessore = idProfessore;
    }
    public String getMateria() {
        return materia;
    }
    public void setMateria(String materia) {
        this.materia = materia;
    }
    public String getEsito() {
        return esito;
    }
    public void setEsito(String esito) {
        this.esito = esito;
    }
    public Integer getVoto() {
        return voto;
    }
    public void setVoto(Integer voto) {
        this.voto = voto;
    }
    public boolean isLode() {
        return lode;
    }
    public void setLode(boolean lode) {
        this.lode = lode;
    }
    public LocalDateTime getDataRegistrazione() {
        return dataRegistrazione;
    }
    public void setDataRegistrazione(LocalDateTime dataRegistrazione) {
        this.dataRegistrazione = dataRegistrazione;
    }

    @Override
    public String toString() {
     /*   String votoStr;
        if (voto == null) votoStr = "-";
        else votoStr = (lode && voto == 30) ? voto + "L" : String.valueOf(voto); */

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

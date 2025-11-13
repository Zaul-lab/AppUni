package it.universita.model;

public class Corso {
    private long id;
    private String nome;
    private String codice;
    private int cfu;
    private long idProfessore;

public Corso() {}
public Corso (long id, String nome, String codice, int cfu, long idProfessore) {
    this.id = id;
    this.nome = nome;
    this.codice = codice;
    this.cfu = cfu;
    this.idProfessore = idProfessore;
}

public long getId() {
    return id;
}

public void setId(long id) {
        this.id = id;
}

public String getNome() {
    return nome;
}

public void setNome(String nome) {
    this.nome = nome;
}

public String getCodice() {
    return codice;
}

public void setCodice(String codice) {
    this.codice = codice;
}

public int getCfu() {
    return cfu;
}

public void setCfu(int cfu) {
    this.cfu = cfu;
}

public long getIdProfessore() {
    return idProfessore;
}

public void setIdProfessore(long idProfessore) {
    this.idProfessore = idProfessore;
}

@Override
public String toString() {
    return "Corso{" +
            "id=" + id +
            ", codice='" + codice + '\'' +
            ", nome='" + nome + '\'' +
            ", cfu=" + cfu +
            ", idProfessore=" + idProfessore +
            '}';

    }
}

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

public String getNome() {
    return nome;
}

public String getCodice() {
    return codice;
}

public int getCfu() {
    return cfu;
}

public long getIdProfessore() {
    return idProfessore;
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

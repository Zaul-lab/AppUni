package it.universita.model;

public class Materia {
    private long id,id_corso,id_professore;
    private String nome;
    private int cfu;
    private int anno;
    private String semestre;

public Materia(){}

    public Materia (long id, long id_corso, String nome, int cfu, int anno,String semestre, long id_professore) {
        this.id = id;
        this.id_corso = id_corso;
        this.nome = nome;
        this.cfu = cfu;
        this.anno = anno;
        this.semestre = semestre;
        this.id_professore = id_professore;
    }

    public long getId() {
        return id;
    }

    public long getId_corso() {
     return id_corso;
    }

    public String getNome() {
        return nome;
    }

    public int getCfu() {
     return cfu;
    }

    public int getAnno() {
     return anno;
    }

    public String getSemestre() {
        return semestre;
    }

    @Override
    public String toString() {
        return "Materia{" +
                "id=" + id +
                ", id_corso=" + id_corso +
                ", nome='" + nome + '\'' +
                ", cfu=" + cfu +
                ", anno=" + anno +
                ", semestre=" + semestre +
                '}';

    }
}


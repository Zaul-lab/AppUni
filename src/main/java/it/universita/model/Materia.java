package it.universita.model;

public class Materia {
    private long id;
    private long id_corso;
    private String nome;
    private int cfu;
    private int anno;
    private int semestre;

public Materia(){}

    public Materia (long id, long id_corso, String nome, int cfu, int anno) {
        this.id = id;
        this.id_corso = id_corso;
        this.nome = nome;
        this.cfu = cfu;
        this.anno = anno;
        this.semestre = 0;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getId_corso() {
     return id_corso;
    }

    public void setId_corso(long id_corso) {
        this.id_corso = id_corso;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
     this.nome = nome;
    }

    public int getCfu() {
     return cfu;
    }

    public void setCfu(int cfu) {
        this.cfu = cfu;
    }

    public int getAnno() {
     return anno;
    }
    public void setAnno(int anno) {
        this.anno = anno;
    }
    public int getSemestre() {
        return semestre;
    }

    public void setSemestre(int semestre) {
        this.semestre = semestre;
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


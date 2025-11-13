package it.universita.model;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Libretto {
    private ArrayList<Esame> esami = new ArrayList<>();

    public Libretto() {}

    public void aggiungiEsameAlLibretto(Esame esame) {
        esami.add(esame);
    }

    public ArrayList<Esame> getEsami() {
        return esami;
    }
}

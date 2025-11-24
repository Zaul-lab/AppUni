package it.universita.model;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Libretto {
    private ArrayList<Esame> esami;

    public Libretto() {
        this.esami =  new ArrayList<>();
    }

    public void aggiungiEsameAlLibretto(Esame esame) {
        esami.add(esame);
    }

    public ArrayList<Esame> getEsami() {
        return esami;
    }

    @Override
    public String toString() {
        return "Libretto{" +
                "esami=" + esami +
                '}';
    }
}

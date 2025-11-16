package it.universita.client;

import it.universita.model.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private ClientTCP clientTCP;
    private final long idUtente;
    private String scelta;

    public Menu(ClientTCP clientTCP, long idUtente) {
        this.clientTCP = clientTCP;
        this.idUtente = idUtente;
    }


    public void menuStudente() {

        Scanner tastiera = new Scanner(System.in);

        while (true) {
            System.out.println("""
                    Inserisci scelta:
                    1: Mostra la lista degli appelli aperti (e puoi iscriverti a uno di essi)
                    2: Mostra la lista degli appelli a cui sono iscritto (e puoi cancellarti a uno di essi)
                    3: Mostra libretto
                    altro: Esci
                    """);
            switch (tastiera.nextLine().trim()) {
                case "1" -> {
                    try {
                        List<Appello> appelli = this.clientTCP.listaAppelliAperti();
                        for (Appello a : appelli) {
                            System.out.println(a);
                        }
                        System.out.println("Vuoi prenotarti a qualche appello?");
                        this.scelta = tastiera.nextLine().trim();
                        if (scelta.equalsIgnoreCase("Si")) {
                            System.out.println("digita l'id dell'appello:");
                            long idAppello = tastiera.nextLong();
                            try {
                                if (this.clientTCP.prenotazioneAppello(idAppello, this.idUtente))
                                    System.out.println("Prenotazione effettuata!");

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                case "2" -> {
                    try {
                        List<Appello> appelli = this.clientTCP.listAppelliPrenotatiDaStudente(this.idUtente);
                        for (Appello a : appelli) {
                            System.out.println(a);
                        }
                        System.out.println("Vuoi cancellarti da qualche appello?");
                        this.scelta = tastiera.nextLine();
                        if (scelta.equalsIgnoreCase("si")) {
                            System.out.println("digita l'id dell'appello:");
                            long idAppello = tastiera.nextLong();
                            try {
                                if (this.clientTCP.cancellazionePrenotazioneAppello(idAppello, this.idUtente))
                                    System.out.println("Cancellazione effettuata!");

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case "3" -> {
                    try {
                        Libretto l = this.clientTCP.mostraLibretto(this.idUtente);
                        for (Esame e : l.getEsami()) {
                            System.out.println(e);
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                default ->{
                    try{
                        clientTCP.close();
                        System.exit(0);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public void menuProfessore() {


        Scanner tastiera = new Scanner(System.in);

        while (true) {
            System.out.println("""
                    Inserisci scelta:
                    1: Mostra la lista dei tuoi appelli (e chiudi eventuali appelli)
                    2: Mostra materie insegnate(e crea eventuale appello)
                    3: Mostra la lista degli utenti iscritti ad un appello (e verbalizza un esame)
                    
                    altro: Esci
                    """);

            switch (tastiera.nextLine().trim()) {
                case "1" -> {
                    try {
                        for (Appello a : clientTCP.listaAppelliPerDocenti(idUtente)) {
                            System.out.println(a);
                        }
                        System.out.println("Vuoi chiudere uno degli appelli?");
                        if(tastiera.nextLine().trim().equalsIgnoreCase("si")){
                            System.out.println("Digita l'id dell'appello da chiudere");
                            long idAppello = tastiera.nextLong();
                            tastiera.nextLine();
                            if(clientTCP.chiudiAppello(idAppello)) System.out.println("appello chiuso correttamente!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "2" -> {
                    try {
                        for (Materia m : clientTCP.mostraMaterieInsegnate()) {
                            System.out.println(m);
                        }
                        System.out.println("Vuoi creare un appello per una di queste materie?");
                        if (tastiera.nextLine().trim().equalsIgnoreCase("si")) {
                            System.out.println("Digita l'id della materia di cui vuoi creare l'appello");
                            long idMateria = tastiera.nextLong();
                            tastiera.nextLine();
                            System.out.println("Digita il nome dell'aula");
                            String nomeAula = tastiera.nextLine().trim();
                            System.out.println("Da che giorno parte l'appello? inserisci la data dell'appello in formato yyyy/mm/dd");
                            String d = tastiera.nextLine().trim();
                            LocalDate ld = Persona.parseData(d);
                            LocalDateTime data = ld.atStartOfDay();
                            clientTCP.creaAppello(idMateria, this.idUtente, data, nomeAula);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }case "3" ->{
                    try{
                        System.out.println("Gli appelli disponibili per controllare gli studenti iscritti sono: ");
                        for (Appello a : clientTCP.listaAppelliPerDocenti(idUtente)) {
                            System.out.println(a);
                        }
                        System.out.println("Qual è l'id dell'appello di cui vuoi visualizzare gli iscritti: ");
                        long idAppello = tastiera.nextLong();
                        tastiera.nextLine();
                        List<StudenteIscrittoAppello> studenti =  clientTCP.listIscrittiAppello(idAppello);
                        for (StudenteIscrittoAppello studente : studenti) {
                            System.out.println(studente);
                        }
                        System.out.println("Vuoi verbalizzare il voto a qualche studente? se si digita solo l'idStudente, oppure digita 0 per uscire:");
                        long idStudente = tastiera.nextLong();
                        tastiera.nextLine();
                        System.out.println("Scegli il voto da inserire, digita 31 per la lode");
                        int voto = tastiera.nextInt();
                        boolean lode = false;
                        if(voto == 31){
                            lode = true;
                        }
                        tastiera.nextLine();
                        if(idStudente != 0){
                            Studente studenteDaVerbalizzare;
                            for (StudenteIscrittoAppello studente : studenti){
                                Studente s = studente.getStudente();
                                if(idStudente == s.getId()){
                                    studenteDaVerbalizzare = new Studente(s.getNome(),s.getCognome(),s.getDataDiNascita(),s.getMatricola(),s.getId());
                                    clientTCP.inserisciVoto(studente.getPrenotazioneId(),this.idUtente,idStudente,voto,lode);
                                }
                            }

                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }default -> System.exit(0);
            }
        }


    }

}
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
    private final Scanner tastiera;

    public Menu(ClientTCP clientTCP, long idUtente, Scanner tastiera) {
        this.clientTCP = clientTCP;
        this.idUtente = idUtente;
        this.tastiera = tastiera;
    }


    public void menuStudente() {

        Scanner tastiera = new Scanner(System.in);

        while (true) {
            System.out.println("""
                    Inserisci scelta:
                    1: Mostra la lista degli appelli aperti (e puoi iscriverti a uno di essi)
                    2: Mostra la lista degli appelli a cui sono iscritto (e puoi cancellarti a uno di essi)
                    3: Mostra libretto
                    4: logout (torna al menù iniziale)
                    altro: Esci
                    """);
            switch (tastiera.nextLine().trim()) {
                case "1" -> {
                    try {
                        List<Appello> appelli = this.clientTCP.listaAppelliAperti();
                        if (appelli.isEmpty()) {
                            System.out.println("non hai appelli disponibili");
                        } else {
                            for (Appello a : appelli) {
                                System.out.println(a);
                            }
                            System.out.println("Vuoi prenotarti a qualche appello?");
                            this.scelta = tastiera.nextLine().trim();
                            if (scelta.equalsIgnoreCase("Si")) {
                                System.out.println("digita l'id dell'appello:");
                                long idAppello = tastiera.nextLong();
                                tastiera.nextLine();
                                try {
                                    if (this.clientTCP.prenotazioneAppello(idAppello, this.idUtente))
                                        System.out.println("Prenotazione effettuata!");

                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                case "2" -> {
                    try {
                        List<Appello> appelli = this.clientTCP.listAppelliPrenotatiDaStudente(this.idUtente);

                        if (appelli.isEmpty()) {
                            System.out.println("Non risulti iscritto a nessun appello");
                        } else {
                            for (Appello a : appelli) {
                                System.out.println(a);
                            }
                            System.out.println("Vuoi cancellarti da qualche appello?");
                            this.scelta = tastiera.nextLine();
                            if (scelta.equalsIgnoreCase("si")) {
                                System.out.println("digita l'id dell'appello:");
                                long idAppello = tastiera.nextLong();
                                tastiera.nextLine();
                                try {
                                    if (this.clientTCP.cancellazionePrenotazioneAppello(idAppello, this.idUtente))
                                        System.out.println("Cancellazione effettuata!");
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case "3" -> {
                    try {
                        Libretto l = this.clientTCP.mostraLibretto(this.idUtente);
                        if (l == null) {
                            System.out.println("Non hai ancora sostenuto esami");
                        } else {
                            for (Esame e : l.getEsami()) {
                                System.out.println(e);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                case "4" -> {
                    return;
                }
                default -> System.out.println("Riprova a digitare meglio");
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
                        List<Appello> appelliDocenti = clientTCP.listaAppelliPerDocenti(idUtente);
                        if (appelliDocenti.isEmpty()) {
                            System.out.println("Non hai ancora aperto appelli");
                        } else {
                            for (Appello a : appelliDocenti) {
                                System.out.println(a);
                            }

                            System.out.println("Vuoi chiudere uno degli appelli?");
                            if (tastiera.nextLine().trim().equalsIgnoreCase("si")) {
                                System.out.println("Digita l'id dell'appello da chiudere");
                                long idAppello = tastiera.nextLong();
                                tastiera.nextLine();
                                if (clientTCP.chiudiAppello(idAppello))
                                    System.out.println("appello chiuso correttamente!");
                            }
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case "2" -> {
                    try {
                        List<Materia> materie = clientTCP.mostraMaterieInsegnate();
                        if (materie.isEmpty()) {
                            System.out.println("Non insegni materie al momento");
                        } else {

                            for (Materia m : materie) {
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
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case "3" -> {
                    boolean presente = false;
                    boolean studentePresente = false;
                    try {
                        System.out.println("Gli appelli disponibili per controllare gli studenti iscritti sono: ");
                        List<Appello> appelliDocenti = clientTCP.listaAppelliPerDocenti(idUtente);
                        if (appelliDocenti.isEmpty()) {
                            System.out.println("Non hai appelli aperti");
                        } else {
                            for (Appello a : appelliDocenti) {
                                System.out.println(a);
                            }
                            System.out.println("Qual è l'id dell'appello di cui vuoi visualizzare gli iscritti: ");
                            long idAppello = tastiera.nextLong();
                            tastiera.nextLine();
                            for (Appello a : clientTCP.listaAppelliPerDocenti(idUtente)) {
                                if (idAppello == a.getId()) {
                                    presente = true;
                                }
                            }
                            if (presente) {
                                List<StudenteIscrittoAppello> studenti = clientTCP.listIscrittiAppello(idAppello);
                                if (studenti.isEmpty()) {
                                    System.out.println("Non ci sono studenti iscritti all'appello");
                                } else {
                                    for (StudenteIscrittoAppello studente : studenti) {
                                        System.out.println(studente);
                                    }
                                    System.out.println("Vuoi verbalizzare il voto a qualche studente? se si digita solo l'idStudente, oppure digita 0 per uscire:");
                                    long idStudente = tastiera.nextLong();
                                    tastiera.nextLine();
                                    for (StudenteIscrittoAppello studente : studenti) {
                                        Studente s = studente.getStudente();
                                        if (idStudente == s.getId()) {
                                            studentePresente = true;
                                        }
                                    }
                                    if (studentePresente) {
                                        System.out.println("Scegli il voto da inserire, digita 31 per la lode");
                                        int voto = tastiera.nextInt();
                                        tastiera.nextLine();
                                        boolean lode = false;
                                        if (idStudente != 0) {
                                            Studente studenteDaVerbalizzare;
                                            for (StudenteIscrittoAppello studente : studenti) {
                                                Studente s = studente.getStudente();
                                                if (idStudente == s.getId()) {
                                                    studenteDaVerbalizzare = new Studente(s.getNome(), s.getCognome(), s.getDataDiNascita(), s.getMatricola(), s.getId());
                                                    if (clientTCP.inserisciVoto(studente.getPrenotazioneId(), this.idUtente, idStudente, voto, lode))
                                                        System.out.println("Voto inserito correttamente!");
                                                }
                                            }
                                        }
                                    } else System.out.println("Studente non presente negli iscritti");
                                }
                            } else System.out.println("Non esiste l'appello scelto");
                        }

                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
                default -> System.exit(0);
            }
        }


    }

}
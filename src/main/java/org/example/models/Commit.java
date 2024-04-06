package org.example.models;
import org.eclipse.jgit.revwalk.RevCommit; //utile per elavorare le metriche

/*La classe RevCommit in Java è parte della libreria JGit.
La classe RevCommit contiene informazioni cruciali su un commit,
come l'autore, il messaggio del commit, il timestamp e i genitori del commit.
Questi dati sono essenziali per comprendere la storia di un repository Git
e per gestire le modifiche nel codice sorgente.*/

public final class Commit {
    private final RevCommit revCommit;
    private Ticket ticket;
    private final Release release;

    public Commit(RevCommit revCommit, Release release) {
        this.revCommit = revCommit;
        this.release = release;
        ticket = null;
    }

    public RevCommit getRevCommit() {
        return revCommit;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Release getRelease() {
        return release;
    }

}
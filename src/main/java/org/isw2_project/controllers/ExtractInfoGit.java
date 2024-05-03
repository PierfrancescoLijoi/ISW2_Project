package org.isw2_project.controllers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.isw2_project.models.Commit;
import org.isw2_project.models.ProjectClass;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

public class ExtractInfoGit {
    private List<Ticket> ticketList;
    private final  List<Release> releaseList;
    protected static  Git git;
    private static  Repository repository ;
    public ExtractInfoGit(String projName, String repoURL, List<Release> releaseList) throws IOException, GitAPIException {
        String filename = projName.toLowerCase() + "Temp";
        File directory = new File(filename);
        if(directory.exists()){
            repository = new FileRepository(filename + "\\.git");
            git = new Git(repository);
        }else{
            git = Git.cloneRepository().setURI(repoURL).setDirectory(directory).call();
            repository = git.getRepository();
        }
        this.releaseList = releaseList;
        this.ticketList = null;

    }
    public List<Commit> extractAllCommits() throws IOException, GitAPIException {
        List<Commit> commitList = new ArrayList<>();
        int i = 0;
        // Lista che conterrà tutti i commit unici recuperati da tutti i rami
        List<RevCommit> revCommitList = new ArrayList<>();

        // Recupera l'elenco di tutti i rami nel repository Git
        List<Ref> branchList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

        // Itera su tutti i rami
        for (Ref branch : branchList) {

            // Ottiene l'oggetto LogCommand per recuperare la storia dei commit
            LogCommand logCommand = git.log();

            // Aggiunge il riferimento del ramo corrente alla lista dei commit da esaminare
            ObjectId branchObjectId = repository.resolve(branch.getName());
            logCommand.add(branchObjectId);

            // Esegue il comando per ottenere un iterabile di tutti i commit nel ramo corrente
            Iterable<RevCommit> allRevCommits = logCommand.call();


            // Itera su tutti i commit nel ramo corrente
            for (RevCommit revCommit : allRevCommits) {
                // Verifica se il commit corrente è già presente nella lista dei commit
                if (!revCommitList.contains(revCommit)) {
                    // Se il commit corrente non è già presente, lo aggiunge alla lista dei commit unici
                    revCommitList.add(revCommit);
                }
            }
        }

        revCommitList.sort(Comparator.comparing(o -> o.getCommitterIdent().getWhen())); //ordina le informazioni dei commit,tramite revcommit, in base alle date

        for (RevCommit revCommit : revCommitList) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            LocalDate commitDate = LocalDate.parse(formatter.format(revCommit.getCommitterIdent().getWhen()));
            LocalDate lowerBoundDate = LocalDate.parse(formatter.format(new Date(0))); //1 gennaio 1970 00:00:00 UTC----> fomrattata: "1970-01-01"

            for(Release release: releaseList){
                LocalDate dateOfRelease = release.getReleaseDate();
                if (commitDate.isAfter(lowerBoundDate) && !commitDate.isAfter(dateOfRelease)) {
                    Commit newCommit = new Commit(revCommit, release);
                    commitList.add(newCommit);
                    release.addCommit(newCommit);
                }
                lowerBoundDate = dateOfRelease;
                //riassegno lowerbound, così da poter inserire solo commit, se validi,
                // verificati dopo di quello appena assegnato
            }
        }
        releaseList.removeIf(release -> release.getCommitList().isEmpty());
        //Release nella lista releaseList, viene verificato se
        // la lista dei commit associati è vuota o meno.
        //Se la lista dei commit associati è vuota per una determinata release,
        // allora quella release viene rimossa dalla lista releaseList

        for (Release release : releaseList) {
            release.setReleaseId(++i);
        }
        commitList.sort(Comparator.comparing(o -> o.getRevCommit().getCommitterIdent().getWhen()));
        //Confrontare gli oggetti Commit in base alle date dei commit dei committenti.
        // Gli Commit con date di commit più antiche verranno posizionati prima nella lista,
        // mentre quelli con date di commit più recenti verranno posizionati dopo.

        return commitList;

    }

    public List<Ticket> getTicketList() {
        return ticketList;
    }

    public void setTicketList(List<Ticket> ticketList) {
        this.ticketList = ticketList;
    }
    public List<Release> getReleaseList() {
        return releaseList;
    }
    public List<Commit> filterFixedCommits(List<Commit> commitList) {
        List<Commit> filteredCommits = new ArrayList<>();
        for (Commit commit : commitList) {
            for (Ticket ticket : ticketList) {
                String commitFullMessage = commit.getRevCommit().getFullMessage();
                String ticketKey = ticket.getTicketKey();
                if (CommitMatchWithTicketID(commitFullMessage, ticketKey)) {
                    filteredCommits.add(commit);
                    ticket.addCommit(commit);
                    commit.setTicket(ticket);
                }
            }
        }
        ticketList.removeIf(ticket -> ticket.getCommitList().isEmpty());
        return filteredCommits;
    }

    // Vede la corrispondenza del commit con ogni ticket. E se è presento lo inserisce.
    public static boolean CommitMatchWithTicketID(String stringToMatch, String commitKey) {
        Pattern pattern = Pattern.compile(commitKey + "\\b");
        return pattern.matcher(stringToMatch).find();
    }

    public List<ProjectClass> extractAllProjectClasses(List<Commit> commitList, int releasesNumber) throws IOException {
        List<Commit> lastCommitList = new ArrayList<>();

        //estrai tutti i commit da ogni release
        for(int i = 1; i <= releasesNumber; i++){
            List<Commit> tempCommits = new ArrayList<>(commitList);
            int finalI = i;
            tempCommits.removeIf(commit -> (commit.getRelease().getReleaseId() != finalI)); //Rimozione dei commit non corrispondenti alla release corrente

            if(tempCommits.isEmpty()){//Se dopo la rimozione dei commit non rimane nessun commit nella lista tempCommits, si salta all'iterazione successiva del ciclo con continue.
                continue;
            }
            lastCommitList.add(tempCommits.get(tempCommits.size()-1));// Se invece non è vuota tempCommits,
            // viene aggiunto l'ultimo commit della lista (che sarà l'ultimo commit della release corrente) alla lista lastCommitList.
        }
        lastCommitList.sort(Comparator.comparing(o -> o.getRevCommit().getCommitterIdent().getWhen())); //Ordinamento di lastCommitList per data

        List<ProjectClass> ListAllProjectClasses = new ArrayList<>();
        for(Commit lastCommit: lastCommitList){ //itero sulla lista di commit associata a ogni release

            Map<String, String> nameAndContentOfClasses = getAllClassesNameAndContent(lastCommit.getRevCommit());

            for(Map.Entry<String,String> nameAndContentOfClass : nameAndContentOfClasses.entrySet()){
                ListAllProjectClasses.add(new ProjectClass(nameAndContentOfClass.getKey(), nameAndContentOfClass.getValue(), lastCommit.getRelease()));
            }
        }
        ClassesBuggyOrNot(ticketList, ListAllProjectClasses); //definisce quali classi erano buggy se era toccata dal commit del ticket fixed

        KnowWhichClassTouchedByCommit(ListAllProjectClasses, commitList); //tiene traccia dei commit che toccano la classe

        ListAllProjectClasses.sort(Comparator.comparing(ProjectClass::getName));

        return ListAllProjectClasses;
    }
    private Map<String, String> getAllClassesNameAndContent(RevCommit revCommit) throws IOException {
        Map<String, String> allClasses = new HashMap<>();
        RevTree tree = revCommit.getTree(); // per ottenere l'albero dei file associato al commit fornito.

        TreeWalk treeWalk = new TreeWalk(repository);// per camminare sull'albero dei file.
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true); //imposta in modalità ricorsiva per considerare tutti i file e le cartelle
        while(treeWalk.next()) { //ricorsivamente visito tutto l'albero dei commit

            if(treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test/")) { //Se il file è un file ".java" e non si trova nella cartella "/test/"

                allClasses.put(treeWalk.getPathString(), new String(repository.open(treeWalk.getObjectId(0)).getBytes(), StandardCharsets.UTF_8));
                // il percorso del file come chiave e il suo contenuto come valore.
            }
        }
        treeWalk.close();
        return allClasses;
    }

    public void ClassesBuggyOrNot(List<Ticket> ticketList, List<ProjectClass> allProjectClasses) throws IOException {
        // obiettivo:L'obiettivo principale del metodo è etichettare le classi come "buggy" (difettose) o meno in base ai commit associati ai ticket e alle date dei ticket.

        for(ProjectClass projectClass: allProjectClasses){ //prima setta tutte le classi a Buggy=false come detto da Falessi
            projectClass.getMetric().setBuggyness(false);
        }
        for(Ticket ticket: ticketList) { //itera su tutti i Ticket

            List<Commit> commitsContainingTicket = ticket.getCommitList(); // il ticket ha una lista di Commit associati a esso, puo capitare che non ho corrispondenza 1-1
            Release injectedVersion = ticket.getInjectedVersion(); //ottengo inject version relativa al ticket corrente

            for (Commit commit : commitsContainingTicket) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                RevCommit revCommit = commit.getRevCommit();
                // Ottiene la data e l'ora del commit come un oggetto Date
                Date commitDateTime = revCommit.getCommitterIdent().getWhen();

                // Formatta la data e l'ora del commit nel formato desiderato "yyyy-MM-dd"
                String formattedDateTime = formatter.format(commitDateTime);

                // Converte la data formattata in un oggetto LocalDate
                LocalDate commitDate = LocalDate.parse(formattedDateTime);

                //controllo consistenza commit associato al ticket
                if (!commitDate.isAfter(ticket.getResolutionDate()) // se la data del commit NON è dopo quella della risoluzione del ticket
                        && !commitDate.isBefore(ticket.getCreationDate())) { //la data NOn è prima della creazione del ticket

                    List<String> modifiedClassesNames = RetriveTouchedClassesNamesByCommit(revCommit);
                    Release releaseOfCommit = commit.getRelease(); //commit associato alla release

                    for (String modifiedClass : modifiedClassesNames) {
                        labelBuggyClasses(modifiedClass, injectedVersion, releaseOfCommit, allProjectClasses);
                    }
                }
            }

        }
    }

    private void KnowWhichClassTouchedByCommit(List<ProjectClass> allProjectClasses, List<Commit> commitList) throws IOException {
        //obiettivo: è mantenere traccia dei commit che toccano ciascuna classe del progetto.

        for (Commit commit: commitList){ //scorri tutta la list dei commit di tutte le release

            Release release = commit.getRelease(); //ogni commit è associato a una release

            List<ProjectClass> InitialProjClassesList=new ArrayList<>(allProjectClasses); //istanzio nella lista temporanea tutte le classi che riceve in Input
            InitialProjClassesList.removeIf(InitialProjClassList -> !InitialProjClassList.getRelease().equals(release)); //rimuove dalla lista InitialProjClassList tutte le classi che non appartengono alla stessa release del commit attualmente considerato nell'iterazione
            List<String> ClassesNamesTouchedByCurrentCommit = RetriveTouchedClassesNamesByCommit(commit.getRevCommit());//inserisce nella lista i nomi delle classi toccate dal commit corrente

            for(String classTouchedByCommit: ClassesNamesTouchedByCurrentCommit){
                for(ProjectClass projectClass: InitialProjClassesList){
                    if(projectClass.getName().equals(classTouchedByCommit) && !projectClass.getCommitsThatTouchTheClass().contains(commit)) {
                        projectClass.addCommitThatTouchesTheClass(commit);
                    }
                }
            }

        }

    }
    private List<String> RetriveTouchedClassesNamesByCommit(RevCommit commit) throws IOException {
        List<String> touchedClassesNamesByCommit = new ArrayList<>();

        try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            ObjectReader reader = this.repository.newObjectReader()) {

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser(); //albero associato al commit corrente
            ObjectId newTree = commit.getTree(); //albero associato al commit corrente
            newTreeIter.reset(reader, newTree); // utilizza l'id dell'albero per poterlo leggere ed esaminare i file

            RevCommit commitParent = commit.getParent(0);	// commit precedente al commit corrente, quindi quello che lo ha generato
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser(); //albero associato al commit precedente
            ObjectId oldTree = commitParent.getTree(); //Ottine l'ID dell'albero associato al commit genitore (==commit precedente a quello corrente)

            oldTreeIter.reset(reader, oldTree);//Utilizza l'ID dell'albero appena ottenuto e l'oggetto ObjectReader per impostare il parser (oldTreeIter)
            // In modo che possa analizzare l'albero associato al commit genitore.
            // Il parser sarà pronto a esaminare i file e le loro differenze presenti nell'albero del commit genitore quando sarà richiesto

            diffFormatter.setRepository(this.repository);
            //sarà configurato per operare all'interno del repository specificato,
            // perchè utilizzerà questo repository per ottenere informazioni sui file e sulle differenze tra i commit.

            // Viene eseguita la scansione delle differenze tra l'albero "vecchio" e l'albero "nuovo"
            // utilizzando diffFormatter.scan(oldTreeIter, newTreeIter).
            // Questo restituisce una lista di oggetti DiffEntry,
            // ognuno dei quali rappresenta una differenza tra due file.
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

            // Ogni voce contiene informazioni per ogni file coinvolto nel commit
            // (vecchio nome del percorso, nuovo nome del percorso,
            // tipo di modifica (che potrebbe essere MODIFY, ADD, RENAME, ecc.))
            for(DiffEntry entry : entries) {
                //Conserviamo solo le classi Java che non sono coinvolte nei test
                if(entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY) && entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/")) {
                    touchedClassesNamesByCommit.add(entry.getNewPath());
                }

            }

        } catch(ArrayIndexOutOfBoundsException e) {
            //il commit non ha genitori: salta questo commit, restituisce una lista vuota e vai avanti

        }

        return touchedClassesNamesByCommit;
    }

    private static void labelBuggyClasses(String modifiedClass, Release injectedVersion, Release fixedVersion, List<ProjectClass> allProjectClasses) {
        for(ProjectClass projectClass: allProjectClasses){
            if(projectClass.getName().equals(modifiedClass)  //la classe modificata è uguale a quella corrente nelle classi del progetto
                    && projectClass.getRelease().getReleaseId() < fixedVersion.getReleaseId() //classe associata alla release che per avere il bug deve avere id < della F.V
                    && projectClass.getRelease().getReleaseId() >= injectedVersion.getReleaseId()){ // classe associata alla release che per avere il bug deve avere id >= della I.V.
                projectClass.getMetric().setBuggyness(true);
            }
        }
    }

    public void computeAddedAndDeletedLOC(ProjectClass projectClass) throws IOException {
        for(Commit commit : projectClass.getCommitsThatTouchTheClass()) { //itera tutti i commit che hanno toccato la classe

            try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) { //DiffFormatter per formattare le differenze tra due punti nello storico dei commit, e non voglio che generi output
                RevCommit revCommit = commit.getRevCommit();//Ottiene le informazioni (RevCommit) corrispondente al commit corrente
                RevCommit parentComm = revCommit.getParent(0);

                diffFormatter.setRepository(this.repository);
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);

                List<DiffEntry> diffs = diffFormatter.scan(parentComm.getTree(), revCommit.getTree());
                for(DiffEntry entry : diffs) {
                    if(entry.getNewPath().equals(projectClass.getName())) {
                        projectClass.addLOCAddedByClass(getAddedLinesCount(diffFormatter, entry));
                        projectClass.addLOCDeletedByClass(getDeletedLinesCount(diffFormatter, entry));
                    }

                }
            } catch(ArrayIndexOutOfBoundsException ignored) {
                //il commit non ha genitori: salta questo commit, restituisce una lista vuota e vai avanti
            }

        }

    }

    private int getAddedLinesCount(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
        int addedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) { //Itera attraverso ogni modifica (Edit) presente nell'intestazione del file associato alla voce di differenza entry
            addedLines += edit.getEndB() - edit.getBeginB();
            //Per ogni modifica, aggiunge al conteggio delle linee aggiunte la differenza tra
            // la posizione finale (endB()) e quella iniziale (beginB()) della modifica.
            // Questo è possibile perché endB()
            // restituisce il numero di linea dopo la fine dell'intervallo di modifiche,
            // mentre beginB() restituisce il numero di linea all'inizio dell'intervallo di modifiche.
        }
        return addedLines;
    }
    private int getDeletedLinesCount(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
        int deletedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            deletedLines += edit.getEndA() - edit.getBeginA();
            // Per ogni modifica, aggiunge al conteggio delle linee eliminate
            // la differenza tra la posizione finale (endA()) e quella iniziale (beginA()) della modifica.
            // Questo è possibile perché endA() restituisce il numero di linea dopo la fine dell'intervallo di modifiche,
            // mentre beginA() restituisce il numero di linea all'inizio dell'intervallo di modifiche.
        }
        return deletedLines;
    }
}

package org.isw2_project.controllers;

import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.isw2_project.models.Commit;
import org.isw2_project.models.LOCMetric;
import org.isw2_project.models.Metric;
import org.isw2_project.models.ProjectClass;

import java.util.HashSet;
import java.util.Set;


import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ComputeMetrics {
    private final List<ProjectClass> allProjectClasses;
    private final List<Commit> filteredCommitsOfIssues;
    private final ExtractInfoGit gitExtractor;

    public ComputeMetrics(ExtractInfoGit gitExtractor, List<ProjectClass> allProjectClasses, List<Commit> filteredCommitsOfIssues) {
        this.allProjectClasses = allProjectClasses;
        this.filteredCommitsOfIssues = filteredCommitsOfIssues;
        this.gitExtractor = gitExtractor;
    }

    public void computeAllMetrics() throws IOException {
       computeSize(); // Conta la dimensione della classe

       computeNumberOfRevisionEachClass();
       /* Conta quante volte una classe è stata aggiornata o modificata nel tempo.
       Questo include qualsiasi tipo di modifica alla classe, non solo quelle correlate alla risoluzione di difetti,
       ma anche aggiunte di nuove funzionalità, ottimizzazioni del codice, refactoring e così via. */

       computeNumberOfAuthors(); // Conta quanti autori hanno contribuito sulla classe

       computeNumberOfCommitFix();
       /* Conta quante volte una classe è stata modificata specificamente per correggere difetti.
       Questo include solo le modifiche che riguardano la risoluzione di problemi noti
       o la correzione di difetti segnalati*/
        computeLOCMetrics();

        NumberOfCommentLinesInCode();//proposta

        // proposta inutile NumberOfPublicAttributeInCode essendo tutti zero, è stata rimossa poiché non veniva
        // infranta l'incapsulamento nel Object oriented

        NumberOfClassesInvoked();//proposta, quante classi invoca ogni classe-->

        /*-->Contare il numero di classi invocate è importante perché fornisce
         informazioni sulla complessità, l'accoppiamento e la dipendenza del codice.
         Un alto numero di classi invocate indica una maggiore complessità e un
         forte accoppiamento tra la classe in esame e altre parti del sistema.
         Questo può rendere il codice più fragile, meno testabile e potenzialmente
         soggetto a violazioni dei principi di progettazione. */

        numberOfMethod();// proposta, numeri di metodi presenti nella classe, definisce la sua ampiezza

        numberOfImports(); //proposta -->

        //---> Il numero totale degli Import può influenzare la complessità della classe,
        // con classi più complesse che possono avere più bug a causa di una maggiore probabilità di errori.

        //il numero degli Import di java possono influire sulla complessità del codice e sulla sua comprensione
        //introducendo quindi bug.

        // il numero degli import di Api (esterne) possono introdurre un bug da un punto di vista operazionale ma anche di sicurezza
        // ragione per cui vengono considerate (un malfunzionamento dal punto di vista della sicurezza descrive sempre un bug)

        //il numero degli import dei package può introdurre un bug nella complessità e coesione delle classi nel codice
        // introducendo quindi una complessità elevata nel codice e nella sua comprensione.
    }

    private void numberOfImports() {
        for (ProjectClass projectClass : allProjectClasses) {
            int javaImportCount = 0;
            int ImportApiCount = 0;
            int ImportPackageCount = 0;

            String[] lines = projectClass.getContentOfClass().split("\\r?\\n");

            // Scansiona ogni riga per cercare gli import
            for (String line : lines) {
                // Ignora le righe che non iniziano con "import"
                if (line.trim().startsWith("import")) {
                    // Controlla se l'import è relativo a Java
                    if (line.contains("java.")) {
                        javaImportCount++;
                    } else {
                        // Controlla se l'import contiene un punto e non è un import statico
                        if (line.contains(".") && !line.contains("import static")
                                && !(line.contains(gitExtractor.getProjectName().toLowerCase() + ".")|| //minuscolo
                                line.contains(gitExtractor.getProjectName().toUpperCase() + "."))) {
                             ImportApiCount++;
                        }
                        if (line.contains(gitExtractor.getProjectName().toLowerCase() + ".")|| //minuscolo
                                line.contains(gitExtractor.getProjectName().toUpperCase() + ".")) { //maiuscolo
                            ImportPackageCount++;
                        }
                    }
                }
            }



            // Imposta le metriche calcolate per la classe

            projectClass.getMetric().setNumberOfJavaImports(javaImportCount);
            projectClass.getMetric().setNumberOfApiImports(ImportApiCount);
            projectClass.getMetric().setNumberOfImportPackageCount(ImportPackageCount);
        }
    }

    public void NumberOfClassesInvoked() {

        for (ProjectClass projectClass : allProjectClasses) {
            Pattern pattern = Pattern.compile("\\bnew\\s+(\\w+)\\s*\\(");

            // Matcher per trovare i match nel contenuto della classe
            Matcher matcher = pattern.matcher(projectClass.getContentOfClass());

            // Set per memorizzare le classi invocate
            Set<String> invokedClasses = new HashSet<>();

            // Trova e aggiungi le classi invocate al set
            while (matcher.find()) {
                String className = matcher.group(1);
                invokedClasses.add(className);
            }
            projectClass.getMetric().setTotalInvokedClasses(invokedClasses.size());
        }

    }

    public void numberOfMethod() {

        for (ProjectClass projectClass : allProjectClasses) {
            int methodCount = 0;
            Pattern pattern = Pattern.compile("\\b(?:public|private|protected)\\s+\\w+\\s+(\\w+)\\s*\\(");

            // Matcher per trovare i match nel contenuto della classe
            Matcher matcher = pattern.matcher(projectClass.getContentOfClass());

            // Contatore per il numero di metodi

            // Conta il numero di metodi trovati
            while (matcher.find()) {
                methodCount++;
            }

            // Restituisci il numero totale di metodi

            projectClass.getMetric().setNumberOfMethods(methodCount);
        }

    }







    private void computeSize() {
        //scorro tutta la lista delle ProjectCLasses con cui ho istanziato la classe
        // le splitto e definisco la size
        for(ProjectClass projectClass : this.allProjectClasses) {
            String[] linesCode = projectClass.getContentOfClass().split("\r\n|\r|\n");
            projectClass.getMetric().setSize(linesCode.length);

        }

    }

    private void computeNumberOfRevisionEachClass() {
        // Itera su tutte le classi del progetto
        for(ProjectClass projectClass : this.allProjectClasses) {
            // Ottiene l'oggetto metrica associato alla classe
            Metric metric = projectClass.getMetric();

            // Ottiene il numero di commit che coinvolgono la classe
            int numberOfRevisions = projectClass.getCommitsThatTouchTheClass().size();

            // Imposta il numero di revisioni sulla metrica associata alla classe
            metric.setNumberOfRevisions(numberOfRevisions);
        }
    }
    private void computeNumberOfCommitFix() {
        // Variabile per tenere traccia del numero totale di correzioni di difetti per la classe corrente
        int numberOfFix;

        // Itera su tutte le classi del progetto
        for(ProjectClass projectClass : allProjectClasses) {
            numberOfFix=0;
            // Itera su tutti i commit che coinvolgono la classe corrente
            for(Commit commitThatTouchesTheClass : projectClass.getCommitsThatTouchTheClass()) {
                // Verifica se il commit corrente è presente nella lista dei commit filtrati relativi ai problemi noti
                for(Commit commit:filteredCommitsOfIssues){
                    if (commit.getRevCommit().getName().contains(commitThatTouchesTheClass.getRevCommit().getName())) {
                        // Se il commit corrente è presente nella lista, incrementa il contatore delle correzioni di difetti
                        numberOfFix++;
                    }
                }


            }

            // Imposta il numero di correzioni di difetti per la classe corrente sulla metrica associata
            projectClass.getMetric().setNumberOfDefectFixes(numberOfFix);
        }
    }
    private void computeNumberOfAuthors() {
        for(ProjectClass projectClass : allProjectClasses) {
            List<String> authorsOfClass = new ArrayList<>();
            for(Commit commit : projectClass.getCommitsThatTouchTheClass()) {
                RevCommit revCommit = commit.getRevCommit();
                if(!authorsOfClass.contains(revCommit.getAuthorIdent().getName())) {
                    authorsOfClass.add(revCommit.getAuthorIdent().getName());
                }
            }
            projectClass.getMetric().setNumberOfAuthors(authorsOfClass.size());
        }


    }

    private void computeLOCMetrics() throws IOException {
        LOCMetric removedLOC = new LOCMetric();
        LOCMetric churnLOC = new LOCMetric();
        LOCMetric addedLOC = new LOCMetric();
        LOCMetric touchedLOC = new LOCMetric();
        int i;
        for(ProjectClass projectClass : allProjectClasses) {
            addedLOC.setVal(0);
            addedLOC.setAvgVal(0);
            addedLOC.setMaxVal(0);

            removedLOC.setVal(0);
            removedLOC.setAvgVal(0);
            removedLOC.setMaxVal(0);

            churnLOC.setVal(0);
            churnLOC.setAvgVal(0);
            churnLOC.setMaxVal(0);

            touchedLOC.setVal(0);
            touchedLOC.setAvgVal(0);
            touchedLOC.setMaxVal(0);

            gitExtractor.extractAddedOrRemovedLOC(projectClass);

            List<Integer> locAddedByClass = projectClass.getLOCAddedByClass();
            List<Integer> locRemovedByClass = projectClass.getLOCDeletedByClass();

            for(i = 0; i < locAddedByClass.size(); i++) {

                int addedLineOfCode = locAddedByClass.get(i);
                int removedLineOfCode = locRemovedByClass.get(i);
                int churningFactor = Math.abs(locAddedByClass.get(i) - locRemovedByClass.get(i));
                int touchedLinesOfCode = locAddedByClass.get(i) + locRemovedByClass.get(i);

                addedLOC.addToVal(addedLineOfCode);
                removedLOC.addToVal(removedLineOfCode);
                churnLOC.addToVal(churningFactor);
                touchedLOC.addToVal(touchedLinesOfCode);
                if(addedLineOfCode > addedLOC.getMaxVal()) {
                    addedLOC.setMaxVal(addedLineOfCode);
                }
                if(removedLineOfCode > removedLOC.getMaxVal()) {
                    removedLOC.setMaxVal(removedLineOfCode);
                }
                if(churningFactor > churnLOC.getMaxVal()) {
                    churnLOC.setMaxVal(churningFactor);
                }
                if(touchedLinesOfCode > touchedLOC.getMaxVal()){
                    touchedLOC.setMaxVal(touchedLinesOfCode);
                }
            }

            //settare i valori calcolati
            int nRevisions = projectClass.getMetric().getNumberOfRevisions();
            if(!locAddedByClass.isEmpty()) {
                addedLOC.setAvgVal(1.0* addedLOC.getVal()/ nRevisions);
            }
            if(!locRemovedByClass.isEmpty()) {
                removedLOC.setAvgVal(1.0* removedLOC.getVal()/ nRevisions);
            }
            if(!locAddedByClass.isEmpty() || !locRemovedByClass.isEmpty()) {
                churnLOC.setAvgVal(1.0* churnLOC.getVal()/ nRevisions);
                touchedLOC.setAvgVal(1.0* touchedLOC.getVal()/nRevisions);
            }
            projectClass.getMetric().setAddedLOCMetrics(addedLOC.getVal(), addedLOC.getMaxVal(), addedLOC.getAvgVal());
            projectClass.getMetric().setRemovedLOCMetrics(removedLOC.getVal(), removedLOC.getMaxVal(), removedLOC.getAvgVal());
            projectClass.getMetric().setChurnMetrics(churnLOC.getVal(), churnLOC.getMaxVal(), churnLOC.getAvgVal());
            projectClass.getMetric().setTouchedLOCMetrics(touchedLOC.getVal(), touchedLOC.getMaxVal(), touchedLOC.getAvgVal());
        }
    }

    private void NumberOfCommentLinesInCode(){
        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
             ObjectReader reader = this.gitExtractor.getRepository().newObjectReader()) {
            // Imposta il repository e l'object reader per il DiffFormatter
            diffFormatter.setRepository(this.gitExtractor.getRepository());


            // Itera su ciascuna classe
            for (ProjectClass projectClass : allProjectClasses) {
                // Ottieni il contenuto della classe
                String classContent = projectClass.getContentOfClass();

                // Conta le righe di commento nel contenuto della classe
                int commentLinesInClass = countCommentLinesInContent(classContent);

                // Imposta il numero di righe di commento nella metrica della classe
                projectClass.getMetric().setNumberOfCommentLinesInClass(commentLinesInClass);
            }
        } catch (Exception e) {
            //non fare nulla
        }

    }
    private int countCommentLinesInContent(String content) {
        // Espressione regolare per individuare le linee di commento
        Pattern pattern = Pattern.compile("//.*$|/\\*.*?\\*/", Pattern.MULTILINE );
        Matcher matcher = pattern.matcher(content);

        int commentLines = 0;
        while (matcher.find()) {
            commentLines++;
        }
        return commentLines;
    }



}


package org.isw2_project.controllers;

import org.eclipse.jgit.revwalk.RevCommit;
import org.isw2_project.models.Commit;
import org.isw2_project.models.LOCMetric;
import org.isw2_project.models.Metric;
import org.isw2_project.models.ProjectClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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


      // computeLocMetrics();
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
                if (filteredCommitsOfIssues.contains(commitThatTouchesTheClass)) {
                    // Se il commit corrente è presente nella lista, incrementa il contatore delle correzioni di difetti
                    numberOfFix++;
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
    /*
    private void computeLocMetrics() throws IOException {

        LOCMetric removedLOC = new LOCMetric();
        LOCMetric churnLOC = new LOCMetric();
        LOCMetric addedLOC = new LOCMetric();
        LOCMetric touchedLOC = new LOCMetric();


        for(ProjectClass projectClass : this.allProjectClasses) {
            // Impostazione delle variabili addedLOC
            addedLOC.setVal(0);
            addedLOC.setAvgVal(0);
            addedLOC.setMaxVal(0);

            // Impostazione delle variabili removedLOC
            removedLOC.setVal(0);
            removedLOC.setAvgVal(0);
            removedLOC.setMaxVal(0);

            // Impostazione delle variabili churnLOC
            churnLOC.setVal(0);
            churnLOC.setAvgVal(0);
            churnLOC.setMaxVal(0);

            // Impostazione delle variabili touchedLOC
            touchedLOC.setVal(0);
            touchedLOC.setAvgVal(0);
            touchedLOC.setMaxVal(0);


            this.gitExtractor.computeAddedAndDeletedLOC(projectClass);

            List<Integer> locAddedByClass = projectClass.getLOCAddedByClass();
            List<Integer> locDeletedByClass = projectClass.getLOCDeletedByClass();

            for (int i = 0; i < locAddedByClass.size(); i++) {
                // Ottieni il numero di linee di codice aggiunte e rimosse per l'elemento corrente
                int addedLineOfCode = locAddedByClass.get(i);
                int removedLineOfCode = locDeletedByClass.get(i);

                // Calcola il fattore di "churning", la differenza assoluta tra le linee aggiunte e rimosse
                int churningFactor = Math.abs(locAddedByClass.get(i) - locDeletedByClass.get(i));

                // Calcola il totale delle linee di codice "toccate", sommando le linee aggiunte e rimosse
                int touchedLinesOfCode = locAddedByClass.get(i) + locDeletedByClass.get(i);

                // Aggiorna i totali delle linee di codice aggiunte, rimosse, "churning" e "toccate"
                addedLOC.addToVal(addedLineOfCode);
                removedLOC.addToVal(removedLineOfCode);
                churnLOC.addToVal(churningFactor);
                touchedLOC.addToVal(touchedLinesOfCode);

                // Aggiorna il massimo registrato per le linee di codice aggiunte, se necessario
                if (addedLineOfCode > addedLOC.getMaxVal()) {
                    addedLOC.setMaxVal(addedLineOfCode);
                }

                // Aggiorna il massimo registrato per le linee di codice rimosse, se necessario
                if (removedLineOfCode > removedLOC.getMaxVal()) {
                    removedLOC.setMaxVal(removedLineOfCode);
                }

                // Aggiorna il massimo registrato per il fattore di "churning", se necessario
                if (churningFactor > churnLOC.getMaxVal()) {
                    churnLOC.setMaxVal(churningFactor);
                }

                // Aggiorna il massimo registrato per le linee di codice "toccate", se necessario
                if (touchedLinesOfCode > touchedLOC.getMaxVal()) {
                    touchedLOC.setMaxVal(touchedLinesOfCode);
                }

                //settare le metriche calcolate

                // Ottieni il numero di revisioni per la classe
                int nRevisions = projectClass.getMetric().getNumberOfRevisions();

                // Calcola la media delle linee di codice aggiunte per revisione, se ci sono dati disponibili
                if (!locAddedByClass.isEmpty()) {
                    addedLOC.setAvgVal(1.0 * addedLOC.getVal() / nRevisions);
                }

                // Calcola la media delle linee di codice rimosse per revisione, se ci sono dati disponibili
                if (!locDeletedByClass.isEmpty()) {
                    removedLOC.setAvgVal(1.0 * removedLOC.getVal() / nRevisions);
                }

                // Calcola la media del fattore di "churning" e delle linee di codice "toccate" per revisione, se ci sono dati disponibili
                if (!locAddedByClass.isEmpty() || !locDeletedByClass.isEmpty()) {
                    churnLOC.setAvgVal(1.0 * churnLOC.getVal() / nRevisions);
                    touchedLOC.setAvgVal(1.0 * touchedLOC.getVal() / nRevisions);
                }

                // Imposta le metriche relative alle linee di codice aggiunte per la classe nel suo oggetto Metric
                projectClass.getMetric().setAddedLOCMetrics(addedLOC.getVal(), addedLOC.getMaxVal(), addedLOC.getAvgVal());

                // Imposta le metriche relative alle linee di codice rimosse per la classe nel suo oggetto Metric
                projectClass.getMetric().setRemovedLOCMetrics(removedLOC.getVal(), removedLOC.getMaxVal(), removedLOC.getAvgVal());

                // Imposta le metriche relative al fattore di "churning" per la classe nel suo oggetto Metric
                projectClass.getMetric().setChurnMetrics(churnLOC.getVal(), churnLOC.getMaxVal(), churnLOC.getAvgVal());

                // Imposta le metriche relative alle linee di codice "toccate" per la classe nel suo oggetto Metric
                projectClass.getMetric().setTouchedLOCMetrics(touchedLOC.getVal(), touchedLOC.getMaxVal(), touchedLOC.getAvgVal());


            }
        }
    }
    */
}

package org.isw2_project.controllers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.isw2_project.models.Commit;
import org.isw2_project.models.Release;
import org.isw2_project.models.Ticket;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ExtractInfoGit {
    private List<Ticket> ticketList;

    private final   List<Release> releaseList;
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
        List<RevCommit> revCommitList = new ArrayList<>();
        List<Ref> branchList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        for (Ref branch : branchList) {
            Iterable<RevCommit> allRevCommits = git.log().add(repository.resolve(branch.getName())).call();
            for (RevCommit revCommit : allRevCommits) {
                if (!revCommitList.contains(revCommit)) {
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
        //confrontare gli oggetti Commit in base alle date dei commit dei committenti.
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
}

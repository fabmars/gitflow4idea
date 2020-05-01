package gitflow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFileManager;
import git4idea.branch.GitBranchUtil;
import git4idea.merge.GitMerger;
import git4idea.repo.GitRepository;
import gitflow.Gitflow;
import gitflow.GitflowBranchUtil;
import gitflow.GitflowBranchUtilManager;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.Nullable;

public abstract class GitflowAction extends DumbAwareAction {
    Gitflow myGitflow = ServiceManager.getService(Gitflow.class);
    GitRepository myRepo;
    GitflowBranchUtil branchUtil;

    VirtualFileManager virtualFileMananger;

    GitflowAction(String actionName){
        super(actionName);
        virtualFileMananger = VirtualFileManager.getInstance();
    }

    GitflowAction(GitRepository repo, String actionName){
        this(actionName);
        myRepo = repo;
        branchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();

        // if repo isn't set explicitly, such as in the case of starting from keyboard shortcut, infer it
        if (myRepo == null){
            myRepo = GitBranchUtil.getCurrentRepository(project);
            branchUtil = GitflowBranchUtilManager.getBranchUtil(myRepo);
        }
    }

    //returns true if merge successful, false otherwise
    public boolean handleMerge(Project project){
        //ugly, but required for intellij to catch up with the external changes made by
        //the CLI before being able to run the merge tool
        virtualFileMananger.syncRefresh();
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException ignored) {
        }


        GitflowActions.runMergeTool();
        myRepo.update();

        //if merge was completed successfully, finish the action
        //note that if it wasn't intellij is left in the "merging state", and git4idea provides no UI way to resolve it
	    //merging can be done via intellij itself or any other util
        int answer = Messages.showYesNoDialog(project, "Was the merge completed succesfully?", "Merge", Messages.getQuestionIcon());
        if (answer == Messages.YES){
            GitMerger gitMerger = new GitMerger(project);

            try {
                gitMerger.mergeCommit(gitMerger.getMergingRoots());
            } catch (VcsException e1) {
                NotifyUtil.notifyError(project, "Error", "Error committing merge result");
                e1.printStackTrace();
            }

            return true;
        }
        else{

	        NotifyUtil.notifyInfo(project,"Merge incomplete","To manually complete the merge choose VCS > Git > Resolve Conflicts.\n" +
			        "Once done, commit the merged files.\n");
            return false;
        }


    }
}

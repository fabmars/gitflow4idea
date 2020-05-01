package gitflow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;
import gitflow.ui.GitflowBranchChooseDialog;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class TrackReleaseAction extends AbstractTrackAction {

    TrackReleaseAction(){
        super("Track Release", BranchType.Release);
    }

    TrackReleaseAction(GitRepository repo){
        super(repo,"Track Release", BranchType.Release);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        List<String> remoteBranches = branchUtil.getRemoteBranchNames();
        //get only the branches with the proper prefix
        List<String> remoteReleaseBranches = remoteBranches.stream()
                .filter(item -> item.contains(branchUtil.getPrefixRelease())).collect(toList());

        final Project project = e.getProject();
        if (!remoteBranches.isEmpty()){
            GitflowBranchChooseDialog branchChoose = new GitflowBranchChooseDialog(project,remoteReleaseBranches);

            branchChoose.show();
            if (branchChoose.isOK()){
                String branchName = branchChoose.getSelectedBranchName();
                GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(project, myRepo);
                final String releaseName = gitflowConfigUtil.getReleaseNameFromBranch(branchName);
                final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(project);

                new Task.Backgroundable(project,"Tracking release "+releaseName,false){
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        GitCommandResult result = myGitflow.trackRelease(myRepo, releaseName, errorLineHandler);

                        if (result.success()) {
                            String trackedReleaseMessage = String.format(" A new remote tracking branch '%s%s' was created", branchUtil.getPrefixRelease(), releaseName);
                            NotifyUtil.notifySuccess(myProject, releaseName, trackedReleaseMessage);
                        }
                        else {
                            NotifyUtil.notifyError(myProject, "Error", "Please have a look at the Version Control console for more details");
                        }

                        myRepo.update();
                    }
                }.queue();
            }
        }
        else {
            NotifyUtil.notifyError(project, "Error", "No remote branches");
        }

    }
}
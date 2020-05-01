package gitflow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import gitflow.GitflowConfigUtil;
import gitflow.ui.GitflowBranchChooseDialog;
import gitflow.ui.NotifyUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class TrackFeatureAction extends AbstractTrackAction {

    TrackFeatureAction(){
        super("Track Feature", BranchType.Feature);
    }

    TrackFeatureAction(GitRepository repo){
        super(repo,"Track Feature", BranchType.Feature);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        List<String> remoteBranches = branchUtil.getRemoteBranchNames();
        //get only the branches with the proper prefix
        List<String> remoteFeatureBranches = remoteBranches.stream()
                .filter(item -> item.contains(branchUtil.getPrefixFeature())).collect(toList());

        final Project project = e.getProject();
        if (!remoteBranches.isEmpty()){
            GitflowBranchChooseDialog branchChoose = new GitflowBranchChooseDialog(project,remoteFeatureBranches);

            branchChoose.show();
            if (branchChoose.isOK()){
                String branchName = branchChoose.getSelectedBranchName();
                GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(project, myRepo);
                final String featureName = gitflowConfigUtil.getFeatureNameFromBranch(branchName);
                final GitRemote remote = branchUtil.getRemoteByBranch(branchName);
                final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(project);

                new Task.Backgroundable(project,"Tracking feature "+featureName,false){
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        GitCommandResult result = myGitflow.trackFeature(myRepo, featureName, remote, errorLineHandler);

                        if (result.success()) {
                            String trackedFeatureMessage = String.format("A new branch '%s%s' was created", branchUtil.getPrefixFeature(), featureName);
                            NotifyUtil.notifySuccess(myProject, featureName, trackedFeatureMessage);
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
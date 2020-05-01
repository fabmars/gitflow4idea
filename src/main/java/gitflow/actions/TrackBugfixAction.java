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

public class TrackBugfixAction extends AbstractTrackAction {

    TrackBugfixAction() {
        super("Track Bugfix", BranchType.Bugfix);
    }

    TrackBugfixAction(GitRepository repo) {
        super(repo, "Track Bugfix", BranchType.Bugfix);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        super.actionPerformed(e);

        List<String> remoteBranches = branchUtil.getRemoteBranchNames();
        //get only the branches with the proper prefix
        List<String> remoteBugfixBranches = remoteBranches.stream()
                .filter(item -> item.contains(branchUtil.getPrefixBugfix())).collect(toList());

        final Project project = e.getProject();
        if (!remoteBranches.isEmpty()) {
            GitflowBranchChooseDialog branchChoose = new GitflowBranchChooseDialog(project, remoteBugfixBranches);

            branchChoose.show();
            if (branchChoose.isOK()) {
                String branchName = branchChoose.getSelectedBranchName();

                GitflowConfigUtil gitflowConfigUtil = GitflowConfigUtil.getInstance(project, myRepo);
                final String bugfixName = gitflowConfigUtil.getBugfixNameFromBranch(branchName);
                final GitRemote remote = branchUtil.getRemoteByBranch(branchName);
                final GitflowErrorsListener errorLineHandler = new GitflowErrorsListener(project);

                new Task.Backgroundable(project, "Tracking bugfix " + bugfixName, false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        GitCommandResult result = myGitflow.trackBugfix(myRepo, bugfixName, remote, errorLineHandler);
                        if (result.success()) {
                            String trackedBugfixMessage = String.format("A new branch '%s%s' was created", branchUtil.getPrefixBugfix(), bugfixName);
                            NotifyUtil.notifySuccess(myProject, bugfixName, trackedBugfixMessage);
                        } else {
                            NotifyUtil.notifyError(myProject, "Error", "Please have a look at the Version Control console for more details");
                        }
                        myRepo.update();
                    }
                }.queue();
            }
        } else {
            NotifyUtil.notifyError(project, "Error", "No remote branches");
        }

    }
}
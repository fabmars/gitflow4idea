package gitflow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;

import java.util.HashMap;
import java.util.List;

/**
 * This class maps repos to their corresponding branch utils
 * Note that the static class is used across projects
 */

public class GitflowBranchUtilManager {
    private static HashMap<GitRepository, GitflowBranchUtil> repoBranchUtilMap;

    static public GitflowBranchUtil getBranchUtil(GitRepository repo){
        if (repoBranchUtilMap != null) {
            return repoBranchUtilMap.get(repo);
        } else {
            return null;
        }
    }

    static public void setupBranchUtil(Project project, GitRepository repo){
        GitflowBranchUtil gitflowBranchUtil = new GitflowBranchUtil(project, repo);
        repoBranchUtilMap.put(repo, gitflowBranchUtil);
        // clean up
        Disposer.register(repo, () -> repoBranchUtilMap.remove(repo));
    }

    /**
     * Repopulates the branchUtils for each repo
     * @param project
     */
    static public void update(Project project){
        if (repoBranchUtilMap == null){
            repoBranchUtilMap = new HashMap<>();
        }

        List<GitRepository> gitRepositories = GitUtil.getRepositoryManager(project).getRepositories();

        for(GitRepository repo : gitRepositories){
            GitflowBranchUtilManager.setupBranchUtil(project, repo);
        }
    }
}

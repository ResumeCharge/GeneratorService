package com.portfolio.generator.utilities.helpers;

import com.amazonaws.services.codecommit.AWSCodeCommit;
import com.amazonaws.services.codecommit.model.CreateBranchRequest;
import com.amazonaws.services.codecommit.model.CreateBranchResult;
import com.amazonaws.services.codecommit.model.DeleteBranchRequest;
import com.amazonaws.services.codecommit.model.DeleteBranchResult;
import org.springframework.stereotype.Component;

@Component
public class CodeCommitHelper implements ICodeCommitHelper {
  @Override
  public CreateBranchResult createBranch(final AWSCodeCommit codeCommit, final String repoName, final String branchName) {
    final CreateBranchRequest createBranchRequest = new CreateBranchRequest()
        .withBranchName(branchName)
        .withRepositoryName(repoName);
    return codeCommit.createBranch(createBranchRequest);
  }

  @Override
  public DeleteBranchResult deleteBranch(final AWSCodeCommit codeCommit, final String repoName, final String branchName) {
    final DeleteBranchRequest deleteBranchRequest = new DeleteBranchRequest()
        .withBranchName(branchName)
        .withRepositoryName(repoName);
    return codeCommit.deleteBranch(deleteBranchRequest);
  }
}

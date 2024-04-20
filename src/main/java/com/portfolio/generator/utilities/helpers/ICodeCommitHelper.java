package com.portfolio.generator.utilities.helpers;

import com.amazonaws.services.codecommit.AWSCodeCommit;
import com.amazonaws.services.codecommit.model.CreateBranchResult;
import com.amazonaws.services.codecommit.model.DeleteBranchResult;

public interface ICodeCommitHelper {
  CreateBranchResult createBranch(final AWSCodeCommit codeCommit, final String repoName, final String branchName);

  DeleteBranchResult deleteBranch(final AWSCodeCommit codeCommit, final String repoName, final String branchName);
}

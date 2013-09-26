package org.obiba.opal.core.vcs.git.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.obiba.opal.core.vcs.OpalGitException;
import org.obiba.opal.core.vcs.support.OpalGitUtils;

import com.google.common.base.Strings;

import edu.umd.cs.findbugs.annotations.Nullable;

public class OpalGitDiffCommand extends OpalGitCommand<List<String>> {

  private String path;

  private String commitId;

  private int nthCommit = 1;


  public OpalGitDiffCommand(@Nonnull Repository repository, @Nullable String datasourceName) {
    super(repository, datasourceName);
  }

  public OpalGitDiffCommand(@Nonnull Repository repository) {
    super(repository);
  }

  public OpalGitDiffCommand addPath(String value) {
    path = value;
    return this;
  }

  public OpalGitDiffCommand addCommitId(String value) {
    commitId = value;
    return this;
  }

  public OpalGitDiffCommand addNthCommit(int value) {
    nthCommit = value;
    return this;
  }

  @Override
  public List<String> execute() {
    ObjectReader reader = repository.newObjectReader();

    try {
      validate();
      DiffCurrentPreviousTreeParsersFactory test = new DiffCurrentPreviousTreeParsersFactory(reader).create();
      CanonicalTreeParser currentCommitParser = test.getCurrentCommitParser();
      AbstractTreeIterator previousCommitParser = test.getPreviousCommitParser();
      return CompareDiffTrees(currentCommitParser, previousCommitParser);

    } catch(IOException e) {
      throw new OpalGitException(e.getMessage(), e);
    } finally {
      reader.release();
    }
  }

  private List<String> CompareDiffTrees(CanonicalTreeParser currentCommitParser,
      AbstractTreeIterator previousCommitParser) throws IOException {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    List<String> diffEntries = new ArrayList<String>();
    DiffFormatter df = new DiffFormatter(out);
    df.setRepository(repository);
    df.setDiffComparator(RawTextComparator.DEFAULT);
    df.setDetectRenames(true);

    if(!Strings.isNullOrEmpty(path)) {
      df.setPathFilter(PathFilter.create(path));
    }

    List<DiffEntry> diffs = df.scan(currentCommitParser, previousCommitParser);

    for(DiffEntry diffEntry : diffs) {
      df.format(diffEntry);
      diffEntry.getOldId();
      diffEntries.add(out.toString("UTF-8"));
      out.reset();
    }

    return diffEntries;
  }

  private class DiffCurrentPreviousTreeParsersFactory {
    private ObjectReader reader;

    private CanonicalTreeParser currentCommitParser;

    private AbstractTreeIterator previousCommitParser;

    public DiffCurrentPreviousTreeParsersFactory(ObjectReader reader) {this.reader = reader;}

    public CanonicalTreeParser getCurrentCommitParser() {
      return currentCommitParser;
    }

    public AbstractTreeIterator getPreviousCommitParser() {
      return previousCommitParser;
    }

    public DiffCurrentPreviousTreeParsersFactory create() throws IOException {
      RevCommit currentCommit = getCommitById(commitId);

      if(currentCommit == null) {
        throw new OpalGitException(String.format("There are no commit for commit id '%s'", commitId));
      }

      currentCommitParser = new CanonicalTreeParser();
      currentCommitParser.reset(reader, currentCommit.getTree());

      previousCommitParser = null;
      RevCommit previousCommit = getCommitById(OpalGitUtils.getNthCommitId(commitId, nthCommit));

      if(previousCommit == null) {
        // currentCommit is the first commit in the tree
        previousCommitParser = new EmptyTreeIterator();
      } else {
        CanonicalTreeParser parser = new CanonicalTreeParser();
        parser.reset(reader, previousCommit.getTree());
        previousCommitParser = parser;
      }

      return this;
    }


    protected void validate() {
      if (Strings.isNullOrEmpty(commitId)) {
        throw new OpalGitException("Commit id cannot be null");
      }
    }
  }
}

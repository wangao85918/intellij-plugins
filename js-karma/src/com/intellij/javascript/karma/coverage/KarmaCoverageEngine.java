package com.intellij.javascript.karma.coverage;

import com.intellij.coverage.*;
import com.intellij.coverage.view.CoverageViewExtension;
import com.intellij.coverage.view.CoverageViewManager;
import com.intellij.coverage.view.DirectoryCoverageViewExtension;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageEngine extends CoverageEngine {

  public static final String ID = "KarmaJavaScriptTestRunnerCoverage";

  @Override
  public boolean isApplicableTo(@Nullable RunConfigurationBase configuration) {
    return configuration instanceof KarmaRunConfiguration;
  }

  @Override
  public boolean canHavePerTestCoverage(@Nullable RunConfigurationBase configuration) {
    return false;
  }

  @NotNull
  @Override
  public CoverageEnabledConfiguration createCoverageEnabledConfiguration(@Nullable RunConfigurationBase configuration) {
    return new KarmaCoverageEnabledConfiguration((KarmaRunConfiguration) configuration);
  }

  @Override
  public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner,
                                           @NotNull String name,
                                           @NotNull CoverageFileProvider coverageDataFileProvider,
                                           @Nullable String[] filters,
                                           long lastCoverageTimeStamp,
                                           @Nullable String suiteToMerge,
                                           boolean coverageByTestEnabled,
                                           boolean tracingEnabled,
                                           boolean trackTestFolders,
                                           Project project) {
    return new KarmaCoverageSuite(covRunner, name, coverageDataFileProvider, lastCoverageTimeStamp,
                                 coverageByTestEnabled, tracingEnabled,
                                 trackTestFolders, project, this);
  }

  @Override
  public CoverageSuite createCoverageSuite(@NotNull CoverageRunner covRunner,
                                           @NotNull String name,
                                           @NotNull CoverageFileProvider coverageDataFileProvider,
                                           @NotNull CoverageEnabledConfiguration config) {
    if (config instanceof KarmaCoverageEnabledConfiguration) {
      Project project = config.getConfiguration().getProject();
      return createCoverageSuite(covRunner, name, coverageDataFileProvider, null,
                                 new Date().getTime(), null, false, false, true, project);
    }
    return null;
  }

  @Override
  public CoverageSuite createEmptyCoverageSuite(@NotNull CoverageRunner coverageRunner) {
    return new KarmaCoverageSuite(this);
  }

  @NotNull
  @Override
  public CoverageAnnotator getCoverageAnnotator(Project project) {
    return KarmaCoverageAnnotator.getInstance(project);
  }

  @Override
  public boolean coverageEditorHighlightingApplicableTo(@NotNull PsiFile psiFile) {
    return psiFile instanceof JSFile;
  }

  @Override
  public boolean acceptedByFilters(@NotNull PsiFile psiFile, @NotNull CoverageSuitesBundle suite) {
    return true;
  }

  @Override
  public boolean recompileProjectAndRerunAction(@NotNull Module module,
                                                @NotNull CoverageSuitesBundle suite,
                                                @NotNull Runnable chooseSuiteAction) {
    return false;
  }

  @Override
  public String getQualifiedName(@NotNull File outputFile, @NotNull PsiFile sourceFile) {
    return getQName(sourceFile);
  }

  @Nullable
  private static String getQName(@NotNull PsiFile sourceFile) {
    final VirtualFile file = sourceFile.getVirtualFile();
    if (file == null) {
      return null;
    }
    final String filePath = file.getPath();
    if (filePath == null) {
      return null;
    }
    return SimpleCoverageAnnotator.getFilePath(filePath);
  }

  @NotNull
  @Override
  public Set<String> getQualifiedNames(@NotNull PsiFile sourceFile) {
    final String qName = getQName(sourceFile);
    return qName != null ? Collections.singleton(qName) : Collections.<String>emptySet();
  }

  @Override
  public boolean includeUntouchedFileInCoverage(@NotNull String qualifiedName,
                                                @NotNull File outputFile,
                                                @NotNull PsiFile sourceFile,
                                                @NotNull CoverageSuitesBundle suite) {
    return false;
  }

  @Override
  public boolean isReportGenerationAvailable(@NotNull Project project,
                                             @NotNull DataContext dataContext,
                                             @NotNull CoverageSuitesBundle currentSuite) {
    return false;
  }

  @Override
  public void generateReport(@NotNull Project project,
                             @NotNull DataContext dataContext,
                             @NotNull CoverageSuitesBundle currentSuiteBundle) {
  }

  @Override
  public List<Integer> collectSrcLinesForUntouchedFile(@NotNull File classFile, @NotNull CoverageSuitesBundle suite) {
    return null;
  }

  @Override
  public List<PsiElement> findTestsByNames(@NotNull String[] testNames, @NotNull Project project) {
    return Collections.emptyList();
  }

  @Override
  public String getTestMethodName(@NotNull PsiElement element, @NotNull AbstractTestProxy testProxy) {
    return null;
  }

  @Override
  public String getPresentableText() {
    return ID;
  }

  @Override
  public boolean coverageProjectViewStatisticsApplicableTo(final VirtualFile fileOrDir) {
    return !(fileOrDir.isDirectory()) && fileOrDir.getFileType() instanceof JavaScriptFileType;
  }

  @Override
  public CoverageViewExtension createCoverageViewExtension(Project project,
                                                           CoverageSuitesBundle suiteBundle,
                                                           CoverageViewManager.StateBean stateBean) {
    return new DirectoryCoverageViewExtension(project, getCoverageAnnotator(project), suiteBundle, stateBean);
  }
}

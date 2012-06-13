package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.execution.tree.NavUtils;
import com.google.jstestdriver.idea.util.EscapeUtils;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testIntegration.TestLocationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
* @author Sergey Simonchik
*/
public class JstdTestLocationProvider implements TestLocationProvider {

  private static final String PROTOCOL_ID__CONFIG_FILE = "config";
  private static final String PROTOCOL_ID__TEST_CASE = "testCase";
  private static final String PROTOCOL_ID__TEST = "test";

  @NotNull
  @Override
  public List<Location> getLocation(@NotNull String protocolId, @NotNull String locationData, Project project) {
    final Location location;
    if (PROTOCOL_ID__CONFIG_FILE.equals(protocolId)) {
      location = findConfigFile(locationData, project);
    }
    else if (PROTOCOL_ID__TEST_CASE.equals(protocolId) || PROTOCOL_ID__TEST.equals(protocolId)) {
      location = findTest(locationData, project);
    }
    else {
      location = null;
    }
    if (location != null) {
      return Collections.singletonList(location);
    }
    return Collections.emptyList();
  }

  @Nullable
  private static Location findTest(@NotNull String locationData, Project project) {
    List<String> path = EscapeUtils.split(locationData, ':');
    if (path.size() < 2) {
      return null;
    }
    String jsTestFilePath = path.get(0);
    String testCaseName = path.get(1);
    String testName = path.size() > 2 ? path.get(2) : null;
    PsiElement element = NavUtils.findPsiElement(
      project,
      new File(jsTestFilePath),
      testCaseName,
      testName
    );
    if (element != null) {
      return PsiLocation.fromPsiElement(element);
    }
    return null;
  }

  @Nullable
  private static Location findConfigFile(@NotNull String locationData, Project project) {
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(locationData));
    if (virtualFile != null && virtualFile.isValid()) {
      PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
      if (psiFile != null && psiFile.isValid()) {
        return PsiLocation.fromPsiElement(psiFile);
      }
    }
    return null;
  }
}
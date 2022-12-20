package ink.organics.pojo2json;

import com.google.gson.GsonBuilder;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiUtil;
import ink.organics.pojo2json.domain.JsonSchemaArray;
import ink.organics.pojo2json.domain.JsonSchemaItem;
import ink.organics.pojo2json.domain.JsonSchemaObject;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UastLanguagePlugin;
import org.jetbrains.uast.UastUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class POJO2JsonSchemaWithCodeAction extends POJO2JsonSchemaAction {

    protected String postProcess(String text){
        return String.format("{\n" +
                             "    \"type\": \"object\",\n" +
                             "    \"properties\": {\n" +
                             "        \"code\": {\n" +
                             "            \"type\": \"string\",\n" +
                             "            \"mock\": {\n" +
                             "                \"mock\": \"OK\"\n" +
                             "            }\n" +
                             "        },\n" +
                             "        \"msg\": {\n" +
                             "            \"type\": \"string\"\n" +
                             "        },\n" +
                             "        \"data\": %s,\n" +
                             "        \"currentTime\": {\n" +
                             "             \"type\": \"integer\"\n" +
                             "         }\n" +
                             "    },\n" +
                             "    \"required\": [\n" +
                             "        \"code\"\n" +
                             "    ]\n" +
                             "}", text);
    }

}



package ink.organics.pojo2json;

import com.google.gson.Gson;
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

public class POJO2JsonSchemaAction extends AnAction {


    private final NotificationGroup notificationGroup =
            NotificationGroupManager.getInstance().getNotificationGroup("pojo2json.NotificationGroup");

    //    private final Map<String, JsonFakeValuesService> normalTypes = new HashMap<>();
    private final ArrayList<String> normalTypes = new ArrayList<>();

    private final List<String> iterableTypes = List.of(
            "Iterable",
            "Collection",
            "List",
            "Set");

    private final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();

    public POJO2JsonSchemaAction() {

        normalTypes.add("java.lang.Boolean");
        normalTypes.add("java.lang.Byte");
        normalTypes.add("java.lang.Short");
        normalTypes.add("java.lang.Integer");
        normalTypes.add("java.lang.Long");
        normalTypes.add("java.lang.String");
        normalTypes.add("java.lang.Float");
        normalTypes.add("java.lang.Double");
        normalTypes.add("java.math.BigDecimal");
        normalTypes.add("java.lang.Character");
        normalTypes.add("java.lang.CharSequence");
        normalTypes.add("java.util.Date");
        normalTypes.add("java.util.UUID");
        normalTypes.add("java.time.LocalDateTime");
        normalTypes.add("java.time.LocalDate");
        normalTypes.add("java.time.LocalTime");
        normalTypes.add("java.time.ZonedDateTime");
        normalTypes.add("java.time.Instant");
        normalTypes.add("java.time.YearMonth");
    }

    protected String postProcess(String text){
        return text;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        boolean menuAllowed = false;
        if (psiFile != null && editor != null && project != null) {
            menuAllowed = UastLanguagePlugin.Companion.getInstances()
                    .stream()
                    .anyMatch(l -> l.isFileSupported(psiFile.getName()));
        }
        e.getPresentation().setEnabledAndVisible(menuAllowed);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        try {
            final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
            final String fileText = psiFile.getText();
            int offset = fileText.contains("class") ? fileText.indexOf("class") : fileText.indexOf("record");
            if (offset < 0) {
                throw new KnownException("Can't find class scope.");
            }
            PsiElement elementAt = psiFile.findElementAt(offset);
            // ADAPTS to all JVM platform languages
            UClass uClass = UastUtils.findContaining(elementAt, UClass.class);

            JsonSchemaObject obj = parseClass(uClass.getJavaPsi(), 0, List.of());
            String json = gsonBuilder.create().toJson(obj);
            json = postProcess(json);
            StringSelection selection = new StringSelection(json);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            String message = "Convert " + uClass.getName() + " to JSON success, copied to clipboard.";
            Notification success = notificationGroup.createNotification(message, NotificationType.INFORMATION);
            Notifications.Bus.notify(success, project);


        } catch (KnownException ex) {
            Notification warn = notificationGroup.createNotification(ex.getMessage(), NotificationType.WARNING);
            Notifications.Bus.notify(warn, project);
        } catch (Exception ex) {
            Notification error = notificationGroup.createNotification("Convert to JSON failed. " + ex.getMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
        }
    }


    private JsonSchemaObject parseClass(PsiClass psiClass, int level, List<String> ignoreProperties) {
        PsiAnnotation annotation = psiClass.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnoreType.class.getName());
        if (annotation != null) {
            return null;
        }
        JsonSchemaObject obj = new JsonSchemaObject();
        obj.properties = Arrays.stream(psiClass.getAllFields())
                .map(field -> parseField(field, level, ignoreProperties))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (ov, nv) -> ov, LinkedHashMap::new));
        return obj;
    }

    private Map.Entry<String, Object> parseField(PsiField field, int level, List<String> ignoreProperties) {
        // 移除所有 static 属性，这其中包括 kotlin 中的 companion object 和 INSTANCE
        if (field.hasModifierProperty(PsiModifier.STATIC)) {
            return null;
        }

        if (ignoreProperties.contains(field.getName())) {
            return null;
        }

        PsiAnnotation annotation = field.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class.getName());
        if (annotation != null) {
            return null;
        }

        PsiDocComment docComment = field.getDocComment();
        if (docComment != null) {
            PsiDocTag psiDocTag = docComment.findTagByName("JsonIgnore");
            if (psiDocTag != null && "JsonIgnore".equals(psiDocTag.getName())) {
                return null;
            }

            ignoreProperties = POJO2JsonPsiUtils.docTextToList("@JsonIgnoreProperties", docComment.getText());
        } else {
            annotation = field.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnoreProperties.class.getName());
            if (annotation != null) {
                ignoreProperties = POJO2JsonPsiUtils.arrayTextToList(annotation.findAttributeValue("value").getText());
            }
        }

        String fieldKey = parseFieldKey(field);
        if (fieldKey == null) {
            return null;
        }
        Object fieldValue = parseFieldValue(field, level, ignoreProperties);
        if (fieldValue == null) {
            return null;
        }
        return Map.entry(fieldKey, fieldValue);
    }

    private String parseFieldKey(PsiField field) {

        PsiAnnotation annotation = field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class.getName());
        if (annotation != null) {
            String fieldName = POJO2JsonPsiUtils.psiTextToString(annotation.findAttributeValue("value").getText());
            if (StringUtils.isNotBlank(fieldName)) {
                return fieldName;
            }
        }

        annotation = field.getAnnotation("com.alibaba.fastjson.annotation.JSONField");
        if (annotation != null) {
            String fieldName = POJO2JsonPsiUtils.psiTextToString(annotation.findAttributeValue("name").getText());
            if (StringUtils.isNotBlank(fieldName)) {
                return fieldName;
            }
        }
        return field.getName();
    }

    private Object parseFieldValue(PsiField field, int level, List<String> ignoreProperties) {
        return parseFieldValueType(field.getType(), field, level, ignoreProperties);
    }

    private Object parseFieldValueType(PsiType type, PsiField baseField, int level, List<String> ignoreProperties) {

        level = ++level;



        if (type instanceof PsiPrimitiveType || normalTypes.contains(type.getCanonicalText())) {       //primitive Type
//            System.out.println("thisType:"+type.getCanonicalText());
            return getPrimitiveTypeValue(type, baseField);

        } else if (type instanceof PsiArrayType) {   //array type

            PsiType deepType = type.getDeepComponentType();
            Object obj = parseFieldValueType(deepType, baseField, level, ignoreProperties);
            return new JsonSchemaArray(findDescription(baseField), obj);

        } else {    //reference Type

            PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);

            if (psiClass == null) {
                return null;
            }

            if (psiClass.isEnum()) { // enum

                return new JsonSchemaItem("string", findDescription(baseField), null);

            } else {

                List<String> fieldTypeNames = new ArrayList<>();

                fieldTypeNames.add(type.getPresentableText());
                fieldTypeNames.addAll(Arrays.stream(type.getSuperTypes())
                        .map(PsiType::getPresentableText).collect(Collectors.toList()));


                boolean iterable = fieldTypeNames.stream().map(typeName -> {
                    int subEnd = typeName.indexOf("<");
                    return typeName.substring(0, subEnd > 0 ? subEnd : typeName.length());
                }).anyMatch(iterableTypes::contains);

                if (iterable) {// Iterable

                    PsiType deepType = PsiUtil.extractIterableTypeParameter(type, false);
                    Object obj = parseFieldValueType(deepType, baseField, level, ignoreProperties);
                    return new JsonSchemaArray(findDescription(baseField), obj);

                } else { // Object

                    if (level > 500) {
                        throw new KnownException("This class reference level exceeds maximum limit or has nested references!");
                    }

                    return parseClass(psiClass, level, ignoreProperties);

                }
            }
        }
    }

    public String findDescription(PsiField field) {

        PsiAnnotation annotation = field.getAnnotation(com.fasterxml.jackson.annotation.JsonPropertyDescription.class.getName());
        if (annotation != null) {
            String text = POJO2JsonPsiUtils.psiTextToString(annotation.findAttributeValue("value").getText());
            if (StringUtils.isNotBlank(text)) {
                return text;
            }
        }

        PsiDocComment docComment = field.getDocComment();
        if (docComment != null) {
            PsiDocTag psiDocTag = docComment.findTagByName("JsonPropertyDescription");
            if (psiDocTag != null) {
                return psiDocTag.getContainingComment().getText();
            }else{
                return formatComment(docComment.getText());
            }

        }
        return null;
    }

    public static String formatComment(String text){
        String[] ps = text.replaceAll("[/* ]","").split("\n");
        List<String> filtered = new ArrayList<>();
        for (String p : ps) {
            if (!Objects.equals(p, "")) {
                filtered.add(p);
            }
        }
        if(filtered.size()>0){
            return filtered.get(0);
        }
        return null;
    }



    public Object getPrimitiveTypeValue(PsiType type, PsiField field) {
        String typeName = type.getCanonicalText();
        if(typeName.contains(".")){
            typeName = typeName.substring(typeName.lastIndexOf(".")+1);
        }
        switch (typeName) {
            case "boolean":
            case "Boolean":
                return new JsonSchemaItem("boolean", findDescription(field), null);
            case "byte":
            case "Byte":
            case "short":
            case "Short":
            case "int":
            case "Integer":
            case "long":
            case "Long":
            case "Instant":
                return new JsonSchemaItem("integer", findDescription(field), null);
            case "float":
            case "Float":
            case "double":
            case "Double":
            case "BigDecimal":
                return new JsonSchemaItem("number", findDescription(field), null);
            default:
                return new JsonSchemaItem("string", findDescription(field), null);
        }

    }

}



package ink.organics.pojo2json.domain;

public class JsonSchemaItem {
    public String type;
    public String description;
    public String format;

    public JsonSchemaItem(String type, String description, String format) {
        this.type = type;
        this.description = description;
        this.format = format;
    }
}

package ink.organics.pojo2json.domain;

public class JsonSchemaArray {
    public String type = "array";
    public String description;
    public Object items;

    public JsonSchemaArray(String description, Object items) {
        this.description = description;
        this.items = items;
    }
}

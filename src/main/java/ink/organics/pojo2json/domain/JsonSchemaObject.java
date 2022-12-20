package ink.organics.pojo2json.domain;

public class JsonSchemaObject {
    public String type = "object";
    public String description;
    public Object properties;

    public JsonSchemaObject(){

    }
    public JsonSchemaObject(String description, Object properties) {
        this.description = description;
        this.properties = properties;
    }
}

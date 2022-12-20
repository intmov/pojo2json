package ink.organics.pojo2json;

public class POJO2JsonSchemaWithPageInfoAction extends POJO2JsonSchemaAction {

    protected String postProcess(String text){
        return String.format("{\n" +
                             "     \"type\": \"object\",\n" +
                             "     \"properties\": {\n" +
                             "         \"code\": {\n" +
                             "             \"type\": \"string\",\n" +
                            "            \"mock\": {\n" +
                            "                \"mock\": \"OK\"\n" +
                            "            }\n" +
                             "         },\n" +
                             "         \"msg\": {\n" +
                             "             \"type\": \"null\"\n" +
                             "         },\n" +
                             "         \"data\": {\n" +
                             "             \"type\": \"object\",\n" +
                             "             \"properties\": {\n" +
                             "                 \"pageSize\": {\n" +
                             "                     \"type\": \"integer\"\n" +
                             "                 },\n" +
                             "                 \"pageNumber\": {\n" +
                             "                     \"type\": \"integer\"\n" +
                             "                 },\n" +
                             "                 \"totalCount\": {\n" +
                             "                     \"type\": \"integer\"\n" +
                             "                 },\n" +
                             "                 \"items\": {\"type\": \"array\", \"items\": %s\n}\n" +
                             "             },\n" +
                             "             \"required\": [\n" +
                             "                 \"pageSize\",\n" +
                             "                 \"pageNumber\",\n" +
                             "                 \"totalCount\"\n" +
                             "             ]\n" +
                             "         },\n" +
                             "         \"currentTime\": {\n" +
                             "             \"type\": \"integer\"\n" +
                             "         }\n" +
                             "     },\n" +
                             "     \"required\": [\n" +
                             "         \"code\",\n" +
                             "         \"data\"\n" +
                             "     ]\n" +
                             " }", text);
    }

}



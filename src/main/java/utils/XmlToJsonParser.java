//package utils;
//
//import org.json.JSONObject;
//import org.json.XML;
//
//public class XmlToJsonParser {
//
//    public static int PRETTY_PRINT_INDENT_FACTOR = 4;
//    public static String TEST_XML_STRING =
//            "<breakfast_menu>\n" +
//                    "<food>\n" +
//                    "<name>Belgian Waffles</name>\n" +
//                    "<price>$5.95</price>\n" +
//                    "<description>\n" +
//                    "Two of our famous Belgian Waffles with plenty of real maple syrup\n" +
//                    "</description>\n" +
//                    "<calories>650</calories>\n" +
//                    "</food>\n" +
//                    "<food>\n" +
//                    "<name>Strawberry Belgian Waffles</name>\n" +
//                    "<price>$7.95</price>\n" +
//                    "<description>\n" +
//                    "Light Belgian waffles covered with strawberries and whipped cream\n" +
//                    "</description>\n" +
//                    "<calories>900</calories>\n" +
//                    "</food>\n" +
//                    "</breakfast_menu>";
//
//    public static void main(String[] args) {
//        try {
//            convertToJson(TEST_XML_STRING);
//
//        } catch (Exception je) {
//            System.out.println(je.toString());
//        }
//    }
//
//    public static JSONObject convertToJson(String xmlWord) {
//        try {
//            JSONObject xmlJSONObj = XML.toJSONObject(TEST_XML_STRING);
//            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
//            return xmlJSONObj;
//        } catch (Exception je) {
//            System.out.println(je.toString());
//        }
//        return null;
//    }
//
//
//}
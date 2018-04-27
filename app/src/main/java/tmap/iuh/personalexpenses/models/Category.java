package tmap.iuh.personalexpenses.models;

import java.util.HashMap;

public class Category {

    public static String[] mFieldKey = new String[] {
            "foodAndDining", "utilities", "autoAndTransport", "personal",
            "home", "clothing", "entertainment", "giftsAndDonations",
            "healthAndFitness", "other"
    };

    public static String[] mFieldName = new String[] {
            "Ăn uống", "Dịch vụ sinh hoạt", "Đi lại", "Phát triển bản thân",
            "Nhà cửa", "Trang phục", "Hưởng thụ", "Hiếu hỉ",
            "Sức khỏe", "Khác"
    };

    public static String getFieldKey(String value) {
        switch (value) {
            case "Ăn uống":
                return "foodAndDining";
            case "Dịch vụ sinh hoạt":
                return "utilities";
            case "Đi lại":
                return "autoAndTransport";
            case "Phát triển bản thân":
                return "personal";
            case "Nhà cửa":
                return "home";
            case "Trang phục":
                return "clothing";
            case "Hưởng thụ":
                return "entertainment";
            case "Hiếu hỉ":
                return "giftsAndDonations";
            case "Sức khỏe":
                return "healthAndFitness";
            case "Khác":
                return "other";
            default:
                return null;
        }
    }

    public static String getFieldName(String value) {
        switch (value) {
            case "foodAndDining":
                return "Ăn uống";
            case "utilities":
                return "Dịch vụ sinh hoạt";
            case "autoAndTransport":
                return "Đi lại";
            case "personal":
                return "Phát triển bản thân";
            case "home":
                return "Nhà cửa";
            case "clothing":
                return "Trang phục";
            case "entertainment":
                return "Hưởng thụ";
            case "giftsAndDonations":
                return "Hiếu hỉ";
            case "healthAndFitness":
                return "Sức khỏe";
            case "other":
                return "Khác";
            default:
                return null;
        }
    }

    private Category() {
    }

    //    private HashMap<String, Object> categoryMap;
//    private static Category instance = new Category();
//
//    private Category() {
//        categoryMap.put("Ăn uống", "foodAndDining");
//        categoryMap.put("Dịch vụ sinh hoạt", "utilities");
//        categoryMap.put("Đi lại", "autoAndTransport");
//        categoryMap.put("Phát triển bản thân", "personal");
//        categoryMap.put("Nhà cửa", "home");
//        categoryMap.put("Trang phục", "clothing");
//        categoryMap.put("Hưởng thụ", "entertainment");
//        categoryMap.put("Hiếu hỉ", "giftsAndDonations");
//        categoryMap.put("Sức khỏe", "healthAndFitness");
//        categoryMap.put("Con cái", "kids");
//        categoryMap.put("Khác", "other");
//    }
//
//    public static Category getInstance() {
//        return instance;
//    }
}

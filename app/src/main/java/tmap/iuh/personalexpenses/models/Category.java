package tmap.iuh.personalexpenses.models;

import java.util.HashMap;

public class Category {

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

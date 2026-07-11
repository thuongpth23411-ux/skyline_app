package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;
import java.util.Locale;
import java.util.Map;

public class Promotion {
    @SerializedName("_id")
    private Object idObj;
    
    @SerializedName("promotionId")
    private String promotionId;
    
    @SerializedName("promotionCode")
    private String promotionCode;
    
    @SerializedName("promotionName")
    private String promotionName;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("promotionCategory")
    private String promotionCategory;
    
    @SerializedName("imageUrl")
    private String imageUrl;
    
    @SerializedName("discountType")
    private String discountType;
    
    @SerializedName("discountValue")
    private double discountValue;
    
    @SerializedName("maxDiscount")
    private double maxDiscount;
    
    @SerializedName("minimumOrder")
    private double minimumOrder;
    
    @SerializedName("startDate")
    private String startDate;
    
    @SerializedName("endDate")
    private String endDate;
    
    @SerializedName("quantity")
    private int quantity;
    
    @SerializedName("applicableAirline")
    private String applicableAirline;
    
    @SerializedName("status")
    private String status;

    private int imageRes; // Dùng cho local fallback (Home)

    public Promotion(String title, String expiryDate, int imageRes) {
        this.promotionName = title;
        this.endDate = expiryDate;
        this.imageRes = imageRes;
    }

    public String getId() {
        if (idObj instanceof String) return (String) idObj;
        if (idObj instanceof Map) {
            return (String) ((Map<?, ?>) idObj).get("$oid");
        }
        return "";
    }
    
    public String getTitle() { return promotionName != null ? promotionName : ""; }
    public String getDescription() { return description != null ? description : ""; }
    public String getCategory() { return promotionCategory != null ? promotionCategory : ""; }
    public String getImageUrl() { return imageUrl; }

    public String getFullImageUrl() {
        return RetrofitClient.formatUrl(imageUrl);
    }
    
    public String getExpiryDate() { 
        if (endDate == null || endDate.isEmpty()) return "Vô thời hạn";
        if (endDate.contains("T")) return endDate.split("T")[0];
        return endDate;
    }
    
    public String getValue() { 
        if ("FIXED".equalsIgnoreCase(discountType)) {
            return String.format(Locale.getDefault(), "%,.0fđ", discountValue).replace(",", ".");
        }
        return String.format(Locale.getDefault(), "%.0f%%", discountValue);
    }
    
    public String getCode() { return promotionCode != null ? promotionCode : ""; }
    public int getImageRes() { return imageRes; }
}

const mongoose = require("mongoose");

const promotionSchema = new mongoose.Schema({
    promotionId: { type: String, required: true },
    promotionCode: { type: String, required: true },
    promotionName: { type: String, required: true },
    description: { type: String },
    promotionCategory: { type: String }, // "MEMBER", "EXCLUSIVE", "PAYMENT", "NEW_USER"
    imageUrl: { type: String },
    discountType: { type: String }, // "FIXED", "PERCENT"
    discountValue: { type: Number },
    maxDiscount: { type: Number },
    minimumOrder: { type: Number },
    startDate: { type: Date },
    endDate: { type: Date },
    quantity: { type: Number },
    applicableAirline: { type: String },
    status: { type: String, default: "Active" }
});

module.exports = mongoose.model("Promotion", promotionSchema);

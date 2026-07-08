const mongoose = require("mongoose");

const promotionSchema = new mongoose.Schema({
    promotionName: { type: String, required: true },
    description: { type: String },
    imageUrl: { type: String, required: true },
    endDate: { type: Date },
    status: { type: String, default: "Active" }
}, { collection: 'promotions' }); // Đảm bảo trỏ đúng vào collection 'promotions'

module.exports = mongoose.model("Promotion", promotionSchema);

const mongoose = require("mongoose");

const rankBenefitSchema = new mongoose.Schema({
    rank: { type: String, required: true }, // "Đồng", "Bạc", "Vàng"
    title: { type: String, required: true },
    description: { type: String, required: true },
    iconType: { type: String, default: "default" }
}, { collection: 'rank_benefits' });

module.exports = mongoose.model("RankBenefit", rankBenefitSchema);

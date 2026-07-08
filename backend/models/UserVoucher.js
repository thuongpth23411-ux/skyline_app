const mongoose = require("mongoose");

const userVoucherSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    promotionId: { type: mongoose.Schema.Types.ObjectId, ref: "Promotion", required: true },
    savedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model("UserVoucher", userVoucherSchema);

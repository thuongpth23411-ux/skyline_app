const mongoose = require("mongoose");

const pointHistorySchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    points: { type: Number, required: true },
    type: { type: String, enum: ["EARN", "REDEEM", "REVOKE"], default: "EARN" }, // EARN: cộng, REDEEM: đổi quà, REVOKE: thu hồi do hủy vé
    amount: { type: Number }, // Số tiền tương ứng (nếu có)
    description: { type: String },
    bookingCode: { type: String },
    date: { type: Date, default: Date.now }
}, {
    timestamps: true
});

module.exports = mongoose.model("PointHistory", pointHistorySchema);

const mongoose = require("mongoose");

const pointHistorySchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    points: { type: Number, required: true },
    type: { type: String, enum: ["EARN", "REDEEM", "REVOKE"], default: "EARN" }, // EARN: cộng, REDEEM: đổi quà, REVOKE: thu hồi do hủy vé
    amount: { type: Number }, // Số tiền thanh toán hoặc giá trị quy đổi
    description: { type: String }, // VD: "Tích điểm từ đơn hàng SKL123456", "Thu hồi điểm do hủy vé SKL123456"
    bookingCode: { type: String },
    status: { type: String, enum: ["COMPLETED", "CANCELLED", "PENDING"], default: "COMPLETED" },
    transactionDate: { type: Date, default: Date.now }, // Ngày thực hiện giao dịch (thanh toán hoặc hủy)
    date: { type: Date, default: Date.now } // Ngày tạo bản ghi lịch sử
}, {
    timestamps: true
});

module.exports = mongoose.model("PointHistory", pointHistorySchema);

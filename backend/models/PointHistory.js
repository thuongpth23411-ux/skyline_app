const mongoose = require("mongoose");

const pointHistorySchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    points: { type: Number, required: true },
    type: { type: String, enum: ["EARN", "SPEND"], default: "EARN" },
    description: { type: String },
    bookingCode: { type: String },
    date: { type: Date, default: Date.now }
}, {
    timestamps: true
});

module.exports = mongoose.model("PointHistory", pointHistorySchema);

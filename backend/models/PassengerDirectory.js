const mongoose = require("mongoose");

const passengerDirectorySchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    passengerName: { type: String, required: true },
    passengerPhone: { type: String },
    passengerCccd: { type: String },
    passengerDob: { type: String },
    passengerEmail: { type: String }
}, { timestamps: true });

module.exports = mongoose.model("PassengerDirectory", passengerDirectorySchema, "passengerdirectory");

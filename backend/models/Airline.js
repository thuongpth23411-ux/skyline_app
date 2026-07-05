const mongoose = require("mongoose");

const airlineSchema = new mongoose.Schema({
    airlineId: { type: String, required: true, unique: true },
    airlineName: { type: String, required: true },
    logoUrl: { type: String },
    isActive: { type: Boolean, default: true }
});

module.exports = mongoose.model("Airline", airlineSchema, "airlines");

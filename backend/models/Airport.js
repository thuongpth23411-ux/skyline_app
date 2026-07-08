const mongoose = require("mongoose");

const airportSchema = new mongoose.Schema({
    airportId: { type: String, required: true, unique: true },
    airportName: { type: String, required: true },
    city: { type: String, required: true },
    country: { type: String, required: true },
    isActive: { type: Boolean, default: true }
});

module.exports = mongoose.model("Airport", airportSchema, "airports");

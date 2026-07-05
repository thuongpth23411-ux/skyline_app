const mongoose = require("mongoose");

const flightSchema = new mongoose.Schema({
    flightId: { type: String, required: true, unique: true },
    airlineId: { type: String, required: true }, // Referencing airlineId from airlines collection
    flightNumber: { type: String, required: true },
    fromAirportId: { type: String, required: true }, // Referencing airportId
    toAirportId: { type: String, required: true },
    departureDate: { type: String, required: true }, // YYYY-MM-DD
    departureAt: { type: String, required: true },
    arrivalAt: { type: String, required: true },
    durationMinutes: { type: Number },
    isDirect: { type: Boolean, default: true },
    aircraftModel: { type: String },
    priceOptions: { type: Array },
    currency: { type: String, default: "VND" },
    flightStatus: { type: String, default: "SCHEDULED" }
});

module.exports = mongoose.model("Flight", flightSchema, "flights");

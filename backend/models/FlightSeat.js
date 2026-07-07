const mongoose = require("mongoose");

const flightSeatSchema = new mongoose.Schema({
    seatId: { type: String, required: true, unique: true },
    flightId: { type: String, required: true },
    seatNumber: { type: String, required: true },
    rowNumber: { type: Number, required: true },
    seatColumn: { type: String, required: true },
    cabinClass: { type: String, required: true }, // "ECONOMY", "BUSINESS"
    seatStatus: { type: String, default: "AVAILABLE" }, // "AVAILABLE", "OCCUPIED", "HELD"
    holdToken: { type: String, default: null },
    holdExpiresAt: { type: Date, default: null }
});

module.exports = mongoose.model("FlightSeat", flightSeatSchema, "flight_seats");
4const mongoose = require("mongoose");

const ticketSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    flightId: { type: mongoose.Schema.Types.ObjectId, ref: "Flight", required: true },
    bookingCode: { type: String, required: true, unique: true },
    seatNumber: { type: String, required: true },
    ticketClass: { type: String, required: true }, // Phổ thông, Thương gia, v.v.
    status: { type: String, default: "UPCOMING" }, // UPCOMING, COMPLETED, CANCELLED
    passengerName: { type: String, required: true },
    totalPrice: { type: Number, required: true },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model("Ticket", ticketSchema, "tickets");

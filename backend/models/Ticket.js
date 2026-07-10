const mongoose = require("mongoose");

const ticketSchema = new mongoose.Schema({
    ticketId: { type: String, required: true },
    bookingCode: { type: String, required: true },
    userId: { type: String, required: true }, // Có thể là String hoặc ObjectId tùy DB
    flightId: { type: String, required: true }, // ID chuỗi của chuyến bay
    seatId: { type: String },
    passengerName: { type: String },
    passengerType: { type: String },
    ticketType: { type: String }, // "Departure", "Return"
    totalAmount: { type: Number },
    paymentStatus: { type: String },
    ticketStatus: { type: String }, // "Booked", "Cancelled", v.v.
    bookedAt: { type: Date }
}, {
    timestamps: true
});

module.exports = mongoose.model("Ticket", ticketSchema, "tickets");

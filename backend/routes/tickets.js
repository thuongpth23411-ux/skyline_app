const express = require("express");
const router = express.Router();
const Ticket = require("../models/Ticket");
const Flight = require("../models/Flight");
const Airport = require("../models/Airport");
const Airline = require("../models/Airline");
const jwt = require("jsonwebtoken");

const verifyToken = (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith("Bearer ")) return res.status(401).json({ success: false });
    const token = authHeader.split(" ")[1];
    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.userId = decoded.id;
        next();
    } catch (err) { res.status(401).json({ success: false }); }
};

router.post("/create", async (req, res) => {
    try {
        const { userId, flights, passengerName, totalAmount, paymentMethod } = req.body;
        const bookingCode = "SKL" + Math.floor(100000 + Math.random() * 900000);

        const createdTickets = [];

        for (let i = 0; i < flights.length; i++) {
            const flight = flights[i];
            const ticketId = "TKT" + Math.floor(10000000 + Math.random() * 90000000);

            const newTicket = new Ticket({
                ticketId: ticketId,
                bookingCode: bookingCode,
                userId: userId,
                flightId: flight.flightId,
                seatId: flight.seatNumber || "N/A",
                passengerName: passengerName,
                passengerType: "Adult",
                ticketType: flight.ticketType || "OneWay", // Nhận từ request
                totalAmount: i === 0 ? totalAmount : 0,
                paymentStatus: "Paid",
                ticketStatus: "Booked",
                bookedAt: new Date()
            });

            await newTicket.save();
            createdTickets.push(newTicket);
        }

        res.json({ success: true, bookingCode, tickets: createdTickets });
    } catch (error) {
        console.error("❌ Error creating booking:", error);
        res.status(500).json({ success: false, message: error.message });
    }
});

router.get("/my-tickets", verifyToken, async (req, res) => {
    try {
        console.log(`🎫 Fetching tickets for userId: ${req.userId}`);

        // Tìm vé theo userId (hỗ trợ cả String và ObjectId)
        let tickets = await Ticket.find({ userId: req.userId }).sort({ createdAt: -1 }).lean();

        for (let ticket of tickets) {
            // Tìm chuyến bay theo flightId (String ID)
            const flight = await Flight.findOne({ flightId: ticket.flightId }).lean();
            if (flight) {
                flight.departureAirport = await Airport.findOne({ airportId: flight.fromAirportId });
                flight.arrivalAirport = await Airport.findOne({ airportId: flight.toAirportId });
                flight.airline = await Airline.findOne({ airlineId: flight.airlineId });
                ticket.flightData = flight;
            }
        }

        console.log(`✅ Found ${tickets.length} tickets`);
        res.json(tickets);
    } catch (error) {
        console.error(error);
        res.status(500).json({ success: false });
    }
});

module.exports = router;

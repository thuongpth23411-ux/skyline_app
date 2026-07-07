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

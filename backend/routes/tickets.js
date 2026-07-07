const express = require("express");
const router = express.Router();
const Ticket = require("../models/Ticket");
const Flight = require("../models/Flight");
const jwt = require("jsonwebtoken");

// Middleware to verify JWT
const verifyToken = (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return res.status(401).json({ success: false, message: "Không có quyền truy cập" });
    }
    const token = authHeader.split(" ")[1];
    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.userId = decoded.id;
        next();
    } catch (err) {
        return res.status(401).json({ success: false, message: "Token không hợp lệ" });
    }
};

// Get all tickets for the logged-in user
router.get("/my-tickets", verifyToken, async (req, res) => {
    try {
        const tickets = await Ticket.find({ userId: req.userId })
            .populate("flightId")
            .sort({ createdAt: -1 });

        res.json(tickets);
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ khi lấy danh sách vé" });
    }
});

// Create a mock ticket for testing (if user has none)
router.post("/mock", verifyToken, async (req, res) => {
    try {
        const firstFlight = await Flight.findOne();
        if (!firstFlight) {
            return res.status(404).json({ message: "No flights found to create mock ticket" });
        }

        const newTicket = new Ticket({
            userId: req.userId,
            flightId: firstFlight._id,
            bookingCode: "SK" + Math.floor(100000 + Math.random() * 900000),
            seatNumber: "08C",
            ticketClass: "Phổ thông",
            status: "UPCOMING",
            passengerName: "User Test",
            totalPrice: firstFlight.priceOptions[0]?.price || 2000000
        });

        await newTicket.save();
        res.json({ success: true, message: "Created mock ticket", ticket: newTicket });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

module.exports = router;

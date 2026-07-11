const express = require("express");
const router = express.Router();
const Ticket = require("../models/Ticket");
const Flight = require("../models/Flight");
const Airport = require("../models/Airport");
const Airline = require("../models/Airline");
const User = require("../models/User");
const bcrypt = require("bcryptjs");
const { sendTicketEmail } = require("../utils/mailer");
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
        const { userId, flights, passengerName, totalAmount, paymentMethod, email } = req.body;
        const bookingCode = "SKL" + Math.floor(100000 + Math.random() * 900000);

        // 1. Kiểm tra trường hợp khách vãng lai
        let targetUserId = userId;
        let tempAccount = null;

        if (!userId || userId.startsWith("guest_")) {
            // Kiểm tra email đã tồn tại chưa
            let user = await User.findOne({ email });

            const randomPass = Math.random().toString(36).slice(-8);
            const hashedPassword = await bcrypt.hash(randomPass, 10);

            if (user) {
                // Nếu User đã tồn tại nhưng mua vãng lai, ta cập nhật lại mật khẩu mới cho họ dễ đăng nhập
                user.password = hashedPassword;
                await user.save();
                targetUserId = user._id;
                tempAccount = { email, password: randomPass, isExisting: true };
            } else {
                // Tạo tài khoản mới hoàn toàn
                const newUser = new User({
                    fullName: passengerName,
                    email: email,
                    password: hashedPassword,
                    isVerified: true,
                    rank: "Đồng"
                });
                const savedUser = await newUser.save();
                targetUserId = savedUser._id;
                tempAccount = { email, password: randomPass, isExisting: false };
            }
        }

        const createdTickets = [];

        for (let i = 0; i < flights.length; i++) {
            const flight = flights[i];
            const ticketId = "TKT" + Math.floor(10000000 + Math.random() * 90000000);

            const newTicket = new Ticket({
                ticketId: ticketId,
                bookingCode: bookingCode,
                userId: targetUserId,
                flightId: flight.flightId,
                seatId: flight.seatNumber || "N/A",
                passengerName: passengerName,
                passengerType: "Adult",
                ticketType: flight.ticketType || "OneWay",
                totalAmount: i === 0 ? totalAmount : 0,
                paymentStatus: "Paid",
                ticketStatus: "Booked",
                bookedAt: new Date()
            });

            await newTicket.save();

            // Fetch flight details specifically for the email payload
            const flightData = await Flight.findOne({ flightId: flight.flightId }).lean();
            const ticketForEmail = newTicket.toObject();

            if (flightData) {
                const depAirport = await Airport.findOne({ airportId: flightData.fromAirportId }).lean();
                const arrAirport = await Airport.findOne({ airportId: flightData.toAirportId }).lean();
                ticketForEmail.flightDetails = {
                    flightNumber: flightData.flightNumber,
                    departureCode: depAirport ? depAirport.airportId : "---", // airportId usually is the code (HAN, SGN)
                    arrivalCode: arrAirport ? arrAirport.airportId : "---",
                    departureAirport: depAirport ? depAirport.airportName : "Sân bay đi",
                    arrivalAirport: arrAirport ? arrAirport.airportName : "Sân bay đến",
                    time: flightData.departureAt
                };
            }

            createdTickets.push(ticketForEmail);
        }

        // 2. Gửi Email thông báo
        const targetEmail = email || (tempAccount ? tempAccount.email : null);
        if (targetEmail) {
            await sendTicketEmail(targetEmail, {
                bookingCode,
                passengerName,
                tickets: createdTickets,
                tempAccount
            });
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

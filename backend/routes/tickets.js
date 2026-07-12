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
const PointHistory = require("../models/PointHistory");

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
        console.log("📥 Received create booking request:", JSON.stringify(req.body, null, 2));
        const { userId, flights, passengerName, totalAmount, paymentMethod, email, oldTicketId } = req.body;

        if (!flights || !Array.isArray(flights) || flights.length === 0) {
            console.error("❌ Flights data is missing or invalid");
            return res.status(400).json({ success: false, message: "Thông tin chuyến bay không hợp lệ" });
        }

        // Nếu là đổi vé, tiến hành xóa vé cũ (theo bookingCode)
        if (oldTicketId) {
            const deleteResult = await Ticket.deleteMany({ bookingCode: oldTicketId });
            console.log(`🗑️ Deleted old ticket(s) for exchange: ${oldTicketId}, count: ${deleteResult.deletedCount}`);
        }

        let bookingCode;
        let isCodeUnique = false;
        let retryCount = 0;

        while (!isCodeUnique && retryCount < 5) {
            bookingCode = "SKL" + Math.floor(100000 + Math.random() * 900000);
            const existingBooking = await Ticket.findOne({ bookingCode });
            if (!existingBooking) {
                isCodeUnique = true;
            }
            retryCount++;
        }

        if (!isCodeUnique) {
            throw new Error("Không thể tạo mã đặt chỗ duy nhất. Vui lòng thử lại.");
        }

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

        const ticketDocs = [];

        for (let i = 0; i < flights.length; i++) {
            const flight = flights[i];

            // Tạo ticketId duy nhất
            let ticketId;
            let isTicketIdUnique = false;
            while (!isTicketIdUnique) {
                ticketId = "TKT" + Math.floor(10000000 + Math.random() * 90000000);
                const existing = await Ticket.findOne({ ticketId });
                if (!existing) isTicketIdUnique = true;
            }

            const newTicket = new Ticket({
                ticketId: ticketId,
                bookingCode: bookingCode,
                userId: targetUserId.toString(),
                flightId: flight.flightId,
                seatId: flight.seatNumber || "N/A",
                passengerName: passengerName,
                passengerType: "Adult",
                ticketType: flight.ticketType || (flights.length > 1 ? (i === 0 ? "Chiều đi" : "Chiều về") : "Một chiều"),
                totalAmount: i === 0 ? totalAmount : 0,
                paymentStatus: "Paid",
                ticketStatus: "Booked",
                bookedAt: new Date()
            });

            ticketDocs.push(newTicket);
        }

        // Lưu tất cả vé
        const savedTickets = await Ticket.insertMany(ticketDocs);
        console.log(`✅ Saved ${savedTickets.length} tickets with bookingCode: ${bookingCode}`);

        // 1.5 CỘNG ĐIỂM THÀNH VIÊN: 100.000 VNĐ = 10 điểm
        if (totalAmount > 0) {
            const pointsToEarn = Math.floor(totalAmount / 100000) * 10;
            if (pointsToEarn > 0) {
                const user = await User.findById(targetUserId);
                if (user) {
                    user.skyPoints = (user.skyPoints || 0) + pointsToEarn;

                    // Tự động nâng hạng nếu đủ điểm (Ví dụ đơn giản)
                    if (user.skyPoints >= 5000) user.rank = "Kim cương";
                    else if (user.skyPoints >= 2000) user.rank = "Vàng";
                    else if (user.skyPoints >= 500) user.rank = "Bạc";

                    await user.save();

                    // Lưu lịch sử tích điểm
                    await PointHistory.create({
                        userId: targetUserId,
                        points: pointsToEarn,
                        type: "EARN",
                        description: `Tích điểm từ đơn hàng ${bookingCode}`,
                        bookingCode: bookingCode
                    });
                    console.log(`💰 Awarded ${pointsToEarn} points to user ${user.fullName}`);
                }
            }
        }

        const createdTickets = [];
        for (let savedTicket of savedTickets) {
            // Fetch flight details specifically for the email payload
            const flightData = await Flight.findOne({ flightId: savedTicket.flightId }).lean();
            const ticketForEmail = savedTicket.toObject();

            if (flightData) {
                const depAirport = await Airport.findOne({ airportId: flightData.fromAirportId }).lean();
                const arrAirport = await Airport.findOne({ airportId: flightData.toAirportId }).lean();
                ticketForEmail.flightDetails = {
                    flightNumber: flightData.flightNumber,
                    departureCode: depAirport ? depAirport.airportId : "---",
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
        let tickets = await Ticket.find({ userId: req.userId }).sort({ bookedAt: -1 }).lean();

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

router.post("/share", async (req, res) => {
    try {
        const { email, bookingCode } = req.body;

        // Find tickets associated with this bookingCode
        const tickets = await Ticket.find({ bookingCode }).lean();
        if (!tickets || tickets.length === 0) {
            return res.status(404).json({ success: false, message: "Không tìm thấy thông tin vé" });
        }

        const passengerName = tickets[0].passengerName;
        const ticketList = [];

        for (const ticket of tickets) {
            const flightData = await Flight.findOne({ flightId: ticket.flightId }).lean();
            const ticketObj = { ...ticket };

            if (flightData) {
                const depAirport = await Airport.findOne({ airportId: flightData.fromAirportId }).lean();
                const arrAirport = await Airport.findOne({ airportId: flightData.toAirportId }).lean();
                ticketObj.flightDetails = {
                    flightNumber: flightData.flightNumber,
                    departureCode: depAirport ? depAirport.airportId : "---",
                    arrivalCode: arrAirport ? arrAirport.airportId : "---",
                    departureAirport: depAirport ? depAirport.airportName : "Sân bay đi",
                    arrivalAirport: arrAirport ? arrAirport.airportName : "Sân bay đến",
                    time: flightData.departureAt
                };
            }
            ticketList.push(ticketObj);
        }

        await sendTicketEmail(email, {
            bookingCode,
            passengerName,
            tickets: ticketList,
            isShare: true
        });

        res.json({ success: true, message: "Email đã được gửi thành công" });
    } catch (error) {
        console.error("❌ Error sharing ticket:", error);
        res.status(500).json({ success: false, message: error.message });
    }
});

module.exports = router;

const express = require("express");
const router = express.Router();
const PassengerDirectory = require("../models/PassengerDirectory");
const jwt = require("jsonwebtoken");

// Middleware to verify JWT
const verifyToken = (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return res.status(401).json({ success: false, message: "Unauthorized" });
    }
    const token = authHeader.split(" ")[1];
    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.userId = decoded.id;
        next();
    } catch (err) {
        res.status(401).json({ success: false, message: "Invalid token" });
    }
};

// Add new passenger
router.post("/add", verifyToken, async (req, res) => {
    try {
        console.log("Adding passenger for user:", req.userId);
        console.log("Body:", req.body);

        const { passengerName, passengerPhone, passengerCccd, passengerDob, passengerEmail } = req.body;

        if (!passengerName) {
            return res.status(400).json({ success: false, message: "Thiếu họ tên hành khách" });
        }

        const newPassenger = new PassengerDirectory({
            userId: req.userId,
            passengerName,
            passengerPhone,
            passengerCccd,
            passengerDob,
            passengerEmail
        });

        await newPassenger.save();
        console.log("Passenger added successfully:", newPassenger._id);
        res.status(201).json({ success: true, message: "Thêm hành khách thành công", data: newPassenger });
    } catch (error) {
        console.error("Add Passenger Error:", error);
        res.status(500).json({ success: false, message: error.message || "Lỗi máy chủ" });
    }
});

// Get all passengers for current user
router.get("/my-passengers", verifyToken, async (req, res) => {
    try {
        const passengers = await PassengerDirectory.find({ userId: req.userId }).sort({ createdAt: -1 });
        res.json(passengers);
    } catch (error) {
        console.error("Fetch Passengers Error:", error);
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Update passenger
router.put("/update/:id", verifyToken, async (req, res) => {
    try {
        const { passengerName, passengerPhone, passengerCccd, passengerDob, passengerEmail } = req.body;
        const passenger = await PassengerDirectory.findOneAndUpdate(
            { _id: req.params.id, userId: req.userId },
            { passengerName, passengerPhone, passengerCccd, passengerDob, passengerEmail },
            { new: true }
        );

        if (!passenger) {
            return res.status(404).json({ success: false, message: "Không tìm thấy hành khách" });
        }

        res.json({ success: true, message: "Cập nhật thành công", data: passenger });
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

// Delete passenger
router.delete("/delete/:id", verifyToken, async (req, res) => {
    try {
        const result = await PassengerDirectory.findOneAndDelete({ _id: req.params.id, userId: req.userId });
        if (!result) {
            return res.status(404).json({ success: false, message: "Không tìm thấy hành khách" });
        }
        res.json({ success: true, message: "Xóa thành công" });
    } catch (error) {
        res.status(500).json({ success: false, message: "Lỗi máy chủ" });
    }
});

module.exports = router;
